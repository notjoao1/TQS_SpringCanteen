package pt.ua.deti.springcanteen.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.MainDish;
import pt.ua.deti.springcanteen.entities.Menu;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuResponseDTO {
    private Long id;
    private String name;
    private float price;
    private String imageLink;
    private Set<Drink> drinkOptions;
    private Set<MainDish> mainDishOptions;

    public MenuResponseDTO(String name, float price) {
        this.name = name;
        this.price = price;
    }

    public static MenuResponseDTO fromMenuEntityWithClientLevelDetails(Menu menu) {
        return new MenuResponseDTO(menu.getName(), menu.getPrice());
    }
}
