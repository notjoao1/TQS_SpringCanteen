package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;
import pt.ua.deti.springcanteen.service.QueueNotifierService;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderUpdatesControllerValidFlowIT {

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

  public OrderUpdatesControllerValidFlowIT() {
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

  private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerValidFlowIT.class);

  @BeforeAll
  void beforeAllSetup() {
    RestAssured.port = port;
    websocketURL = String.format("ws://localhost:%d/websocket", port);

    KioskTerminal kioskTerminal = kioskTerminalRepository.save(new KioskTerminal());
    Order testOrder = new Order();
    testOrder.setPriority(false);
    testOrder.setPaid(true);
    testOrder.setOrderStatus(OrderStatus.IDLE);
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
                  Queue<OrderEntry> queue = (Queue<OrderEntry>) ReflectionTestUtils.getField(orderManagementServiceSpy, "regularIdleOrders");
                  queue.add(OrderEntry.fromOrderEntity(newOrder));
                  OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
                  orderUpdateRequest.setOrderId(newOrder.getId());
                  StompHeaders handshakeHeaders = new StompHeaders();
                  handshakeHeaders.set("Authorization", "Bearer " + token);
                  return Arguments.of(newOrder, orderUpdateRequest, handshakeHeaders);
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
    if (stompSession != null && stompSession.isConnected()) {
      logger.info("disconnecting");
      stompSession.disconnect();
    }
  }

  @AfterAll
  void afterAll() {
    container.stop();
  }

  private Stream<Arguments> provideAllTokenHeaders() {
    return employeeOrderAndUpdateRequestAndToken.stream();
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(1)
  void whenReceiveUpdateOrder_FromIDLE_thenSendUpdateThroughWebsockets(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
  )
      throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {};",
      testOrder, orderUpdateRequest, userHandshakeHeaders);
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
              verify(orderManagementServiceSpy, times(1))
                  .manageIdleOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.PREPARING, testOrder.isPriority());
            });

    // assert
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(any());
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(2)
  void whenReceiveUpdateOrder_FromPREPARING_thenSendUpdateThroughWebsockets(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders);
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
              verify(orderManagementServiceSpy, times(1))
                  .managePreparingOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.READY, testOrder.isPriority());
            });

    // assert
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(any());
  }

  @ParameterizedTest
  @MethodSource("provideAllTokenHeaders")
  @org.junit.jupiter.api.Order(3)
  void whenReceiveUpdateOrder_FromREADY_PICKED_UP_thenRemoveFromQueue(
      Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
  ) throws ExecutionException, InterruptedException, TimeoutException {
    // setup
    logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}",
      testOrder, orderUpdateRequest, userHandshakeHeaders);
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
              verify(orderManagementServiceSpy, times(1))
                .manageReadyOrder(argThat((Order order) -> order.getId().equals(testOrder.getId())));
              verify(orderNotifierServiceSpy, times(1))
                  .sendOrderStatusUpdates(
                      testOrder.getId(), OrderStatus.PICKED_UP, testOrder.isPriority());
            });

    // assert
    verify(orderNotifierServiceSpy, times(0)).sendNewOrder(any());
    verify(orderServiceSpy, times(0)).changeNotPaidOrderToNextOrderStatus(any());
  }
}
