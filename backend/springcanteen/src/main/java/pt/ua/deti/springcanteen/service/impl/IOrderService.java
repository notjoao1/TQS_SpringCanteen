package pt.ua.deti.springcanteen.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.PriceService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class IOrderService implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(IOrderService.class);
    private PriceService priceService;
    private OrderRepository orderRepository;
    private OrderMenuRepository orderMenuRepository;

    @Override
    @Transactional
    public Optional<Order> createOrder(CustomizeOrderDTO customizeOrderDTO) {
        logger.info("Creating order...");

        Order order = orderRepository.save(customizeOrderDTO.toOrderEntity());
        Set<OrderMenu> orderMenus = order.getOrderMenus();
        for (OrderMenu orderMenu : orderMenus) {
            orderMenu.setOrder(order);
        }
        orderMenuRepository.saveAll(orderMenus);
        return Optional.of(order);
    }


}
