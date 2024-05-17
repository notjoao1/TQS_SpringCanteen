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
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.MenuService;
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
    private OrderRepository orderRepository;
    private OrderMenuRepository orderMenuRepository;
    private MenuService menuService;

    @Override
    @Transactional
    public Optional<Order> createOrder(CustomizeOrderDTO customizeOrderDTO) {
        logger.info("Creating order...");

        Optional<Order> orderOpt = this.orderEntityFromDTO(customizeOrderDTO);
        if (orderOpt.isEmpty())
            return Optional.empty();

        Order order = orderOpt.get();
        float totalOrderPrice = 0.0f;
        Set<OrderMenu> orderMenus = order.getOrderMenus();
        for (OrderMenu orderMenu : orderMenus) {
            orderMenu.setOrder(order);
            totalOrderPrice = totalOrderPrice + priceService.getOrderMenuPrice(orderMenu);
        }
        order.setPrice(totalOrderPrice);
        orderRepository.save(order);
        orderMenuRepository.saveAll(orderMenus);
        return Optional.of(order);
    }

    private Optional<Order> orderEntityFromDTO(CustomizeOrderDTO customizeOrderDTO) {
        KioskTerminal kioskTerminal = new KioskTerminal();
        kioskTerminal.setId(customizeOrderDTO.getKiosk_id());
        Order order = new Order(OrderStatus.IDLE, customizeOrderDTO.getIsPaid(), customizeOrderDTO.getIsPriority(), customizeOrderDTO.getNif(), kioskTerminal);
        Set<OrderMenu> orderMenus = new HashSet<>();
        // check if all menus provided exist and add them to orderMenus set
        for (OrderMenuDTO orderMenuDTO : customizeOrderDTO.getOrderMenus()) {
            Optional<Menu> menuOpt = menuService.getMenuById(orderMenuDTO.getMenu_id());
            if (menuOpt.isEmpty())
                return Optional.empty();
            orderMenus.add(new OrderMenu(order, menuOpt.get(), new Gson().toJson(orderMenuDTO.getCustomization())));
        }
        order.setOrderMenus(orderMenus);
        return Optional.of(order);

    }
}
