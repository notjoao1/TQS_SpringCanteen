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
import pt.ua.deti.springcanteen.exceptions.InvalidStatusChangeException;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.QueueNotifierService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderUpdatesIT {

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

  public OrderUpdatesIT() {
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

  List<Arguments> employeeOrderAndUpdateRequestAndHeader;

  private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesIT.class);

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
    employeeOrderAndUpdateRequestAndHeader =
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
                  StompHeaders handshakeHeaders = new StompHeaders();
                  handshakeHeaders.set("Authorization", "Bearer " + token);
                  return Arguments.of(newOrder, orderUpdateRequest, handshakeHeaders, employee.getRole());
                }).toList();
  }

  @BeforeEach
  void setup() {
    webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
    webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
  }

  @AfterEach
  void teardown() {
    if (stompSession != null && stompSession.isConnected()) {
      logger.info("disconnecting");
      stompSession.disconnect();
    }
  }

  @AfterAll
  void afterAll() {
    container.stop();
  }

  private Stream<Arguments> provideAllArguments() {
    return employeeOrderAndUpdateRequestAndHeader.stream();
  }

  private Arguments provideArgumentsForEmployeeWithCertainRole(EmployeeRole employeeRole){
    return provideAllArguments().filter(arguments -> arguments.get()[3] == employeeRole).findFirst().orElseThrow();
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
  @MethodSource("provideAllArguments")
  void whenAuthenticatedConnect_thenMessagesReceived(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) throws InterruptedException, ExecutionException, TimeoutException {
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
  }

  @ParameterizedTest
  @EnumSource(value = OrderStatus.class, names = {"NOT_PAID", "PICKED_UP"})
  void whenReceiveUpdateOrder_FromInvalidStatus_thenInvalidStatusChangeException( OrderStatus orderStatus )
  throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    Object[] argumentsObjects = provideArgumentsForEmployeeWithCertainRole(EmployeeRole.DESK_PAYMENTS).get();
    OrderUpdateRequestDTO orderUpdateRequest = (OrderUpdateRequestDTO) argumentsObjects[1];
    StompHeaders userHandshakeHeaders = (StompHeaders) argumentsObjects[2];
    EmployeeRole employeeRole = (EmployeeRole) argumentsObjects[3];
    Order testOrder = (Order) argumentsObjects[0];
    testOrder.setOrderStatus(orderStatus);
    orderRepository.save(testOrder);
    logger.info("testOrder: {}; orderUpdateRequest: {}; employeeRole: {}; userHandshakeHeaders: {};",
      testOrder.getId(), orderUpdateRequest, employeeRole, userHandshakeHeaders);
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
    verify(orderManagementServiceSpy, times(0)).manageNotPaidOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
  }

  @ParameterizedTest
  @EnumSource(value = OrderStatus.class, names = {"IDLE", "PREPARING", "READY", "PICKED_UP"})
  void whenPayOrderWithInvalidStatus_thenReturn400_ForDeskPayments(OrderStatus orderStatus) {
    // setup
    Object[] argumentsObjects = provideArgumentsForEmployeeWithCertainRole(EmployeeRole.DESK_PAYMENTS).get();
    StompHeaders userHandshakeHeaders = (StompHeaders) argumentsObjects[2];
    EmployeeRole employeeRole = (EmployeeRole) argumentsObjects[3];
    Order testOrder = (Order) argumentsObjects[0];
    testOrder.setOrderStatus(orderStatus);
    orderRepository.save(testOrder);
    logger.info("testOrder: {}; employeeRole: {}; userHandshakeHeaders: {};", testOrder.getId(), employeeRole, userHandshakeHeaders);

    RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", userHandshakeHeaders.get("Authorization").get(0))
      .when()
        .put(String.format("api/orders/%s", testOrder.getId()))
      .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .extract().statusCode();

    verify(orderServiceSpy, times(1)).changeNotPaidOrderToNextOrderStatus(testOrder.getId());
    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(any());
    verify(orderManagementServiceSpy, times(0)).manageOrder(any());
    verify(orderManagementServiceSpy, times(0)).manageNotPaidOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
  }

  @ParameterizedTest
  @MethodSource("provideAllArguments")
  void whenPayOrderThatDoesntExist_thenReturn404_OnlyForDeskPayments_elseReturn403(
    Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) {
    // setup
    logger.info("employeeRole: {}; userHandshakeHeaders: {};",employeeRole, userHandshakeHeaders);

    int statusCode = RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", userHandshakeHeaders.get("Authorization").get(0))
      .when()
        .put("api/orders/398")
      .then()
        .statusCode(employeeRole == EmployeeRole.DESK_PAYMENTS ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_FORBIDDEN)
        .extract().statusCode();

    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderManagementServiceSpy, times(0)).manageOrder(any());
    verify(orderManagementServiceSpy, times(0)).manageNotPaidOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
    if (statusCode == HttpStatus.SC_FORBIDDEN) {
      verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(398L);
    } else {
      verify(orderServiceSpy, times(1)).changeNotPaidOrderToNextOrderStatus(398L);
    }
  }

  @ParameterizedTest
  @MethodSource("provideAllArguments")
  void whenReceiveOrderThatDoesntExist_thenDoNothing(
    Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    OrderUpdateRequestDTO orderUpdateRequestDTO = new OrderUpdateRequestDTO();
    orderUpdateRequestDTO.setOrderId(398L);
    logger.info("orderUpdateRequestDTO: {}; employeeRole: {}; userHandshakeHeaders: {};",
      orderUpdateRequestDTO, employeeRole, userHandshakeHeaders);
    stompSession =
      connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

    // act
    stompSession.send("/app/order_updates", orderUpdateRequestDTO);

    // wait until message received
    Awaitility.await()
      .atMost(2, TimeUnit.SECONDS)
      .untilAsserted(
        () -> {
          verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(orderUpdateRequestDTO);
        });

    // assert
    verify(orderServiceSpy, times(1)).changePaidOrderToNextOrderStatus(orderUpdateRequestDTO.getOrderId());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(any());
    verify(orderManagementServiceSpy, times(0)).manageOrder(any());
    verify(orderManagementServiceSpy, times(0)).manageNotPaidOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
  }

  @ParameterizedTest
  @MethodSource("provideAllArguments")
  void whenPayNotPaidOrder_WithStatusNotPaid_thenReturn204_OnlyForDeskPayments_elseReturn403(
    Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders, EmployeeRole employeeRole
  ) {
    // setup
    Order newOrder = new Order(
      OrderStatus.NOT_PAID,testOrder.isPaid(),testOrder.isPriority(),testOrder.getNif(),testOrder.getKioskTerminal()
    );
    orderRepository.save(newOrder);
    logger.info("newOrder: {}; orderUpdateRequest: {}; employeeRole: {}; userHandshakeHeaders: {};",
      newOrder.getId(), orderUpdateRequest, employeeRole, userHandshakeHeaders);

    int statusCode = RestAssured
      .given()
        .contentType(ContentType.JSON)
        .header("Authorization", userHandshakeHeaders.get("Authorization").get(0))
      .when()
        .put(String.format("api/orders/%d", newOrder.getId()))
      .then()
        .statusCode(employeeRole == EmployeeRole.DESK_PAYMENTS ? HttpStatus.SC_NO_CONTENT : HttpStatus.SC_FORBIDDEN)
        .extract().statusCode();

    verify(orderServiceSpy, times(0)).changePaidOrderToNextOrderStatus(orderUpdateRequest.getOrderId());
    verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(anyLong(), any(), anyBoolean());
    if (statusCode == HttpStatus.SC_FORBIDDEN) {
      verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(newOrder.getId());
      verify(orderManagementServiceSpy, times(0)).manageOrder(any());
      verify(orderManagementServiceSpy, times(0)).manageNotPaidOrder(any());
      verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    } else {
      verify(orderServiceSpy, times(1)).changeNotPaidOrderToNextOrderStatus(newOrder.getId());
      verify(orderManagementServiceSpy, times(1)).manageOrder(
        argThat((Order order) -> order.getId().equals(newOrder.getId()))
      );
      verify(orderManagementServiceSpy, times(1)).manageNotPaidOrder(
        argThat((Order order) -> order.getId().equals(newOrder.getId()))
      );
      verify(orderNotifierServiceSpy, times(1)).sendNewOrder(
        argThat((Order order) -> order.getId().equals(newOrder.getId()) && order.getOrderStatus() == OrderStatus.IDLE)
      );
    }
  }


}
