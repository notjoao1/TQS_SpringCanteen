package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.ua.deti.springcanteen.entities.OrderStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderUpdateResponseDTO {
  private Long orderId;
  private OrderStatus newOrderStatus;
  private boolean isPriority;
}
