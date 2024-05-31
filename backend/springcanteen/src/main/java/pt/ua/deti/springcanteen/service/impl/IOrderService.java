package pt.ua.deti.springcanteen.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.OrderMenuDTO;
import pt.ua.deti.springcanteen.entities.KioskTerminal;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.exceptions.InvalidStatusChangeException;
import pt.ua.deti.springcanteen.exceptions.QueueTransferException;
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.MenuService;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.PriceService;

import java.util.*;

@Service
@AllArgsConstructor
public class IOrderService implements OrderService {
  private static final Logger logger = LoggerFactory.getLogger(IOrderService.class);
  private PriceService priceService;
  private MenuService menuService;
  private OrderManagementService orderManagementService;
  private OrderRepository orderRepository;
  private OrderMenuRepository orderMenuRepository;

  @Override
  @Transactional
  public Optional<Order> createOrder(CustomizeOrderDTO customizeOrderDTO) {
    logger.info("Creating order...");

    Order order = this.orderEntityFromDTO(customizeOrderDTO);

    float totalOrderPrice = 0.0f;
    Set<OrderMenu> orderMenus = order.getOrderMenus();
    for (OrderMenu orderMenu : orderMenus) {
      orderMenu.setOrder(order);
      float orderMenuPrice = priceService.getOrderMenuPrice(orderMenu);
      totalOrderPrice = totalOrderPrice + orderMenuPrice;
      orderMenu.setCalculatedPrice(orderMenuPrice);
    }
    order.setPrice(totalOrderPrice);
    orderRepository.save(order);
    // add order to queue that is paid and therefore, ready to be cooked (idle status)
    if (order.getOrderStatus() == OrderStatus.IDLE) {
      logger.info("Created order is in IDLE status, ready to cook -> adding it to the queue...");
      if (orderManagementService.addNewIdleOrder(order)) {
        logger.info("Successfully added IDLE order to queue. Sending it through Websockets...");
      } else logger.error("Could not add IDLE order to queue...");
    }
    orderMenuRepository.saveAll(orderMenus);
    return Optional.of(order);
  }

  private Order orderEntityFromDTO(CustomizeOrderDTO customizeOrderDTO) {
    KioskTerminal kioskTerminal = new KioskTerminal();
    kioskTerminal.setId(customizeOrderDTO.getKioskId());

    Order order;
    if (Boolean.TRUE.equals(customizeOrderDTO.getIsPaid()))
      order =
          new Order(
              OrderStatus.IDLE,
              customizeOrderDTO.getIsPaid(),
              customizeOrderDTO.getIsPriority(),
              customizeOrderDTO.getNif(),
              kioskTerminal);
    else
      order =
          new Order(
              OrderStatus.NOT_PAID,
              customizeOrderDTO.getIsPaid(),
              customizeOrderDTO.getIsPriority(),
              customizeOrderDTO.getNif(),
              kioskTerminal);

    Set<OrderMenu> orderMenus = new HashSet<>();
    // check if all menus provided exist and add them to orderMenus set
    for (OrderMenuDTO orderMenuDTO : customizeOrderDTO.getOrderMenus()) {
      Optional<Menu> menuOpt = menuService.getMenuById(orderMenuDTO.getMenuId());
      if (menuOpt.isEmpty())
        throw new InvalidOrderException(
            String.format(
                "Order has an invalid menu that does not exist with id '%s'",
                orderMenuDTO.getMenuId()));
      orderMenus.add(
          new OrderMenu(order, menuOpt.get(), new Gson().toJson(orderMenuDTO.getCustomization())));
    }
    order.setOrderMenus(orderMenus);
    return order;
  }

  public Optional<Order> changeNotPaidOrderToNextOrderStatus(Long orderId) {
    return changeToNextOrderStatus(orderId, List.of(OrderStatus.NOT_PAID));
  }

  public Optional<Order> changePaidOrderToNextOrderStatus(Long orderId) {
    return changeToNextOrderStatus(orderId, List.of(OrderStatus.IDLE, OrderStatus.PREPARING, OrderStatus.READY));
  }

  private Optional<Order> changeToNextOrderStatus(Long orderId, List<OrderStatus> availableOldOrderStatus){

    Optional<Order> orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isEmpty()) return Optional.empty();

    Order order = orderOpt.get();
    if (availableOldOrderStatus.contains(order.getOrderStatus()))
      throw new InvalidStatusChangeException(String.format(
              "Can only change status from Order with the following status: %s. Got %s",
              Arrays.toString(availableOldOrderStatus.toArray()), order.getOrderStatus()
      ));
    else
      logger.info("Changing from order status {}", order.getOrderStatus());

    if (orderManagementService.manageOrder(order)) {
      logger.info(
              "Order with id {} moved to the next queue and OrderStatus changed to {}",
              order.getId(), order.getOrderStatus()
      );
      return Optional.of(order);
    }
    logger.error(
            "Order with id {} could not be transferred to another queue. OrderStatus unchanged: {}",
            order.getId(), order.getOrderStatus()
    );
    throw new QueueTransferException(String.format(
            "Order with id %s could not be transferred to another queue. OrderStatus unchanged: %s",
            order.getId(), order.getOrderStatus()
    ));

  }
}
