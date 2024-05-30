package pt.ua.deti.springcanteen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.EmployeeRole;

@Data
@AllArgsConstructor
public class SignUpRequestDTO {
  @NotNull private String username;
  @NotNull private String email;
  @NotNull private String password;
  @NotNull private EmployeeRole role;
}
