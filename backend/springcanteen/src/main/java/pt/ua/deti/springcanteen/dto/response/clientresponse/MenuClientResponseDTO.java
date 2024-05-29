package pt.ua.deti.springcanteen.dto.response.clientresponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Menu;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuClientResponseDTO {
    private String name;
    private float price;

    public static MenuClientResponseDTO fromMenuEntity(Menu menu, float price) {
        return new MenuClientResponseDTO(menu.getName(), price);
    }
}
