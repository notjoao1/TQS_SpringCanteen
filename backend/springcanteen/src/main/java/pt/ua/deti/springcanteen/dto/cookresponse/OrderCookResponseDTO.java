package pt.ua.deti.springcanteen.dto.cookresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.service.impl.IPriceService;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
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
