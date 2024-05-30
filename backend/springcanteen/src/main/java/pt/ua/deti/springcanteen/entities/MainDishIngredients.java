package pt.ua.deti.springcanteen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "maindish_ingredients")
@AllArgsConstructor
@NoArgsConstructor
public class MainDishIngredients {

  @Id private Long id;

  @ManyToOne
  @JoinColumn(name = "item_id")
  @JsonIgnore
  private MainDish mainDish;

  @ManyToOne
  @JoinColumn(name = "ingredient_id")
  private Ingredient ingredient;

  @NotNull private int quantity;
}
