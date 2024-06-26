package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderMenuDTO {

  @NotNull private Long menuId;
  @Valid @NotNull private CustomizeDTO customization;
}
