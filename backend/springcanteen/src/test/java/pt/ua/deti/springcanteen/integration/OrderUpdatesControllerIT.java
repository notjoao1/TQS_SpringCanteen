package pt.ua.deti.springcanteen.integration;

import static io.restassured.RestAssured.reset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.OrderManagementService;
import pt.ua.deti.springcanteen.service.OrderNotifierService;
import pt.ua.deti.springcanteen.service.OrderService;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderUpdatesControllerIT {
    @Container
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

    Order testOrder;


    @BeforeEach
    void setup() throws InterruptedException, ExecutionException, TimeoutException {
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompSession = webSocketStompClient.connectAsync("ws://localhost:" + port + "/websocket", new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);

        // will be saved in db in each test (gotta set all fields because of @NotNull annotations)
        testOrder = new Order();
        testOrder.setPaid(false);
        testOrder.setPrice(0.0f);
        testOrder.setPriority(false);
        testOrder.setNif("123456789");
        testOrder.setKioskTerminal(new KioskTerminal(1L, null));
    }

    @AfterEach
    void teardown() {
        orderRepository.deleteById(10L);
        stompSession.disconnect();
    }


    @Test
    void whenReceiveUpdateOrder_FromIDLE_toPREPARING_thenSendUpdateThroughWebsockets() throws Exception {
        // setup
        testOrder.setOrderStatus(OrderStatus.IDLE);
        orderRepository.save(testOrder);
        OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
        orderUpdateRequest.setOrderId(testOrder.getId());
        orderUpdateRequest.setNewOrderStatus(OrderStatus.PREPARING);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PREPARING);
        });

        // assert
        verify(orderServiceSpy, times(1)).changeOrderStatus(testOrder.getId(), OrderStatus.PREPARING);
    }


    @Test
    void whenReceiveUpdateOrder_FromPREPARING_to_READY_thenSendUpdateThroughWebsockets() throws Exception {
        // setup
        testOrder.setOrderStatus(OrderStatus.PREPARING);
        orderRepository.save(testOrder);
        OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
        orderUpdateRequest.setOrderId(testOrder.getId());
        orderUpdateRequest.setNewOrderStatus(OrderStatus.READY);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.READY);
        });

        // assert
        verify(orderServiceSpy, times(1)).changeOrderStatus(testOrder.getId(), OrderStatus.READY);
    }

    @Test
    void whenReceiveUpdateOrder_FromREADY_to_PICKED_UP_thenRemoveFromQueue() throws Exception {
        // setup
        testOrder.setOrderStatus(OrderStatus.READY);
        orderRepository.save(testOrder);
        OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
        orderUpdateRequest.setOrderId(testOrder.getId());
        orderUpdateRequest.setNewOrderStatus(OrderStatus.PICKED_UP);

        // act
        stompSession.send("/app/order_updates", orderUpdateRequest);

        // wait until message received and update message is sent
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PICKED_UP);
        });

        // assert
        verify(orderManagementServiceSpy, times(1)).removeOrder(any());
        verify(orderServiceSpy, times(1)).changeOrderStatus(testOrder.getId(), OrderStatus.PICKED_UP);
        verify(orderNotifierServiceSpy, times(1)).sendOrderStatusUpdates(testOrder.getId(), OrderStatus.PICKED_UP);
    }

    @Test
    void whenReceiveUpdateOrder_FromIDLE_toInvalidNewStatus_thenDontUpdate() {
        // setup
        testOrder.setOrderStatus(OrderStatus.IDLE);
        orderRepository.save(testOrder);

        List<OrderStatus> invalidNewStatuses = Arrays.asList(OrderStatus.NOT_PAID, OrderStatus.READY, OrderStatus.PICKED_UP);

        for (OrderStatus invalidNewStatus : invalidNewStatuses) {
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(testOrder.getId());
            orderUpdateRequest.setNewOrderStatus(invalidNewStatus);

            // act
            stompSession.send("/app/order_updates", orderUpdateRequest);

            // wait until message received 
            Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            });

            // assert
            verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(testOrder.getId(), invalidNewStatus);
            verify(orderManagementServiceSpy, times(0)).addOrder(any());
            verify(orderManagementServiceSpy, times(0)).removeOrder(any());

            // reset for next iteration
            reset(orderUpdatesControllerSpy, orderServiceSpy, orderNotifierServiceSpy);    
        }
    }

    /*
     * Updating a NOT_PAID order will probably be done with an endpoint in OrderController
     * and not through messaging, as we decided in the architecture, so any requests to update
     * NOT_PAID orders using the websockets server will be ignored
     */
    @Test
    void whenReceiveUpdateOrder_FromNOT_PAID_toAnyNewStatus_thenDontUpdate() {
        // setup
        testOrder.setOrderStatus(OrderStatus.NOT_PAID);
        orderRepository.save(testOrder);

        List<OrderStatus> invalidNewStatuses = Arrays.asList(OrderStatus.IDLE, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.PICKED_UP);

        for (OrderStatus invalidNewStatus : invalidNewStatuses) {
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(testOrder.getId());
            orderUpdateRequest.setNewOrderStatus(invalidNewStatus);

            // act
            stompSession.send("/app/order_updates", orderUpdateRequest);

            // wait until message received 
            Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            });

            // assert
            verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(testOrder.getId(), invalidNewStatus);
            verify(orderManagementServiceSpy, times(0)).addOrder(any());
            verify(orderManagementServiceSpy, times(0)).removeOrder(any());

            // reset for next iteration
            reset(orderUpdatesControllerSpy, orderServiceSpy, orderNotifierServiceSpy);    
        }
    }

    @Test
    void whenReceiveUpdateOrder_FromPREPARING_toInvalidNewStatus_thenDontUpdate() {
        // setup
        testOrder.setOrderStatus(OrderStatus.PREPARING);
        orderRepository.save(testOrder);

        List<OrderStatus> invalidNewStatuses = Arrays.asList(OrderStatus.IDLE, OrderStatus.NOT_PAID, OrderStatus.PICKED_UP);

        for (OrderStatus invalidNewStatus : invalidNewStatuses) {
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(testOrder.getId());
            orderUpdateRequest.setNewOrderStatus(invalidNewStatus);

            // act
            stompSession.send("/app/order_updates", orderUpdateRequest);

            // wait until message received 
            Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            });

            // assert
            verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(testOrder.getId(), invalidNewStatus);
            verify(orderManagementServiceSpy, times(0)).addOrder(any());
            verify(orderManagementServiceSpy, times(0)).removeOrder(any());

            // reset for next iteration
            reset(orderUpdatesControllerSpy, orderServiceSpy, orderNotifierServiceSpy);    
        }
    }

    @Test
    void whenReceiveUpdateOrder_FromREADY_toInvalidNewStatus_thenDontUpdate() {
        // setup
        testOrder.setOrderStatus(OrderStatus.READY);
        orderRepository.save(testOrder);

        List<OrderStatus> invalidNewStatuses = Arrays.asList(OrderStatus.IDLE, OrderStatus.NOT_PAID, OrderStatus.PREPARING);

        for (OrderStatus invalidNewStatus : invalidNewStatuses) {
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(testOrder.getId());
            orderUpdateRequest.setNewOrderStatus(invalidNewStatus);

            // act
            stompSession.send("/app/order_updates", orderUpdateRequest);

            // wait until message received 
            Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            });

            // assert
            verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(testOrder.getId(), invalidNewStatus);
            verify(orderManagementServiceSpy, times(0)).addOrder(any());
            verify(orderManagementServiceSpy, times(0)).removeOrder(any());

            // reset for next iteration
            reset(orderUpdatesControllerSpy, orderServiceSpy, orderNotifierServiceSpy);    
        }
    }

    @Test
    void whenReceiveUpdateOrder_FromPICKED_UP_toAnyNewStatus_thenDontUpdate() {
        // setup
        testOrder.setOrderStatus(OrderStatus.READY);
        orderRepository.save(testOrder);

        List<OrderStatus> invalidNewStatuses = Arrays.asList(OrderStatus.NOT_PAID, OrderStatus.IDLE, OrderStatus.PREPARING, OrderStatus.READY);

        for (OrderStatus invalidNewStatus : invalidNewStatuses) {
            OrderUpdateRequestDTO orderUpdateRequest = new OrderUpdateRequestDTO();
            orderUpdateRequest.setOrderId(testOrder.getId());
            orderUpdateRequest.setNewOrderStatus(invalidNewStatus);

            // act
            stompSession.send("/app/order_updates", orderUpdateRequest);

            // wait until message received 
            Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(orderUpdatesControllerSpy, times(1)).receiveOrderUpdates(any());
            });

            // assert
            verify(orderNotifierServiceSpy, times(0)).sendOrderStatusUpdates(testOrder.getId(), invalidNewStatus);
            verify(orderManagementServiceSpy, times(0)).addOrder(any());
            verify(orderManagementServiceSpy, times(0)).removeOrder(any());

            // reset for next iteration
            reset(orderUpdatesControllerSpy, orderServiceSpy, orderNotifierServiceSpy);    
        }
    }
}
