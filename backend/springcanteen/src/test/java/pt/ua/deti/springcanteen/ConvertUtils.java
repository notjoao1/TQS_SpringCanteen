package pt.ua.deti.springcanteen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.List;

public class ConvertUtils {

    public static List<String> getMenuNamesFromDTO(OrderCookResponseDTO orderCookResponseDTO) {
        return orderCookResponseDTO.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }

    public static List<Float> getMenuPricesFromDTO(OrderCookResponseDTO orderCookResponseDTO) {
        return orderCookResponseDTO.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getPrice()).sorted().toList();
    }

    public static List<String> getMenuNamesFromOrder(Order order) {
        return order.getOrderMenus().stream().map(orderMenu -> orderMenu.getMenu().getName()).sorted().toList();
    }
}
