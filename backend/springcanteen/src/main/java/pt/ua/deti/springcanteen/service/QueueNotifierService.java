package pt.ua.deti.springcanteen.service;

import org.springframework.web.socket.messaging.SessionSubscribeEvent;

public interface QueueNotifierService {
    void sendExistingOrderQueues(SessionSubscribeEvent event);
}
