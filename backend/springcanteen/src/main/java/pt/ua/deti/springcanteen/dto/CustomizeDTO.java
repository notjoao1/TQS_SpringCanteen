package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeDTO {
    @Valid
    @NotNull
    private CustomizeDrinkDTO customizedDrink;

    @Valid
    @NotNull
    private CustomizeMainDishDTO customizedMainDish;
}
