package pt.ua.deti.springcanteen.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "drinks")
@PrimaryKeyJoinColumn(name = "item_id")
public class Drink extends Item{
}
