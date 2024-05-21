package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.Queue;

@Data
@AllArgsConstructor
public class QueueOrdersDTO {

    private Queue<Order> regularOrders;
    private Queue<Order> priorityOrders;


}
