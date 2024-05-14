package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "main_dishes")
@NoArgsConstructor
public class MainDish extends Item {

    @OneToMany(mappedBy = "mainDish", fetch = FetchType.EAGER)
    private Set<MainDishIngredients> mainDishIngredients;

}
