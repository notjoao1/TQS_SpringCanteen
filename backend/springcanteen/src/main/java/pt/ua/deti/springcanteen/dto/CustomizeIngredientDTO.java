package pt.ua.deti.springcanteen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeIngredientDTO {

    @NotNull
    private Long ingredientId;
    
    @NotNull
    private Integer quantity;

}
