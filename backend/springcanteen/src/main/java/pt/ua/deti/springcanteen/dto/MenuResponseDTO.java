package pt.ua.deti.springcanteen.dto;

import java.util.Set;

import lombok.Data;
import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.MainDish;

@Data
public class MenuResponseDTO {
    private Long id;
    private String name;
    private float price;
    private Set<Drink> drinkOptions;
    private Set<MainDish> mainDishOptions;
}
