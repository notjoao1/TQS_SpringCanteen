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
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderUpdatesControllerIT {

    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest")
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

    @LocalServerPort
    Integer port;

    private String websocketURL;

    @SpyBean
    OrderNotifierService orderNotifierServiceSpy;

    @SpyBean
    OrderService orderServiceSpy;

    @SpyBean
    OrderManagementService orderManagementServiceSpy;

    @SpyBean
    OrderUpdatesController orderUpdatesControllerSpy;

    WebSocketStompClient webSocketStompClient;
    StompSession stompSession;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KioskTerminalRepository kioskTerminalRepository;

    List<Arguments> employeeOrderAndUpdateRequestAndToken;

    private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerIT.class);

    @BeforeAll
    void beforeAllSetup(){
        RestAssured.port = port;
        websocketURL = String.format("ws://localhost:%d/websocket", port);

        KioskTerminal kioskTerminal = kioskTerminalRepository.save(new KioskTerminal());
        Order testOrder = new Order();
        testOrder.setPaid(false);
        testOrder.setPrice(0.0f);
        testOrder.setPriority(false);
        testOrder.setNif("123456789");
        testOrder.setKioskTerminal(kioskTerminal);
        testOrder.setOrderStatus(OrderStatus.NOT_PAID);
        orderRepository.save(testOrder);

        String signUpRequestTemplate = "{" +
                "    \"username\": \"%s\"," +
                "    \"email\": \"%s\"," +
                "    \"password\": \"%s\"," +
                "    \"role\": \"%s\"}";
        List<Employee> employees = List.of(
                new Employee("cook", "mockcook@gmail.com", "cook_password", EmployeeRole.COOK),
                new Employee("desk_payments", "desk_payments@gmail.com", "desk_payments_password", EmployeeRole.DESK_PAYMENTS),
                new Employee("desk_orders", "desk_orders@gmail.com", "desk_orders_password", EmployeeRole.DESK_ORDERS)
        );
        employeeOrderAndUpdateRequestAndToken = employees.stream().map(employee -> {
            String token = RestAssured.
                    given()
                        .contentType(ContentType.JSON)
                        .body(String.format(signUpRequestTemplate, employee.getUsername(), employee.getEmail(), employee.getPassword(), employee.getRole().name()))
                    .when()
                        .post("api/auth/signup")
                    .then()
                        .statusCode(HttpStatus.SC_CREATED)
                        .body("$", hasKey("token"))
                    .extract()
                        .path("token");
            Order newOrder = orderRepository.save(new Order(testOrder.getOrderStatus(), testOrder.isPaid(), testOrder.isPriority(), testOrder.getNif(), testOrder.getKioskTerminal()));
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(newOrder.getId());
            return Arguments.of(newOrder, orderUpdateRequest, token);
        }).toList();
    }


    @BeforeEach
    void setup(){
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void teardown() {
        if (stompSession != null)
            stompSession.disconnect();
    }

    @AfterAll
    void afterAll(){
        container.stop();
    }


    private Stream<Arguments> provideAllTokenHeaders(){
        return employeeOrderAndUpdateRequestAndToken.stream().map(arguments -> {
            Object[] argumentsObjects = arguments.get();
            Order order = (Order) argumentsObjects[0];
            OrderUpdateRequestDTO orderUpdateRequest = (OrderUpdateRequestDTO) argumentsObjects[1];
            String token = (String) argumentsObjects[2];
            StompHeaders handshakeHeaders = new StompHeaders();
            handshakeHeaders.set("Authorization", "Bearer " + token);
            return Arguments.of(order, orderUpdateRequest, handshakeHeaders);
        });
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void whenUnauthenticatedConnect_thenConnectionClosed(){
        // act
        // try to connect and verify it failed
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            webSocketStompClient.connectAsync(websocketURL, new StompSessionHandlerAdapter() {})
                    .get(1, TimeUnit.SECONDS);
        });

        assertInstanceOf(ConnectionLostException.class, exception.getCause());
    }


    @ParameterizedTest
    @MethodSource("provideAllTokenHeaders")
    @org.junit.jupiter.api.Order(2)
    void whenReceiveUpdateOrder_FromNOTPAID_toIDLE_thenSendUpdateThroughWebsockets(
            Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
    ) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}", testOrder, orderUpdateRequest, userHandshakeHeaders);
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());
            verify(orderManagementServiceSpy, times(1)).manageOrder(argThat((Order order) ->
                    order.getId().equals(testOrder.getId()) // TODO: couldn't verify orderStatus because it uses reference
            ));
            verify(orderNotifierServiceSpy, times(1)).sendNewOrder(argThat((Order order) ->
                order.getId().equals(testOrder.getId()) && order.getOrderStatus() == OrderStatus.IDLE
            ));
        });

        // assert
        verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());

    }

    @ParameterizedTest
    @MethodSource("provideAllTokenHeaders")
    @org.junit.jupiter.api.Order(3)
    void whenReceiveUpdateOrder_FromIDLE_toPREPARING_thenSendUpdateThroughWebsockets(
            Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
    ) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        logger.info("testOrder: {}; orderUpdateRequest: {}; userHandshakeHeaders: {}", testOrder, orderUpdateRequest, userHandshakeHeaders);
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());
            verify(orderManagementServiceSpy, times(1)).manageOrder(argThat((Order order) ->
                    order.getId().equals(testOrder.getId()) // TODO: couldn't verify orderStatus because it uses reference
            ));
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PREPARING);
        });

        // assert
        verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());

    }


    @ParameterizedTest
    @MethodSource("provideAllTokenHeaders")
    @org.junit.jupiter.api.Order(4)
    void whenReceiveUpdateOrder_FromPREPARING_to_READY_thenSendUpdateThroughWebsockets(
            Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
    ) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        logger.info("testOrder {} {}", testOrder.getId(), testOrder.getOrderStatus());
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

        // setup (have to change the status of the object because the reference isn't the same)
        testOrder.setOrderStatus(OrderStatus.PREPARING);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());
            verify(orderManagementServiceSpy, times(1)).manageOrder(argThat((Order order) ->
                    order.getId().equals(testOrder.getId()) // TODO: couldn't verify orderStatus because it uses reference
            ));
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.READY);
        });

        // assert
        verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());
    }

    @ParameterizedTest
    @MethodSource("provideAllTokenHeaders")
    @org.junit.jupiter.api.Order(5)
    void whenReceiveUpdateOrder_FromREADY_to_PICKED_UP_thenRemoveFromQueue(
            Order testOrder, OrderUpdateRequestDTO orderUpdateRequest, StompHeaders userHandshakeHeaders
    ) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        logger.info("testOrder {} {}", testOrder.getId(), testOrder.getOrderStatus());
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);

        // setup (have to change the status of the object because the reference isn't the same)
        testOrder.setOrderStatus(OrderStatus.READY);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderServiceSpy, times(1)).changeToNextOrderStatus(testOrder.getId());
            verify(orderManagementServiceSpy, times(1)).manageOrder(argThat((Order order) ->
                    order.getId().equals(testOrder.getId()) // TODO: couldn't verify orderStatus because it uses reference
            ));
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PICKED_UP);
        });

        // assert
        verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PICKED_UP);
    }

}
