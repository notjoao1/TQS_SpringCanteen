package pt.ua.deti.springcanteen.dto.response.clientresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.dto.response.cookresponse.MenuCookResponseDTO;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
@AllArgsConstructor
public class OrderMenuClientResponseDTO {
    private MenuCookResponseDTO menu;
    private String customization;

    public static OrderMenuClientResponseDTO fromOrderMenuEntity(OrderMenu orderMenu){
        return new OrderMenuClientResponseDTO(
                MenuCookResponseDTO.fromMenuEntity(orderMenu.getMenu(), orderMenu.getCalculatedPrice()), orderMenu.getCustomization()
        );
    }
}
