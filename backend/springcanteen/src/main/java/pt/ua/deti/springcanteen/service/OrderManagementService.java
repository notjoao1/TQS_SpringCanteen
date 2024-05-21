package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.entities.Order;

public interface OrderManagementService {
    boolean manageOrder(Order order);

    QueueOrdersDTO getAllIdleOrders();
}
