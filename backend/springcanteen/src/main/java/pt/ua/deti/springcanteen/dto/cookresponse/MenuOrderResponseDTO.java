package pt.ua.deti.springcanteen.dto.cookresponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Menu;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuOrderResponseDTO {
    private String name;
    private float price;

    public static MenuOrderResponseDTO fromMenuEntity(Menu menu, float price) {
        return new MenuOrderResponseDTO(menu.getName(), price);
    }
}
