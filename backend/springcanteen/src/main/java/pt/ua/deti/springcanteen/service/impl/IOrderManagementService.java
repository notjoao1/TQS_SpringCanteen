package pt.ua.deti.springcanteen.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.service.OrderManagementService;

import java.util.Queue;

@Service
public class IOrderManagementService implements OrderManagementService {

    private final Queue<Order> regularOrders;
    private final Queue<Order> priorityOrders;

    @Autowired
    public IOrderManagementService(Queue<Order> regularOrders, Queue<Order> priorityOrders) {
        this.regularOrders = regularOrders;
        this.priorityOrders = priorityOrders;
    }

    public boolean addOrder(Order order) {
        if (order.isPriority()) {
            return priorityOrders.add(order);
        }
        return regularOrders.add(order);
    }

    public boolean removeOrder(Order order){
        if (order.isPriority()) {
            return priorityOrders.remove(order);
        }
        return regularOrders.remove(order);
    }

}
