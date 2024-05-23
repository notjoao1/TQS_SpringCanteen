package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
@Getter
@Setter
@AllArgsConstructor
public class IOrderManagementService implements OrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(IOrderManagementService.class);
    private OrderNotifierService orderNotifierService;
    private OrderRepository orderRepository;
    private Queue<OrderEntry> regularIdleOrders;
    private Queue<OrderEntry> priorityIdleOrders;
    private Queue<OrderEntry> regularPreparingOrders;
    private Queue<OrderEntry> priorityPreparingOrders;
    private Queue<OrderEntry> regularReadyOrders;
    private Queue<OrderEntry> priorityReadyOrders;

    public boolean manageOrder(Order order) {
        Queue<OrderEntry> oldOrderQueue;
        Queue<OrderEntry> newOrderQueue;
        boolean result = false;
        logger.info("Order status {}", order.getOrderStatus());
        switch (order.getOrderStatus()){
            case NOT_PAID:
            
            newOrderQueue = order.isPriority() ? priorityIdleOrders : regularIdleOrders;
            order.setOrderStatus(OrderStatus.IDLE);
            result = newOrderQueue.add(OrderEntry.fromOrderEntity(order));
                logger.info("newOrderQueue = regularIdleOrders {}", newOrderQueue == regularIdleOrders);
                if(result){
                    logger.info("order status {}", order.getOrderStatus());
                    logger.info("newOrderQueue = regularIdleOrders {}", newOrderQueue == regularIdleOrders);
                    logger.info("newOrderQueue {}", newOrderQueue);
                    logger.info("regularIdleOrders {}", regularIdleOrders);
                    orderRepository.save(order);
                    orderNotifierService.sendNewOrder(order);
                }
                break;
            case IDLE:
                if (order.isPriority()){
                    oldOrderQueue = priorityIdleOrders;
                    newOrderQueue = priorityPreparingOrders;
                } else {
                    oldOrderQueue = regularIdleOrders;
                    newOrderQueue = regularPreparingOrders;
                }
                if (oldOrderQueue.remove(OrderEntry.fromOrderEntity(order))){
                    order.setOrderStatus(OrderStatus.PREPARING);
                    result = newOrderQueue.add(OrderEntry.fromOrderEntity(order));
                    if (result){
                        orderRepository.save(order);
                        orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.PREPARING);
                    }
                } else {
                    logger.info("Order not found in queue {}", order);
                    logger.info("regularIdleOrderQueue {} {}", regularIdleOrders, regularIdleOrders.size());
                    oldOrderQueue.forEach(o -> logger.info("Order in queue {} {}", o, order.equals(o)));

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
                if (oldOrderQueue.remove(OrderEntry.fromOrderEntity(order))){
                    order.setOrderStatus(OrderStatus.READY);
                    result = newOrderQueue.add(OrderEntry.fromOrderEntity(order));
                    if (result){
                        orderRepository.save(order);
                        orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.READY);
                    }
                }
                break;
            case READY:
                oldOrderQueue = order.isPriority() ? priorityReadyOrders : regularReadyOrders;
                result = oldOrderQueue.remove(OrderEntry.fromOrderEntity(order));
                if (result){
                    order.setOrderStatus(OrderStatus.PICKED_UP);
                    orderNotifierService.sendOrderStatusUpdates(order.getId(), OrderStatus.PICKED_UP);
                }
                break;
            default:
                logger.info("Invalid Order Status {}", order.getOrderStatus());
        }
        return result;
    }

    public QueueOrdersDTO getAllIdleOrders(){
        return new QueueOrdersDTO(
                convertToOrderList(regularIdleOrders),
                convertToOrderList(priorityIdleOrders),
                convertToOrderList(regularPreparingOrders),
                convertToOrderList(priorityPreparingOrders),
                convertToOrderList(regularReadyOrders),
                convertToOrderList(priorityReadyOrders)
        );
    }

    private List<OrderCookResponseDTO> convertToOrderList(Queue<OrderEntry> orderQueue){
        return orderQueue.stream()
                .map(orderEntry -> orderRepository.findById(orderEntry.getId()))
                .filter(Optional::isPresent)
                .map(orderOpt -> OrderCookResponseDTO.fromOrderEntity(orderOpt.get()))
                .toList();
    }

}
