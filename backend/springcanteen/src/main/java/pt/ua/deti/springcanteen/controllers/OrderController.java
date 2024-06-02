package pt.ua.deti.springcanteen.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.response.clientresponse.OrderClientResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.service.OrderService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
  private final OrderService orderService;

  @Operation(summary = "Create a new order")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Create a new order given a requested list of menus and their customization,"
                    + " including selected main dish, drink and customized ingredients.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderClientResponseDTO.class))
            }),
        @ApiResponse(
            responseCode = "422",
            description =
                "The order is invalid - invalid menus, options chosen, or ingredients in main"
                    + " dish.",
            content = @Content)
      })
  @PostMapping("")
  public ResponseEntity<OrderClientResponseDTO> createOrder(
      @Valid @RequestBody CustomizeOrderDTO customizeOrderDTO) {
    logger.info("POST /api/orders - create order");

    return orderService
        .createOrder(customizeOrderDTO)
        .map(
            order ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(OrderClientResponseDTO.fromOrderEntity(order)))
        .orElse(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());
  }

  @Operation(summary = "Confirm the payment of an order")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Order payment confirmed."),
        @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content)
      })
  @PutMapping("/{id}")
  @Secured("DESK_PAYMENTS")
  public ResponseEntity<Void> payNotPaidOrder(@PathVariable Long id) {
    logger.info("PUT /api/orders/{id} - pay not paid order");
    Optional<Order> orderOpt = orderService.changeNotPaidOrderToNextOrderStatus(id);
    if (orderOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "List not paid orders")
  @ApiResponse(
      responseCode = "200",
      description = "List of all orders that have not been paid yet.",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = OrderClientResponseDTO.class))
      })
  @GetMapping("/notpaid")
  @Secured("DESK_PAYMENTS")
  public ResponseEntity<List<OrderClientResponseDTO>> getNotPaidOrders() {
    logger.info("GET /api/orders/notpaid - get not paid orders");
    return ResponseEntity.ok(
        orderService.getNotPaidOrders().stream()
            .map(OrderClientResponseDTO::fromOrderEntity)
            .toList());
  }
}
