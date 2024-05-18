package pt.ua.deti.springcanteen.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface EmployeeService {
    // security related
    UserDetailsService userDetailsService();
}
