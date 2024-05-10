package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "main_dishes")
@PrimaryKeyJoinColumn(name = "item_id")
public class MainDish extends Item{

    @OneToMany(mappedBy = "mainDish")
    private Set<MainDishIngredients> mainDishIngredients;

}
