package pt.ua.deti.springcanteen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.entities.Order;

public interface OrderManagementService {

    Logger logger = LoggerFactory.getLogger(OrderManagementService.class);

    default boolean manageOrder(Order order) {
        switch (order.getOrderStatus()){
            case NOT_PAID:
                return manageNotPaidOrder(order);
            case IDLE:
                return manageIdleOrder(order);
            case PREPARING:
                return managePreparingOrder(order);
            case READY:
                return manageReadyOrder(order);
            default:
                logger.info("Invalid Order Status {}", order.getOrderStatus());
        }
        return false;
    }

    boolean addNewIdleOrder(Order order);

    boolean manageNotPaidOrder(Order order);
    boolean manageIdleOrder(Order order);
    boolean managePreparingOrder(Order order);
    boolean manageReadyOrder(Order order);

    QueueOrdersDTO getAllOrders();
}
