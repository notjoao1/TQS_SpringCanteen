package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class OrderClientResponseDTO {
    private long id;
    private boolean isPaid;
    private boolean isPriority;
    private String nif;
    private Set<OrderMenuClientResponseDTO> orderMenus;
    private float price;

    public static OrderClientResponseDTO fromOrderEntity(Order order){
        return new OrderClientResponseDTO(
                order.getId(), order.isPaid(), order.isPriority(), order.getNif(),
                order.getOrderMenus().stream().map(OrderMenuClientResponseDTO::fromOrderMenuEntity).collect(Collectors.toSet()),
                order.getPrice()
        );
    }

}
