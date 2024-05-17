package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CustomizeMainDishDTO {

    @NotNull
    private Long item_id;
    @Valid
    private Set<CustomizeIngredientDTO> customized_ingredients;

}
