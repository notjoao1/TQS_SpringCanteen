package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.dto.OrderUpdateResponseDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.exceptions.InvalidStatusChangeException;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.QueueNotifierService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderUpdatesControllerIT {

  public static PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:latest")
          .withUsername("testname")
          .withPassword("testpassword")
          .withDatabaseName("sc_test");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.password", container::getPassword);
    registry.add("spring.datasource.username", container::getUsername);
  }

  public OrderUpdatesControllerIT() {
    container.start();
  }

  @LocalServerPort Integer port;

  private String websocketURL;

  @SpyBean OrderNotifierService orderNotifierServiceSpy;

  @SpyBean OrderService orderServiceSpy;

  @SpyBean OrderManagementService orderManagementServiceSpy;

  @SpyBean OrderUpdatesController orderUpdatesControllerSpy;

  @SpyBean QueueNotifierService queueNotifierService;

  WebSocketStompClient webSocketStompClient;
  StompSession stompSession;

  @Autowired OrderRepository orderRepository;

  @Autowired KioskTerminalRepository kioskTerminalRepository;

  List<Arguments> employeeOrderAndUpdateRequestAndToken;

  private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerIT.class);

  @BeforeAll
  void beforeAllSetup() {
    RestAssured.port = port;
    websocketURL = String.format("ws://localhost:%d/websocket", port);

    KioskTerminal kioskTerminal = kioskTerminalRepository.save(new KioskTerminal());
    Order testOrder = new Order();
    testOrder.setPriority(false);
    testOrder.setPaid(false);
    testOrder.setOrderStatus(OrderStatus.NOT_PAID);
    testOrder.setPrice(0.0f);
    testOrder.setNif("123456789");
    testOrder.setKioskTerminal(kioskTerminal);

    String signUpRequestTemplate =
        "{"
            + "    \"username\": \"%s\","
            + "    \"email\": \"%s\","
            + "    \"password\": \"%s\","
            + "    \"role\": \"%s\"}";
    List<Employee> employees =
        List.of(
          new Employee("desk_payments","desk_payments@gmail.com","desk_payments_password",EmployeeRole.DESK_PAYMENTS),
          new Employee("cook", "mockcook@gmail.com", "cook_password", EmployeeRole.COOK),
          new Employee("desk_orders","desk_orders@gmail.com","desk_orders_password",EmployeeRole.DESK_ORDERS));
    employeeOrderAndUpdateRequestAndToken =
        employees.stream()
            .map(
                employee -> {
                  String token =
                      RestAssured.given()
                          .contentType(ContentType.JSON)
                          .body(
                              String.format(
                                  signUpRequestTemplate,
                                  employee.getUsername(),
                                  employee.getEmail(),
                                  employee.getPassword(),
                                  employee.getRole().name()))
                          .when()
                          .post("api/auth/signup")
                          .then()
                          .statusCode(HttpStatus.SC_CREATED)
                          .body("$", hasKey("token"))
                          .extract()
                          .path("token");
                  Order newOrder =
                      orderRepository.save(
                          new Order(
                              testOrder.getOrderStatus(),
                              testOrder.isPaid(),
                              testOrder.isPriority(),
                              testOrder.getNif(),
                              testOrder.getKioskTerminal()));
                  OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
                  orderUpdateRequest.setOrderId(newOrder.getId());
                  return Arguments.of(newOrder, orderUpdateRequest, token, employee.getRole());
                })
            .toList();
  }

  @BeforeEach
  void setup() {
    webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
    webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
  }

  @AfterEach
  void teardown() {
    if (stompSession != null) {
      logger.info("disconnecting");
      stompSession.disconnect();
    }
  }

  @AfterAll
  void afterAll() {
    container.stop();
  }

  private Stream<Arguments> provideAllTokenHeaders() {
    return employeeOrderAndUpdateRequestAndToken.stream()
        .map(
            arguments -> {
              Object[] argumentsObjects = arguments.get();
              Order order = (Order) argumentsObjects[0];
              OrderUpdateRequestDTO orderUpdateRequest =
                  (OrderUpdateRequestDTO) argumentsObjects[1];
              String token = (String) argumentsObjects[2];
              EmployeeRole employeeRole = (EmployeeRole) argumentsObjects[3];
              StompHeaders handshakeHeaders = new StompHeaders();
              handshakeHeaders.set("Authorization", "Bearer " + token);
              return Arguments.of(order, orderUpdateRequest, handshakeHeaders, employeeRole);
            });
  }

  private class CustomStompFrameHandler implements StompFrameHandler {

    private CompletableFuture<OrderUpdateResponseDTO> futureMessage;

    public CustomStompFrameHandler(CompletableFuture<OrderUpdateResponseDTO> futureMessage) {
      this.futureMessage = futureMessage;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      logger.info("getPayloadType");
      return OrderUpdateResponseDTO.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      logger.info("converting payload to OrderUpdateResponseDTO");
      futureMessage.complete((OrderUpdateResponseDTO) payload);
    }
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(1)
  void whenAuthenticatedConnect_thenMessagesReceived(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole)
      throws InterruptedException, ExecutionException, TimeoutException {
    // act
    stompSession =
        connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);
    CompletableFuture<OrderUpdateResponseDTO> futureMessage = new CompletableFuture<>();

    stompSession.subscribe("/topic/orders", new CustomStompFrameHandler(futureMessage));
    Awaitility.await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              verify(queueNotifierService, times(1)).sendExistingOrderQueues(any());
            });

    orderNotifierServiceSpy.sendOrderStatusUpdates(1L, OrderStatus.PREPARING, false);

    OrderUpdateResponseDTO receivedOrderUpdateResponseDTO = futureMessage.get(10, TimeUnit.SECONDS);
    assertThat(receivedOrderUpdateResponseDTO)
      .isNotNull()
      .extracting(OrderUpdateResponseDTO::getOrderId, OrderUpdateResponseDTO::getNewOrderStatus, OrderUpdateResponseDTO::isPriority)
      .containsExactly(1L, OrderStatus.PREPARING, false);
    assertNotNull(receivedOrderUpdateResponseDTO);
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(2)
  void whenReceiveUpdateOrder_FromNOTPAID_toIDLE_thenInvalidStatusChangeException(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);
    stompSession =
        connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

    // act
    stompSession.send("/app/order_updates", orderUpdateRequest);

    // wait until message received
    Awaitility.await()
      .atMost(2, TimeUnit.SECONDS)
      .untilAsserted(
        () -> {
          verify(orderUpdatesControllerSpy, times(1)).handleException(any(InvalidStatusChangeException.class));
        });

    // assert
    verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
    verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderManagementServiceSpy, times(0)).manageOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
  }
  @ParameterizedTest
  @EnumSource(value = OrderStatus.class, names = {"IDLE", "PREPARING", "READY", "PICKED_UP"})
  @org.junit.jupiter.api.Order(4)
  void whenPayOrderWithInvalidStatus_thenReturn400W_forDeskPayments( OrderStatus orderStatus ) {
    // setup
    Object[] argumentsObjects = provideAllTokenHeaders().findFirst().orElseThrow().get();

    int statusCode = RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + argumentsObjects[2])
      .when()
        .put(String.format("api/orders/%d", testOrder.getId()))
      .then()
        .statusCode(employeeRole == EmployeeRole.DESK_PAYMENTS ? HttpStatus.SC_NO_CONTENT : HttpStatus.SC_FORBIDDEN)
        .extract().statusCode();

    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
    if (statusCode == HttpStatus.SC_FORBIDDEN) {
      verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
      verify(orderManagementServiceSpy, times(0)).manageOrder(any());
      verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    } else {
      verify(orderServiceSpy, times(1)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
      verify(orderManagementServiceSpy, times(1)).manageOrder(any());
      verify(orderNotifierServiceSpy, times(1)).sendNewOrder(
        argThat((Order order) -> order.getId().equals(testOrder.getId()))
      );
    }
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(4)
  void whenPayNotPaidOrder_WithStatusNotPaid_thenReturn204_OnlyForDeskPayments_elseReturn403(
    Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);

    int statusCode = RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", userHandshakeHeaders.get("Authorization").get(0))
      .when()
        .put(String.format("api/orders/%d", testOrder.getId()))
      .then()
        .statusCode(employeeRole == EmployeeRole.DESK_PAYMENTS ? HttpStatus.SC_NO_CONTENT : HttpStatus.SC_FORBIDDEN)
        .extract().statusCode();

    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
    if (statusCode == HttpStatus.SC_FORBIDDEN) {
      verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
      verify(orderManagementServiceSpy, times(0)).manageOrder(any());
      verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
      // Setup test number X
      for (Optional<Order> orderOpt: List.of(orderRepository.findById(testOrder.getId()+1), orderRepository.findById(testOrder.getId()+2))){
        Order order = orderOpt.orElseThrow();
        orderManagementServiceSpy.manageOrder(order);
      }
    } else {
      verify(orderServiceSpy, times(1)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
      verify(orderManagementServiceSpy, times(1)).manageOrder(any());
      verify(orderNotifierServiceSpy, times(1)).sendNewOrder(
        argThat((Order order) -> order.getId().equals(testOrder.getId()))
      );
    }
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(4)
  void whenPayOrderThatDoesntExist_thenReturn404_OnlyForDeskPayments_elseReturn403(
    Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);

    RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", userHandshakeHeaders.get("Authorization").get(0))
      .when()
        .put("api/orders/398")
      .then()
        .statusCode(employeeRole == EmployeeRole.DESK_PAYMENTS ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_FORBIDDEN)
        .extract().statusCode();

    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
    verify(orderManagementServiceSpy, times(0)).manageOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(5)
  void whenReceiveUpdateOrder_FromIDLE_toPREPARING_thenSendUpdateThroughWebsockets(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  )
      throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);
    stompSession =
        connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

    // act
    stompSession.send("/app/order_updates", orderUpdateRequest);

    // wait until message received and update message is sent
    Awaitility.await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
              verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(testOrder.getId());
              verify(orderManagementServiceSpy, times(1))
                  .manageOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.PREPARING, testOrder.isPriority());
            });

    // assert
    verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(testOrder.getId());
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(6)
  void whenReceiveUpdateOrder_FromPREPARING_to_READY_thenSendUpdateThroughWebsockets(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);
    stompSession =
        connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

    // setup (have to change the status of the object because the reference isn't the same)
    testOrder.setOrderStatus(OrderStatus.PREPARING);

    // act
    stompSession.send("/app/order_updates", orderUpdateRequest);

    // wait until message received and update message is sent
    Awaitility.await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
              verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(testOrder.getId());
              verify(orderManagementServiceSpy, times(1))
                  .manageOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.READY, testOrder.isPriority());
            });

    // assert
    verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(testOrder.getId());
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(7)
  void whenReceiveUpdateOrder_FromREADY_to_PICKED_UP_thenRemoveFromQueue(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}; employeeRole: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders, employeeRole);
    stompSession =
        connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

    // setup (have to change the status of the object because the reference isn't the same)
    testOrder.setOrderStatus(OrderStatus.READY);

    // act
    stompSession.send("/app/order_updates", orderUpdateRequest);

    // wait until message received and update message is sent
    Awaitility.await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
              verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(testOrder.getId());
              verify(orderManagementServiceSpy, times(1))
                  .manageOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.PICKED_UP, testOrder.isPriority());
            });

    // assert
    verify(orderNotifierServiceSpy, times(1))
        .sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PICKED_UP, testOrder.isPriority());
  }
}
