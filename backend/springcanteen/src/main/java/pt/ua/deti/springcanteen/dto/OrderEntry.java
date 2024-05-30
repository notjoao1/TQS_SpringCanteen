package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class OrderEntry {
  Long id;
  OrderStatus orderStatus;

  public static OrderEntry fromOrderEntity(Order order) {
    return new OrderEntry(order.getId(), order.getOrderStatus());
  }
}
