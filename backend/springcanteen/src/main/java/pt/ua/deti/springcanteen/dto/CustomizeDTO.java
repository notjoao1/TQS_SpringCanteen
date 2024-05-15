package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeDTO {

    // Drinks can be null depending on the menu
    @Valid
    private CustomizeDrinkDTO customized_drink;
    @Valid
    @NotNull
    private CustomizeMainDishDTO customized_main_dish;


}
