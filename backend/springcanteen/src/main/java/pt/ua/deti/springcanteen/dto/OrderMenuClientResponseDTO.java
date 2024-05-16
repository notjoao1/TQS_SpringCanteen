package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
@AllArgsConstructor
public class OrderMenuClientResponseDTO {
    private MenuClientResponseDTO menu;
    private String customization;

    public static OrderMenuClientResponseDTO fromOrderMenuEntity(OrderMenu orderMenu){
        return new OrderMenuClientResponseDTO(
                MenuClientResponseDTO.fromMenuEntity(orderMenu.getMenu(), 0), orderMenu.getCustomization()
        );
    }
}
