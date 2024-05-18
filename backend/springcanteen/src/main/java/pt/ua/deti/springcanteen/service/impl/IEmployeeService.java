package pt.ua.deti.springcanteen.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.repositories.EmployeeRepository;
import pt.ua.deti.springcanteen.service.EmployeeService;

@Service
@AllArgsConstructor
public class IEmployeeService implements EmployeeService {

    private EmployeeRepository employeeRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            // username -> our email
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return employeeRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User by that email not found"));
            }
        };
    }
}
