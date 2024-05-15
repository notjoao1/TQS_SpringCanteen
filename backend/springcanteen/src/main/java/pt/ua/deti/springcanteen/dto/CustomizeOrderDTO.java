package pt.ua.deti.springcanteen.dto;

import com.google.gson.Gson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pt.ua.deti.springcanteen.entities.*;

import java.util.HashSet;
import java.util.Set;

@Data
public class CustomizeOrderDTO {

    @NotNull
    private Long kiosk_id;
    @NotNull
    private Boolean isPaid;
    @NotNull
    private Boolean isPriority;
    @NotNull
    @Pattern(regexp = "^\\d{9}$")
    private String nif;
    @Valid
    private Set<OrderMenuDTO> orderMenus;

    public Order toOrderEntity(){
        KioskTerminal kioskTerminal = new KioskTerminal();
        kioskTerminal.setId(this.kiosk_id);
        Order order = new Order(OrderStatus.IDLE, this.isPaid, this.isPriority, this.nif, kioskTerminal);
        Set<OrderMenu> orderMenus = new HashSet<>();
        for (OrderMenuDTO orderMenuDTO : this.orderMenus) {
            Menu menu = new Menu();
            menu.setId(orderMenuDTO.getMenu_id());
            orderMenus.add(new OrderMenu(order, menu, new Gson().toJson(orderMenuDTO.getCustomization())));
        }
        order.setOrderMenus(orderMenus);
        return order;
    }
}
