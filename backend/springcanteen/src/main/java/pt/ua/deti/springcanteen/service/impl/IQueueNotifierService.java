package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import pt.ua.deti.springcanteen.entities.Employee;
import pt.ua.deti.springcanteen.entities.EmployeeRole;
import pt.ua.deti.springcanteen.service.EmployeeService;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.QueueNotifierService;

import java.security.Principal;
import java.util.Optional;

@Service
@AllArgsConstructor
public class IQueueNotifierService implements QueueNotifierService {

    private static final String ORDER_TOPIC = "/topic/orders";
    private static final Logger logger = LoggerFactory.getLogger(IQueueNotifierService.class);
    private SimpMessagingTemplate websocketClient;
    private OrderManagementService orderManagementService;
    private EmployeeService employeeService;


    @Override
    @EventListener
    public void sendExistingOrderQueues(SessionSubscribeEvent event) {
        Principal user = event.getUser();
        if (user != null){
            Optional<Employee> employeeOpt = employeeService.getEmployeeByEmail(user.getName());
            if (employeeOpt.isPresent()){
                EmployeeRole employeeRole = employeeOpt.get().getRole();
                if (employeeRole != EmployeeRole.COOK && employeeRole != EmployeeRole.DESK_ORDERS){
                    logger.info("Employee role is not COOK or DESK_ORDERS. No need to send all orders");
                } else {
                    websocketClient.convertAndSendToUser(user.getName(), ORDER_TOPIC, orderManagementService.getAllOrders());
                    logger.info("Sent all orders to user {}", user.getName());
                }
            } else {
                logger.info("Employee corresponding to the user received in websocket is null");
            }
        } else {
            logger.info("User received in websocket is null");
        }
    }
}
