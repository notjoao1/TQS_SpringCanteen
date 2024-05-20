package pt.ua.deti.springcanteen.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.service.OrderService;

@Controller
@AllArgsConstructor
public class OrderUpdatesController {
    private OrderService orderService;
    
    @MessageMapping("/order_updates")
    public void receiveOrderUpdates(OrderUpdateRequestDTO orderUpdateRequest) {
        // FIXME: null here, gotta put the order that was updated based on the id on orderUpdateRequest
        orderService.changeOrderStatus(null, orderUpdateRequest.getNewOrderStatus());
    }
}
