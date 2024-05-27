package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.List;

public class Utils {
    public static List<String> getMenuNamesFromDTO(OrderCookResponseDTO orderCookResponseDTO) {
        return orderCookResponseDTO.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }

    public static List<String> getMenuNamesFromOrder(Order order) {
        return order.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }
}
