package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Set;

@Entity
@Setter
@Getter
@EqualsAndHashCode
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @NotNull
    private boolean isPaid;

    // calculated based on the customizations for each OrderMenu in this order
    @NotNull
    private float price;

    @NotNull
    private boolean isPriority;

    @NotNull
    private String nif;

    @ManyToOne
    @JoinColumn(name = "kiosk_id", nullable = false)
    private KioskTerminal kioskTerminal;

    @OneToMany(mappedBy = "order")
    @Fetch(FetchMode.JOIN)
    private Set<OrderMenu> orderMenus;

    public Order(OrderStatus orderStatus, boolean isPaid, boolean isPriority, String nif, KioskTerminal kioskTerminal) {
        this.orderStatus = orderStatus;
        this.isPaid = isPaid;
        this.isPriority = isPriority;
        this.nif = nif;
        this.kioskTerminal = kioskTerminal;
    }



}
