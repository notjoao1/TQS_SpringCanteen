package pt.ua.deti.springcanteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ua.deti.springcanteen.entities.Employee;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private String userRole;

    public static JwtAuthenticationResponseDTO fromEmployeeEntityAndTokens(Employee employee, String token, String refreshToken) {
        return new JwtAuthenticationResponseDTO(
                employee.getId(), employee.getName(), employee.getEmail(),
                token, refreshToken, employee.getRole().toString()
        );
    }
}
