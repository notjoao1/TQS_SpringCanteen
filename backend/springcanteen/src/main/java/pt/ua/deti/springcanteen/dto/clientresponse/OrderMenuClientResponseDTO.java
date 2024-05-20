package pt.ua.deti.springcanteen.dto.clientresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.dto.cookresponse.MenuOrderResponseDTO;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
@AllArgsConstructor
public class OrderMenuClientResponseDTO {
    private MenuOrderResponseDTO menu;
    private String customization;

    public static OrderMenuClientResponseDTO fromOrderMenuEntity(OrderMenu orderMenu){
        return new OrderMenuClientResponseDTO(
                MenuOrderResponseDTO.fromMenuEntity(orderMenu.getMenu(), orderMenu.getCalculatedPrice()), orderMenu.getCustomization()
        );
    }
}
