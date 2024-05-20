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
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.MenuService;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.PriceService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class IOrderService implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(IOrderService.class);
    private PriceService priceService;
    private MenuService menuService;
    private OrderManagementService orderManagementService;
    private OrderRepository orderRepository;
    private OrderMenuRepository orderMenuRepository;
    private OrderNotifierService orderNotifierService;

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
            if (orderManagementService.addOrder(order)) {
                logger.info("Successfully added IDLE order to queue. Sending it through Websockets...");
                orderNotifierService.sendNewOrder(order);
            }
            else
                logger.error("Could not add IDLE order to queue...");
            
        }
        orderMenuRepository.saveAll(orderMenus);
        return Optional.of(order);
    }

    private Order orderEntityFromDTO(CustomizeOrderDTO customizeOrderDTO) {
        KioskTerminal kioskTerminal = new KioskTerminal();
        kioskTerminal.setId(customizeOrderDTO.getKioskId());
        
        Order order;
        if (Boolean.TRUE.equals(customizeOrderDTO.getIsPaid()))
            order = new Order(OrderStatus.IDLE, customizeOrderDTO.getIsPaid(), customizeOrderDTO.getIsPriority(), customizeOrderDTO.getNif(), kioskTerminal);
        else 
            order = new Order(OrderStatus.NOT_PAID, customizeOrderDTO.getIsPaid(), customizeOrderDTO.getIsPriority(), customizeOrderDTO.getNif(), kioskTerminal);
        
        Set<OrderMenu> orderMenus = new HashSet<>();
        // check if all menus provided exist and add them to orderMenus set
        for (OrderMenuDTO orderMenuDTO : customizeOrderDTO.getOrderMenus()) {
            Optional<Menu> menuOpt = menuService.getMenuById(orderMenuDTO.getMenuId());
            if (menuOpt.isEmpty())
                throw new InvalidOrderException(String.format("Order has an invalid menu that does not exist with id '%s'", orderMenuDTO.getMenuId()));
            orderMenus.add(new OrderMenu(order, menuOpt.get(), new Gson().toJson(orderMenuDTO.getCustomization())));
        }
        order.setOrderMenus(orderMenus);
        return order;
    }

    public Optional<Order> changeOrderStatus(Long orderId, OrderStatus newOrderStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty())
            return Optional.empty();

        Order order = orderOpt.get();
        if (!isValidOrderStatusChange(order.getOrderStatus(), newOrderStatus)) {
            logger.error("Invalid order status change from {} to {}", order.getOrderStatus(), newOrderStatus);
            return Optional.empty();
        }
        // TODO: para além de dar update no status na DB, temos de dar update do status das Orders nas queues
        // TODO: quando uma order passa de READY para PICKED_UP, mandar mensagem pela websocket só para no frontend
        //       ficar confirmado que está terminada a order?
        // order gets paid
        if (newOrderStatus == OrderStatus.IDLE) {
            if(orderManagementService.addOrder(order)){
                order.setOrderStatus(newOrderStatus);
                orderNotifierService.sendNewOrder(order);
                logger.info("Order with id {} added to queue and OrderStatus changed to {}", order.getId(), newOrderStatus);
            } else {
                logger.error("Order with id {} could not be added to queue. OrderStatus unchanged.", order.getId());
            }
        } else if (newOrderStatus == OrderStatus.PREPARING || newOrderStatus == OrderStatus.READY) {
            logger.info("Order with id {} status updated from {} to {}", order.getId(), order.getOrderStatus(), newOrderStatus);
            order.setOrderStatus(newOrderStatus);
            orderNotifierService.sendOrderStatusUpdates(order.getId(), newOrderStatus);
        } else if (newOrderStatus == OrderStatus.PICKED_UP) { // order finished
            if (orderManagementService.removeOrder(order)){
                order.setOrderStatus(newOrderStatus);
                logger.info("Order with id {} removed from the queue and OrderStatus changed to {}", order.getId(), newOrderStatus);
            } else {
                logger.error("Order with id {} could not be removed from the queue. OrderStatus unchanged.", order.getId());
            }
        }
        orderRepository.save(order);
        return Optional.of(order);
    }

    private boolean isValidOrderStatusChange(OrderStatus oldOrderStatus, OrderStatus newOrderStatus) {
        if (
            (oldOrderStatus == null && newOrderStatus != OrderStatus.IDLE) ||
            (oldOrderStatus == OrderStatus.IDLE && newOrderStatus != OrderStatus.PREPARING) ||
            (oldOrderStatus == OrderStatus.PREPARING && newOrderStatus != OrderStatus.READY) ||
            (oldOrderStatus == OrderStatus.READY && newOrderStatus != OrderStatus.PICKED_UP)
        ) return false;
        
        return true;
    }
}
