package pt.ua.deti.springcanteen.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import pt.ua.deti.springcanteen.controllers.OrderUpdatesController;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.entities.KioskTerminal;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.KioskTerminalRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;

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

    Order testOrder;
    OrderUpdateRequestDTO orderUpdateRequest;

    private static final Logger logger = LoggerFactory.getLogger(OrderUpdatesControllerIT.class);

    @BeforeAll
    void beforeAllSetup(){
        KioskTerminal kioskTerminal = kioskTerminalRepository.save(new KioskTerminal());
        testOrder = new Order();
        testOrder.setPaid(false);
        testOrder.setPrice(0.0f);
        testOrder.setPriority(false);
        testOrder.setNif("123456789");
        testOrder.setKioskTerminal(kioskTerminal);
        testOrder.setOrderStatus(OrderStatus.NOT_PAID);
        orderRepository.save(testOrder);
        orderUpdateRequest = new OrderUpdateRequestDTO();
        orderUpdateRequest.setOrderId(testOrder.getId());
    }


    @BeforeEach
    void setup() throws InterruptedException, ExecutionException, TimeoutException {
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompSession = webSocketStompClient.connectAsync("ws://localhost:" + port + "/websocket", new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);
    }

    @AfterEach
    void teardown() {
        stompSession.disconnect();
    }

    @AfterAll
    void afterAll(){
        container.stop();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void whenReceiveUpdateOrder_FromNOTPAID_toIDLE_thenSendUpdateThroughWebsockets(){
        logger.info("test orderUpdateRequest {}", orderUpdateRequest);
        logger.info("testOrder {} {}", testOrder.getId(), testOrder.getOrderStatus());
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

    @Test
    @org.junit.jupiter.api.Order(2)
    void whenReceiveUpdateOrder_FromIDLE_toPREPARING_thenSendUpdateThroughWebsockets() throws Exception {
        logger.info("test orderUpdateRequest {}", orderUpdateRequest);
        logger.info("testOrder {} {}", testOrder.getId(), testOrder.getOrderStatus());
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


    @Test
    @org.junit.jupiter.api.Order(3)
    void whenReceiveUpdateOrder_FromPREPARING_to_READY_thenSendUpdateThroughWebsockets() throws Exception {
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

    @Test
    @org.junit.jupiter.api.Order(4)
    void whenReceiveUpdateOrder_FromREADY_to_PICKED_UP_thenRemoveFromQueue() throws Exception {
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
