package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.lang.Nullable;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.entities.Employee;
import pt.ua.deti.springcanteen.entities.EmployeeRole;
import pt.ua.deti.springcanteen.entities.KioskTerminal;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasKey;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SendExistingQueuesIT {

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

    public SendExistingQueuesIT() {
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

    Stream<StompHeaders> authorizedEmployeesHeaders;
    StompHeaders unauthorizedEmployeeHeaders;

    private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerIT.class);
    private static final String ORDER_TOPIC = "/topic/orders";

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
        List<Employee> authorizedEmployees = List.of(
                new Employee("cook", "mockcook@gmail.com", "cook_password", EmployeeRole.COOK),
                new Employee("desk_orders", "desk_orders@gmail.com", "desk_orders_password", EmployeeRole.DESK_ORDERS)
        );

        authorizedEmployeesHeaders = authorizedEmployees.stream().map(employee -> {
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
            StompHeaders handshakeHeaders = new StompHeaders();
            handshakeHeaders.set("Authorization", "Bearer " + token);
            return handshakeHeaders;
        });

        Employee unauthorizedEmployee = new Employee("desk_payments", "desk_payments@gmail.com", "desk_payments_password", EmployeeRole.DESK_PAYMENTS);
        String token = RestAssured.
                given()
                    .contentType(ContentType.JSON)
                    .body(String.format(signUpRequestTemplate, unauthorizedEmployee.getUsername(), unauthorizedEmployee.getEmail(), unauthorizedEmployee.getPassword(), unauthorizedEmployee.getRole().name()))
                .when()
                    .post("api/auth/signup")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .body("$", hasKey("token"))
                .extract()
                    .path("token");
        unauthorizedEmployeeHeaders = new StompHeaders();
        unauthorizedEmployeeHeaders.set("Authorization", "Bearer " + token);

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

    private Stream<StompHeaders> provideAuthorizedUsersTokenHeaders(){
        return Stream.of(authorizedEmployeesHeaders.toList().get(0));
    }

    private Stream<StompHeaders> provideUnauthorizedUsersTokenHeaders(){
        return Stream.of(unauthorizedEmployeeHeaders);
    }

    private class CustomStompFrameHandler implements StompFrameHandler {

        private final CompletableFuture<QueueOrdersDTO> futureMessage;

        public CustomStompFrameHandler(CompletableFuture<QueueOrdersDTO> futureMessage) {
            this.futureMessage = futureMessage;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            logger.info("Received headers: {}", headers);
            return QueueOrdersDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            logger.info("Received payload: {}", payload);
            futureMessage.complete((QueueOrdersDTO) payload);
        }

    }

    @ParameterizedTest
    @MethodSource("provideAuthorizedUsersTokenHeaders")
    void whenSubscribeToOrderTopic_AndAthorized_ThenReturnAllQueueOrders(StompHeaders userHandshakeHeaders) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        CompletableFuture<QueueOrdersDTO> allOrdersMessage = new CompletableFuture<>();

        // act
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);
        stompSession.subscribe("/user" + ORDER_TOPIC, new CustomStompFrameHandler(allOrdersMessage));
        QueueOrdersDTO allOrders = allOrdersMessage.get(10, TimeUnit.SECONDS);
        logger.info("Received all orders: {}", allOrders);

    }

}
