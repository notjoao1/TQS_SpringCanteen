package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.controllers.MenuController;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;

import java.util.Queue;

@Service
@Getter
@AllArgsConstructor
public class IOrderManagementService implements OrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(IOrderManagementService.class);
    private OrderNotifierService orderNotifierService;
    private OrderRepository orderRepository;
    private final Queue<Order> regularIdleOrders;
    private final Queue<Order> priorityIdleOrders;
    private final Queue<Order> regularPreparingOrders;
    private final Queue<Order> priorityPreparingOrders;
    private final Queue<Order> regularReadyOrders;
    private final Queue<Order> priorityReadyOrders;

    public boolean manageOrder(Order order) {
        Queue<Order> oldOrderQueue;
        Queue<Order> newOrderQueue;
        boolean result = false;
        switch (order.getOrderStatus()){
            case NOT_PAID:
                newOrderQueue = order.isPriority() ? regularIdleOrders : priorityIdleOrders;
                order.setOrderStatus(OrderStatus.IDLE);
                if(result = newOrderQueue.add(order))
                    orderNotifierService.sendNewOrder(order);
                break;
            case IDLE:
                if (order.isPriority()){
                    oldOrderQueue = priorityIdleOrders;
                    newOrderQueue = priorityPreparingOrders;
                } else {
                    oldOrderQueue = regularIdleOrders;
                    newOrderQueue = regularPreparingOrders;
                }
                if (oldOrderQueue.remove(order)){
                    order.setOrderStatus(OrderStatus.PREPARING);
                    if (result = newOrderQueue.add(order)){
                        orderRepository.save(order);
                        orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.PREPARING);
                    }
                }
                break;
            case PREPARING:
                if (order.isPriority()){
                    oldOrderQueue = priorityPreparingOrders;
                    newOrderQueue = priorityReadyOrders;
                } else {
                    oldOrderQueue = regularPreparingOrders;
                    newOrderQueue = regularReadyOrders;
                }
                if (oldOrderQueue.remove(order)){
                    order.setOrderStatus(OrderStatus.READY);
                    if (result = newOrderQueue.add(order)){
                        orderRepository.save(order);
                        orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.READY);
                    }
                }
                break;
            case READY:
                oldOrderQueue = order.isPriority() ? priorityReadyOrders : regularReadyOrders;
                if (oldOrderQueue.remove(order)){
                    order.setOrderStatus(OrderStatus.PICKED_UP);
                    orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.PICKED_UP);
                }
                break;
            default:
                logger.info("Invalid Order Status {}", order.getOrderStatus());
        }
        return result;
    }

    public QueueOrdersDTO getAllOrders(){
        return new QueueOrdersDTO(regularIdleOrders, priorityIdleOrders);
    }

}
