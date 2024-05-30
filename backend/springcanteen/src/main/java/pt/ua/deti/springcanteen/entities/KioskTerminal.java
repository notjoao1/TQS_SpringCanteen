package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "kiosk_terminals")
@NoArgsConstructor
public class KioskTerminal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "kioskTerminal")
  private Set<Order> orders = Set.of();
}
