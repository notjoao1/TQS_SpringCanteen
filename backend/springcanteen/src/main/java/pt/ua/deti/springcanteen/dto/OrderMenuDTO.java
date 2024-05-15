package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.OrderMenu;

@Data
public class OrderMenuDTO {

    @NotNull
    private Long menu_id;
    @Valid
    @NotNull
    private CustomizeDTO customization;

}
