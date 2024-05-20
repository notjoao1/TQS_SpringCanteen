package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.entities.Order;

public interface OrderManagementService {
    boolean addOrder(Order order);
    boolean removeOrder(Order order);
}
