package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Setter
@Getter
@Table(name = "order_menus")
@AllArgsConstructor
@NoArgsConstructor
public class OrderMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @NotNull
    private float calculatedPrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private String customization;

    public OrderMenu(Order order, Menu menu, String customization) {
        this.order = order;
        this.menu = menu;
        this.customization = customization;
    }
}
