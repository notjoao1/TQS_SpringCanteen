package pt.ua.deti.springcanteen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.deti.springcanteen.entities.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
}
