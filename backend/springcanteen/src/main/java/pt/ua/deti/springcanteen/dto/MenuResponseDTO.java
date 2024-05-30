package pt.ua.deti.springcanteen.dto;

import lombok.Data;
import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.MainDish;

import java.util.Set;

@Data
public class MenuResponseDTO {
  private Long id;
  private String name;
  private float price;
  private String imageLink;
  private Set<Drink> drinkOptions;
  private Set<MainDish> mainDishOptions;
}
