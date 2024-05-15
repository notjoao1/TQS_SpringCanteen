package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "main_dishes")
@NoArgsConstructor
@AllArgsConstructor
public class MainDish extends Item {

    @OneToMany(mappedBy = "mainDish")
    private Set<MainDishIngredients> mainDishIngredients;

}
