package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_ingredients")
@AllArgsConstructor
@NoArgsConstructor
public class MainDishIngredients {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private MainDish mainDish;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @NotNull
    private int quantity;

}
