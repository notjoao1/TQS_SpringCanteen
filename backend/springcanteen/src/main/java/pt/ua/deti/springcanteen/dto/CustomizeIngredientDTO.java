package pt.ua.deti.springcanteen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeIngredientDTO {

    @NotNull
    private Long ingredient_id;
    @NotNull
    private Integer quantity;

}
