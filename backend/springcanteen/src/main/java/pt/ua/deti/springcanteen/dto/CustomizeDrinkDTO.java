package pt.ua.deti.springcanteen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeDrinkDTO {

  @NotNull private Long itemId;
}
