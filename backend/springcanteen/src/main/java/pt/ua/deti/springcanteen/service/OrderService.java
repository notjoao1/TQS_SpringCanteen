package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.dto.OrderMenuDTO;
import pt.ua.deti.springcanteen.entities.Order;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderService {
    Optional<Order> createOrder(CustomizeOrderDTO customizeOrderDTO);
}
