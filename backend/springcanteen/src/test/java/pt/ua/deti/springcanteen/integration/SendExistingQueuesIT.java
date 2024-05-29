package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.MenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Stream;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ua.deti.springcanteen.ConvertUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @SpyBean
    OrderNotifierService orderNotifierServiceSpy;

    @SpyBean
    OrderService orderServiceSpy;

    @SpyBean
    OrderManagementService orderManagementServiceSpy;

    @SpyBean
    OrderUpdatesController orderUpdatesControllerSpy;

    @Autowired
    KioskTerminalRepository kioskTerminalRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    OrderMenuRepository orderMenuRepository;

    @Autowired
    OrderRepository orderRepository;

    private String websocketURL;

    private static final int QUEUE_CAPACITY = 120;

    private final Queue<OrderEntry> regularIdleOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);;

    private final Queue<OrderEntry> priorityIdleOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final Queue<OrderEntry> regularPreparingOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final Queue<OrderEntry> priorityPreparingOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final Queue<OrderEntry> regularReadyOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final Queue<OrderEntry> priorityReadyOrders = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    Order order1, order2, order3, order4, order5, order6, order7, order8;

    WebSocketStompClient webSocketStompClient;
    StompSession stompSession;

    Stream<StompHeaders> authorizedEmployeesHeaders;
    StompHeaders unauthorizedEmployeeHeaders;

    private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerIT.class);
    private static final String ORDER_TOPIC = "/topic/orders";

    @BeforeAll
    void beforeAllSetup(){
        RestAssured.port = port;
        websocketURL = String.format("ws://localhost:%d/websocket", port);

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

        // Already created in database
        KioskTerminal kioskTerminal;
        OrderMenu orderMenu1, orderMenu2, orderMenu3, orderMenu4, orderMenu5, orderMenu6, orderMenu7, orderMenu8, orderMenu9, orderMenu10, orderMenu11;
        Menu menu1, menu2, menu3, menu4;

        kioskTerminal = kioskTerminalRepository.findById(1L).orElseThrow();
        menu1 = menuRepository.findById(1L).orElseThrow();
        menu2 = menuRepository.findById(2L).orElseThrow();
        menu3 = menuRepository.findById(3L).orElseThrow();
        menu4 = menuRepository.findById(4L).orElseThrow();
        orderMenu1 = new OrderMenu(null, menu1, "{}");
        orderMenu2 = new OrderMenu(null, menu2, "{}");
        orderMenu3 = new OrderMenu(null, menu3, "{}");
        orderMenu4 = new OrderMenu(null, menu4, "{}");
        orderMenu5 = new OrderMenu(null, menu3, "{}");
        orderMenu6 = new OrderMenu(null, menu1, "{}");
        orderMenu7 = new OrderMenu(null, menu2, "{}");
        orderMenu8 = new OrderMenu(null, menu4, "{}");
        orderMenu9 = new OrderMenu(null, menu1, "{}");
        orderMenu10 = new OrderMenu(null, menu2, "{}");
        orderMenu11 = new OrderMenu(null, menu1, "{}");
        order1  = orderRepository.save(new Order(OrderStatus.IDLE, true, false, "12345678", kioskTerminal));
        order2 = orderRepository.save(new Order(OrderStatus.IDLE, true, false, "12345678", kioskTerminal));
        order3 = orderRepository.save(new Order(OrderStatus.IDLE, true, true, "12345678", kioskTerminal));
        order4 = orderRepository.save(new Order(OrderStatus.PREPARING, true, false, "12345678", kioskTerminal));
        order5 = orderRepository.save(new Order(OrderStatus.PREPARING, true, true, "12345678", kioskTerminal));
        order6 = orderRepository.save(new Order(OrderStatus.PREPARING, true, true, "12345678", kioskTerminal));
        order7 = orderRepository.save(new Order(OrderStatus.READY, true, false, "12345678", kioskTerminal));
        order8 = orderRepository.save(new Order(OrderStatus.READY, true, true, "12345678", kioskTerminal));
        orderMenu1.setOrder(order1); orderMenu2.setOrder(order1); order1.setOrderMenus(Set.of(orderMenu1, orderMenu2));
        orderMenu3.setOrder(order2); order2.setOrderMenus(Set.of(orderMenu3));
        orderMenu4.setOrder(order3); orderMenu5.setOrder(order3); order3.setOrderMenus(Set.of(orderMenu4, orderMenu5));
        orderMenu6.setOrder(order4); order4.setOrderMenus(Set.of(orderMenu6));
        orderMenu7.setOrder(order5); orderMenu8.setOrder(order5); order5.setOrderMenus(Set.of(orderMenu7, orderMenu8));
        orderMenu9.setOrder(order6); order6.setOrderMenus(Set.of(orderMenu9));
        orderMenu10.setOrder(order7); order7.setOrderMenus(Set.of(orderMenu10));
        orderMenu11.setOrder(order8); order8.setOrderMenus(Set.of(orderMenu11));
        orderMenuRepository.saveAll(Set.of(orderMenu1, orderMenu2, orderMenu3, orderMenu4, orderMenu5, orderMenu6, orderMenu7, orderMenu8, orderMenu9, orderMenu10, orderMenu11));

        regularIdleOrders.add(OrderEntry.fromOrderEntity(order1));
        regularIdleOrders.add(OrderEntry.fromOrderEntity(order2));
        priorityIdleOrders.add(OrderEntry.fromOrderEntity(order3));
        regularPreparingOrders.add(OrderEntry.fromOrderEntity(order4));
        priorityPreparingOrders.add(OrderEntry.fromOrderEntity(order5));
        priorityPreparingOrders.add(OrderEntry.fromOrderEntity(order6));
        regularReadyOrders.add(OrderEntry.fromOrderEntity(order7));
        priorityReadyOrders.add(OrderEntry.fromOrderEntity(order8));

        ReflectionTestUtils.setField(orderManagementServiceSpy, "regularIdleOrders", regularIdleOrders);
        ReflectionTestUtils.setField(orderManagementServiceSpy, "priorityIdleOrders", priorityIdleOrders);
        ReflectionTestUtils.setField(orderManagementServiceSpy, "regularPreparingOrders", regularPreparingOrders);
        ReflectionTestUtils.setField(orderManagementServiceSpy, "priorityPreparingOrders", priorityPreparingOrders);
        ReflectionTestUtils.setField(orderManagementServiceSpy, "regularReadyOrders", regularReadyOrders);
        ReflectionTestUtils.setField(orderManagementServiceSpy, "priorityReadyOrders", priorityReadyOrders);
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
        return authorizedEmployeesHeaders.toList().stream();
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
            return QueueOrdersDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            futureMessage.complete((QueueOrdersDTO) payload);
        }

    }

    @ParameterizedTest
    @MethodSource("provideUnauthorizedUsersTokenHeaders")
    void whenSubscribeToOrderTopic_AndUnauthorized_ThenDoNotReturnAllQueueOrders(StompHeaders userHandshakeHeaders) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        CompletableFuture<QueueOrdersDTO> allOrdersMessage = new CompletableFuture<>();

        // act
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);
        stompSession.subscribe("/user" + ORDER_TOPIC, new CustomStompFrameHandler(allOrdersMessage));
        assertThrows(TimeoutException.class, () -> allOrdersMessage.get(10, TimeUnit.SECONDS));
    }

    @ParameterizedTest
    @MethodSource("provideAuthorizedUsersTokenHeaders")
    void whenSubscribeToOrderTopic_AndAuthorized_ThenReturnAllQueueOrders(StompHeaders userHandshakeHeaders) throws ExecutionException, InterruptedException, TimeoutException {
        // setup
        CompletableFuture<QueueOrdersDTO> allOrdersMessage = new CompletableFuture<>();

        // act
        stompSession = connectAsyncWithHeaders(websocketURL, webSocketStompClient, userHandshakeHeaders);
        stompSession.subscribe("/user" + ORDER_TOPIC, new CustomStompFrameHandler(allOrdersMessage));
        QueueOrdersDTO allOrders = allOrdersMessage.get(10, TimeUnit.SECONDS);
        assertThat(allOrders).isNotNull();
        assertThat(allOrders.getRegularIdleOrders())
                .isNotNull()
                .hasSize(2)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order1.getId(), ConvertUtils.getMenuNamesFromOrder(order1)),
                        tuple(order2.getId(), ConvertUtils.getMenuNamesFromOrder(order2))
                );
        assertThat(allOrders.getPriorityIdleOrders())
                .isNotNull()
                .hasSize(1)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order3.getId(), ConvertUtils.getMenuNamesFromOrder(order3))
                );
        assertThat(allOrders.getRegularPreparingOrders())
                .isNotNull()
                .hasSize(1)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order4.getId(), ConvertUtils.getMenuNamesFromOrder(order4))
                );
        assertThat(allOrders.getPriorityPreparingOrders())
                .isNotNull()
                .hasSize(2)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order5.getId(), ConvertUtils.getMenuNamesFromOrder(order5)),
                        tuple(order6.getId(), ConvertUtils.getMenuNamesFromOrder(order6))
                );
        assertThat(allOrders.getRegularReadyOrders())
                .isNotNull()
                .hasSize(1)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order7.getId(), ConvertUtils.getMenuNamesFromOrder(order7))
                );
        assertThat(allOrders.getPriorityReadyOrders())
                .isNotNull()
                .hasSize(1)
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order8.getId(), ConvertUtils.getMenuNamesFromOrder(order8))
                );
    }

}
