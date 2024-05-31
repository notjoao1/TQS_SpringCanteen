package pt.ua.deti.springcanteen.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.service.OrderService;

@Controller
@AllArgsConstructor
public class OrderUpdatesController {
  private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesController.class);
  private OrderService orderService;

  @MessageMapping("/order_updates")
  public void receiveOrderUpdates(OrderUpdateRequestDTO orderUpdateRequest) {
    logger.info(
        "STOMP MESSAGE RECEIVED AT /order_updates: Update order with body - {}",
        orderUpdateRequest);
    orderService.changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
  }
}
