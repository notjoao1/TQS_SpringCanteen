package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
@AllArgsConstructor
public class OrderMenuResponseDTO {
    private MenuResponseDTO menu;
    private String customization;

    public static OrderMenuResponseDTO fromOrderMenuEntityWithClientLevelDetails(OrderMenu orderMenu){
        return new OrderMenuResponseDTO(
                MenuResponseDTO.fromMenuEntityWithClientLevelDetails(orderMenu.getMenu()), orderMenu.getCustomization()
        );
    }
}
