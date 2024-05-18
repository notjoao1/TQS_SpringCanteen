package pt.ua.deti.springcanteen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Set;

@Data
public class CustomizeOrderDTO {

    @NotNull
    private Long kioskId;
    @NotNull
    private Boolean isPaid;
    @NotNull
    private Boolean isPriority;
    @NotNull
    @Pattern(regexp = "^\\d{9}$")
    private String nif;
    @Valid
    private Set<OrderMenuDTO> orderMenus;
}
