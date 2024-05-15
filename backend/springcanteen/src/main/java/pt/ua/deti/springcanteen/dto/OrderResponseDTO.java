package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;

import java.util.Set;

@AllArgsConstructor
public class OrderResponseDTO {
    private long id;
    private boolean isPaid;
    private boolean isPriority;
    private String nif;
    private Set<OrderMenu> orderMenus;
    private float price;

    public static OrderResponseDTO fromOrderEntityWithClientLevelDetails(Order order){
        return new OrderResponseDTO(
                order.getId(), order.isPaid(), order.isPriority(),
                order.getNif(), order.getOrderMenus(), order.getPrice()
        );
    }

}
