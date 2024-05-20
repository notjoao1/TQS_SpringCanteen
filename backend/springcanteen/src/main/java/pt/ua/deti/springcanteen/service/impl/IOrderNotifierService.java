package pt.ua.deti.springcanteen.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import pt.ua.deti.springcanteen.dto.OrderUpdateResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.service.OrderNotifierService;

@Service
@AllArgsConstructor
public class IOrderNotifierService implements OrderNotifierService {
    private static final Logger logger = LoggerFactory.getLogger(IOrderNotifierService.class);

    private SimpMessagingTemplate websocketClient;

    @Override
    public void sendNewOrder(Order order) {
        logger.info("Sending new order to /topic/orders. Order {}", order.toString());
        websocketClient.convertAndSend("/topic/orders", order);
    }

    @Override
    public void sendExistingOrderQueues() {
        throw new UnsupportedOperationException("Unimplemented method 'sendExistingOrderQueues'");
    }

    @Override
    public void sendOrderStatusUpdates(Order order, OrderStatus newOrderStatus) {
        OrderUpdateResponseDTO updateResponse = new OrderUpdateResponseDTO();
        updateResponse.setOrderId(order.getId());
        updateResponse.setNewOrderStatus(newOrderStatus); 
        logger.info("Sending order update to /topic/orders. New status - {}; Order - {}", newOrderStatus, order);
        websocketClient.convertAndSend("/topic/orders", updateResponse);
    }
    
}
