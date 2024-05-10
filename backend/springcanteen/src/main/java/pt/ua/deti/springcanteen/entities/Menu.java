package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "menus")
@AllArgsConstructor
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private float price;

    @OneToMany(mappedBy = "menu")
    private Set<OrderMenu> menuOrders;

    @ManyToMany
    @JoinTable(
        name = "menu_drinks",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Drink> drinkOptions;

    @ManyToMany
    @JoinTable(
            name = "menu_main_dishes",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<MainDish> mainDishOptions;

}
