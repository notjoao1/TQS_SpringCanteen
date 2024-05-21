package pt.ua.deti.springcanteen.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.clientresponse.OrderClientResponseDTO;
import pt.ua.deti.springcanteen.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @PostMapping("")
    public ResponseEntity<OrderClientResponseDTO> createOrder(@Valid @RequestBody CustomizeOrderDTO customizeOrderDTO) {
        logger.info("POST /api/orders - create order");

        return orderService.createOrder(customizeOrderDTO)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(OrderClientResponseDTO.fromOrderEntity(order)))
                .orElse(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());
    }
}
