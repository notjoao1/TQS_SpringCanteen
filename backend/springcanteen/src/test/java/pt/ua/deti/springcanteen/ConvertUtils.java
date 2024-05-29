package pt.ua.deti.springcanteen;

import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.Comparator;
import java.util.List;

public class ConvertUtils {

    public static List<String> getMenuNamesFromDTO(OrderCookResponseDTO orderCookResponseDTO) {
        return orderCookResponseDTO.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }

    public static List<Float> getMenuPricesFromDTO(OrderCookResponseDTO orderCookResponseDTO) {
        return orderCookResponseDTO.getOrderMenus().stream()
                .sorted(Comparator.comparing(t0 -> t0.getMenu().getName()))
                .map(orderMenu -> orderMenu.getMenu().getPrice())
                .toList();
    }

    public static List<String> getMenuNamesFromOrder(Order order) {
        return order.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }
}
