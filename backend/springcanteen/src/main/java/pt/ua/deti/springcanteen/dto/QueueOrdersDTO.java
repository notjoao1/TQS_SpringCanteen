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

    private List<OrderCookResponseDTO> regularIdleOrders;
    private List<OrderCookResponseDTO> priorityIdleOrders;
    private List<OrderCookResponseDTO> regularPreparingOrders;
    private List<OrderCookResponseDTO> priorityPreparingOrders;
    private List<OrderCookResponseDTO> regularReadyOrders;
    private List<OrderCookResponseDTO> priorityReadyOrders;



}
