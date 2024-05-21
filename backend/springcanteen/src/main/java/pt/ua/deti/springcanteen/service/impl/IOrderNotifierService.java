package pt.ua.deti.springcanteen.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import pt.ua.deti.springcanteen.dto.OrderUpdateResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;

@Service
@AllArgsConstructor
public class IOrderNotifierService implements OrderNotifierService {
    private static final Logger logger = LoggerFactory.getLogger(IOrderNotifierService.class);
    private static final String ORDER_TOPIC = "/topic/orders";

    private OrderManagementService orderManagementService;
    private SimpMessagingTemplate websocketClient;

    @Override
    public void sendNewOrder(Order order) {
        logger.info("Sending new order to /topic/orders. Order {}", order.toString());
        websocketClient.convertAndSend(ORDER_TOPIC, order);
    }

    @Override
    @EventListener
    public void sendExistingOrderQueues(SessionSubscribeEvent event) {
        // websocketClient.convertAndSendToUser(event.getUser().getName(), "/topic/orders", payload);
        websocketClient.convertAndSend(ORDER_TOPIC, orderManagementService.getAllOrders());
    }

    @Override
    public void sendOrderStatusUpdates(Long orderId, OrderStatus newOrderStatus) {
        OrderUpdateResponseDTO updateResponse = new OrderUpdateResponseDTO();
        updateResponse.setOrderId(orderId);
        updateResponse.setNewOrderStatus(newOrderStatus); 
        logger.info("Sending order update to /topic/orders. New status - {}; Order id - {}", newOrderStatus, orderId);
        websocketClient.convertAndSend(ORDER_TOPIC, updateResponse);
    }
    
}
