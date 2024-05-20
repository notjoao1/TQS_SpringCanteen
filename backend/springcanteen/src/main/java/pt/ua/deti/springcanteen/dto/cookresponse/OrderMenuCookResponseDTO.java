package pt.ua.deti.springcanteen.dto.cookresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.dto.clientresponse.MenuClientResponseDTO;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
@AllArgsConstructor
public class OrderMenuCookResponseDTO {
    private MenuClientResponseDTO menu;
    private String customization;

    public static OrderMenuCookResponseDTO fromOrderMenuEntity(OrderMenu orderMenu){
        return new OrderMenuCookResponseDTO(
                MenuClientResponseDTO.fromMenuEntity(orderMenu.getMenu(), orderMenu.getCalculatedPrice()), orderMenu.getCustomization()
        );
    }
}
