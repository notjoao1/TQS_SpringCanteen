package pt.ua.deti.springcanteen.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import pt.ua.deti.springcanteen.entities.Employee;

import java.util.Optional;

public interface EmployeeService {
    // security related
    UserDetailsService userDetailsService();

    Optional<Employee> getEmployeeByEmail(String email);
}
