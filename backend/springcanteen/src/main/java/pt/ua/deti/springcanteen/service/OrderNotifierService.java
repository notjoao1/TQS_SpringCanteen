package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;

public interface OrderNotifierService {
  void sendNewOrder(Order order);

  void sendOrderStatusUpdates(Long orderId, OrderStatus newOrderStatus, boolean isPriority);
}
