package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CustomizeMainDishDTO {

  @NotNull private Long itemId;

  @NotNull @Valid private Set<CustomizeIngredientDTO> customizedIngredients;
}
