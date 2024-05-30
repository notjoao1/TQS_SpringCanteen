package pt.ua.deti.springcanteen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ua.deti.springcanteen.entities.KioskTerminal;

@Repository
public interface KioskTerminalRepository extends JpaRepository<KioskTerminal, Long> {}
