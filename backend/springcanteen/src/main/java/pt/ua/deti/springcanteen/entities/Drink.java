package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "drinks")
@NoArgsConstructor
public class Drink extends Item {
}
