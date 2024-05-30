package pt.ua.deti.springcanteen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ua.deti.springcanteen.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {}
