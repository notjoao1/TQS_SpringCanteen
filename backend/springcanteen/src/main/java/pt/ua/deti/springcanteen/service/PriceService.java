package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.entities.OrderMenu;

public interface PriceService {
    float getOrderMenuPrice(OrderMenu orderMenu);
}
