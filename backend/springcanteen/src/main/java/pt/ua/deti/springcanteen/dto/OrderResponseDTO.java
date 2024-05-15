package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private long id;
    private boolean isPaid;
    private boolean isPriority;
    private String nif;
    private Set<OrderMenuResponseDTO> orderMenus;
    private float price;

    public static OrderResponseDTO fromOrderEntityWithClientLevelDetails(Order order){
        return new OrderResponseDTO(
                order.getId(), order.isPaid(), order.isPriority(), order.getNif(),
                order.getOrderMenus().stream().map(OrderMenuResponseDTO::fromOrderMenuEntityWithClientLevelDetails).collect(Collectors.toSet()),
                order.getPrice()
        );
    }

}
