package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "kiosk_terminals")
@AllArgsConstructor
@NoArgsConstructor
public class KioskTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "kioskTerminal")
    private Set<Order> orders;

}