package pt.ua.deti.springcanteen.dto.response.cookresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCookResponseDTO {
    private long id;
    private boolean isPriority;
    private Set<OrderMenuCookResponseDTO> orderMenus;

    public static OrderCookResponseDTO fromOrderEntity(Order order){
        return new OrderCookResponseDTO(
                order.getId(),
                order.isPriority(),
                order.getOrderMenus().stream().map(OrderMenuCookResponseDTO::fromOrderMenuEntity).collect(Collectors.toSet())
        );
    }
}
