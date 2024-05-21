package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.QueueNotifierService;

@Service
@AllArgsConstructor
public class IQueueNotifierService implements QueueNotifierService {

    private static final String ORDER_TOPIC = "/topic/orders";
    private SimpMessagingTemplate websocketClient;
    private OrderManagementService orderManagementService;


    @Override
    @EventListener
    public void sendExistingOrderQueues(SessionSubscribeEvent event) {
        // websocketClient.convertAndSendToUser(event.getUser().getName(), "/topic/orders", payload);
        websocketClient.convertAndSend(
                ORDER_TOPIC,
                orderManagementService.getAllIdleOrders()
        );
    }
}
