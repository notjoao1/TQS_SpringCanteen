package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Setter
@Getter
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

    @NotNull
    private boolean isPriority;

    @NotNull
    private String nif;

    @ManyToOne
    @JoinColumn(name = "kiosk_id", nullable = false)
    private KioskTerminal kioskTerminal;

    @OneToMany(mappedBy = "order")
    private Set<OrderMenu> orderMenus;

}
