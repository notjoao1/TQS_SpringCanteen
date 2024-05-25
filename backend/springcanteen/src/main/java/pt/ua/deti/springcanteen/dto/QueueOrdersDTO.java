package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;

import java.util.List;

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
