package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.dto.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.List;
import java.util.Queue;

@Data
@AllArgsConstructor
public class QueueOrdersDTO {

    private List<OrderCookResponseDTO> regularOrders;
    private List<OrderCookResponseDTO> priorityOrders;


}
