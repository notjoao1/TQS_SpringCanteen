package pt.ua.deti.springcanteen.dto.response.cookresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Menu;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuCookResponseDTO {
  private String name;
  private float price;

  public static MenuCookResponseDTO fromMenuEntity(Menu menu, float price) {
    return new MenuCookResponseDTO(menu.getName(), price);
  }
}
