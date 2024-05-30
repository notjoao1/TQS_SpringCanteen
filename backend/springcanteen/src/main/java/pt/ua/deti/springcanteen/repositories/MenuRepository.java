package pt.ua.deti.springcanteen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ua.deti.springcanteen.entities.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {}
