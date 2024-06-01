package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
  Optional<Order> createOrder(CustomizeOrderDTO customizeOrderDTO);

  Optional<Order> changePaidOrderToNextOrderStatus(Long orderId);

  Optional<Order> changeNotPaidOrderToNextOrderStatus(Long orderId);

  List<Order> getNotPaidOrders();

}
