package pt.ua.deti.springcanteen.service;

import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;

public interface OrderNotifierService {
    void sendNewOrder(Order order);

    void sendOrderStatusUpdates(Long orderId, OrderStatus newOrderStatus);
}
