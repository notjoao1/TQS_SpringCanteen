package pt.ua.deti.springcanteen.dto;

import lombok.Data;
import pt.ua.deti.springcanteen.entities.OrderStatus;

@Data
public class OrderUpdateResponseDTO {
    private Long orderId;
    private OrderStatus newOrderStatus; 
}
