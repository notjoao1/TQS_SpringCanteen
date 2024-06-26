package pt.ua.deti.springcanteen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInRequestDTO {
  @NotNull private String email;
  @NotNull private String password;
}
