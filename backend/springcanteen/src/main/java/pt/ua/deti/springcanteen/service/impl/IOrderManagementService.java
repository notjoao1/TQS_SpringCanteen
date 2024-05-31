package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
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

  @Override
  public boolean addNewIdleOrder(Order order) {
    boolean result;
    Queue<OrderEntry> newOrderQueue = order.isPriority() ? priorityIdleOrders : regularIdleOrders;
    if (order.getOrderStatus() != OrderStatus.IDLE) {
      logger.info("Order status must be IDLE. Not {}", order.getOrderStatus());
      return false;
    }
    result = newOrderQueue.offer(OrderEntry.fromOrderEntity(order));
    if (result) {
      orderNotifierService.sendNewOrder(order);
    }
    return result;
  }

  @Override
  public boolean manageNotPaidOrder(Order order) {
    boolean result;
    Queue<OrderEntry> newOrderQueue = order.isPriority() ? priorityIdleOrders : regularIdleOrders;
    order.setOrderStatus(OrderStatus.IDLE);
    result = newOrderQueue.offer(OrderEntry.fromOrderEntity(order));
    logger.info("newOrderQueue = regularIdleOrders {}", newOrderQueue == regularIdleOrders);
    if (result) {
      logger.info("order status {}", order.getOrderStatus());
      logger.info("newOrderQueue = regularIdleOrders {}", newOrderQueue == regularIdleOrders);
      logger.info("newOrderQueue {}", newOrderQueue);
      logger.info("regularIdleOrders {}", regularIdleOrders);
      orderRepository.save(order);
      orderNotifierService.sendNewOrder(order);
    }
    return result;
  }

  @Override
  public boolean manageIdleOrder(Order order) {
    boolean result = false;
    Queue<OrderEntry> oldOrderQueue;
    Queue<OrderEntry> newOrderQueue;
    if (order.isPriority()) {
      oldOrderQueue = priorityIdleOrders;
      newOrderQueue = priorityPreparingOrders;
    } else {
      oldOrderQueue = regularIdleOrders;
      newOrderQueue = regularPreparingOrders;
    }
    OrderEntry oldOrderEntry = OrderEntry.fromOrderEntity(order);
    if (oldOrderQueue.contains(oldOrderEntry)) {
      order.setOrderStatus(OrderStatus.PREPARING);
      result = newOrderQueue.offer(OrderEntry.fromOrderEntity(order));
      if (result) {
        oldOrderQueue.remove(oldOrderEntry);
        orderRepository.save(order);
        orderNotifierService.sendOrderStatusUpdates(
            order.getId(), OrderStatus.PREPARING, order.isPriority());
      }
    } else {
      logger.info("Order not found in queue {}", order);
    }
    return result;
  }

  @Override
  public boolean managePreparingOrder(Order order) {
    boolean result = false;
    Queue<OrderEntry> oldOrderQueue;
    Queue<OrderEntry> newOrderQueue;
    if (order.isPriority()) {
      oldOrderQueue = priorityPreparingOrders;
      newOrderQueue = priorityReadyOrders;
    } else {
      oldOrderQueue = regularPreparingOrders;
      newOrderQueue = regularReadyOrders;
    }
    OrderEntry oldOrderEntry = OrderEntry.fromOrderEntity(order);
    if (oldOrderQueue.contains(oldOrderEntry)) {
      order.setOrderStatus(OrderStatus.READY);
      result = newOrderQueue.offer(OrderEntry.fromOrderEntity(order));
      if (result) {
        oldOrderQueue.remove(OrderEntry.fromOrderEntity(order));
        orderRepository.save(order);
        orderNotifierService.sendOrderStatusUpdates(
            order.getId(), OrderStatus.READY, order.isPriority());
      }
    } else {
      logger.info("Order not found in queue {}", order);
    }
    return result;
  }

  @Override
  public boolean manageReadyOrder(Order order) {
    Queue<OrderEntry> oldOrderQueue = order.isPriority() ? priorityReadyOrders : regularReadyOrders;
    boolean result = oldOrderQueue.remove(OrderEntry.fromOrderEntity(order));
    if (result) {
      order.setOrderStatus(OrderStatus.PICKED_UP);
      orderRepository.save(order);
      orderNotifierService.sendOrderStatusUpdates(
          order.getId(), OrderStatus.PICKED_UP, order.isPriority());
    }
    return result;
  }

  @Override
  public QueueOrdersDTO getAllOrders() {
    return new QueueOrdersDTO(
        convertToOrderList(regularIdleOrders),
        convertToOrderList(priorityIdleOrders),
        convertToOrderList(regularPreparingOrders),
        convertToOrderList(priorityPreparingOrders),
        convertToOrderList(regularReadyOrders),
        convertToOrderList(priorityReadyOrders));
  }

  private List<OrderCookResponseDTO> convertToOrderList(Queue<OrderEntry> orderQueue) {
    return orderQueue.stream()
        .map(orderEntry -> orderRepository.findById(orderEntry.getId()))
        .filter(Optional::isPresent)
        .map(orderOpt -> OrderCookResponseDTO.fromOrderEntity(orderOpt.get()))
        .toList();
  }
}
