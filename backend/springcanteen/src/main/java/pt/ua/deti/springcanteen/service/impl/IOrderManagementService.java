package pt.ua.deti.springcanteen.service.impl;

import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.service.OrderManagementService;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class IOrderManagementService implements OrderManagementService {

    private static final int QUEUE_CAPACITY = 120;
    private final Queue<Order> regularOrders;
    private final Queue<Order> priorityOrders;

    public IOrderManagementService() {
        this.regularOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.priorityOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
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
