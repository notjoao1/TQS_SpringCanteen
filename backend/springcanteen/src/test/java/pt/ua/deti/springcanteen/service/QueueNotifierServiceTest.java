package pt.ua.deti.springcanteen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import pt.ua.deti.springcanteen.ConvertUtils;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.service.impl.IQueueNotifierService;

/*
 * Need to initialize Spring Boot context, since it handles passing events
 * that occur (we will simulate a websocket event in this test class)
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueueNotifierServiceTest {
    @Mock
    SimpMessagingTemplate websocketClient;

    @Mock
    EmployeeService employeeService;

    @Mock
    OrderManagementService orderManagementService;

    @Mock
    SessionSubscribeEvent event;

    @InjectMocks
    IQueueNotifierService queueNotifierService;


    Order order1, order2, order3, order4, order5, order6, order7, order8;

    List<OrderCookResponseDTO> regularIdleOrders, priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders,
            regularReadyOrders, priorityReadyOrders;

    Employee cook, desk_payments, desk_orders;

    private static final String ORDER_TOPIC = "/topic/orders";


    @BeforeAll
    void setup() {
        OrderMenu orderMenu1, orderMenu2, orderMenu3, orderMenu4;
        Menu menu1, menu2, menu3, menu4;

        menu1 = new Menu(1L, "Beef with Rice", null, null, null, null);
        menu2 = new Menu(2L, "Vegan Rissois", null, null, null, null);
        menu3 = new Menu(3L, "Meat of pig Alentejana style", null, null, null, null);
        menu4 = new Menu(4L, "Chicken", null, null, null, null);
        orderMenu1 = new OrderMenu(null, menu1, "{}");
        orderMenu2 = new OrderMenu(null, menu2, "{}");
        orderMenu3 = new OrderMenu(null, menu3, "{}");
        orderMenu4 = new OrderMenu(null, menu4, "{}");
        order1  = new Order(1L, OrderStatus.IDLE, true, 10.0f, false, null, null, Set.of(orderMenu1, orderMenu2));
        order2 = new Order(2L, OrderStatus.IDLE, true, 5.0f, false, null, null, Set.of(orderMenu3));
        order3 = new Order(3L, OrderStatus.IDLE, true, 5.0f, true, null, null, Set.of(orderMenu2));
        order4 = new Order(4L, OrderStatus.PREPARING, true, 5.0f, false, null, null, Set.of(orderMenu3));
        order5 = new Order(5L, OrderStatus.PREPARING, true, 5.0f, true, null, null, Set.of(orderMenu1, orderMenu2));
        order6 = new Order(6L, OrderStatus.PREPARING, true, 5.0f, true, null, null, Set.of(orderMenu2, orderMenu4));
        order7 = new Order(7L, OrderStatus.READY, true, 5.0f, false, null, null, Set.of(orderMenu3));
        order8 = new Order(8L, OrderStatus.READY, true, 5.0f, true, null, null, Set.of(orderMenu3, orderMenu4));
        regularIdleOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order1), OrderCookResponseDTO.fromOrderEntity(order2));
        priorityIdleOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order3));
        regularPreparingOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order4));
        priorityPreparingOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order5), OrderCookResponseDTO.fromOrderEntity(order6));
        regularReadyOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order7));
        priorityReadyOrders = List.of(OrderCookResponseDTO.fromOrderEntity(order8));
        cook = new Employee("cook", "mockcook@gmail.com", "cook_password", EmployeeRole.COOK);
        desk_payments = new Employee("desk_payments", "desk_payments@gmail.com", "desk_payments_password", EmployeeRole.DESK_PAYMENTS);
        desk_orders = new Employee("desk_orders", "desk_orders@gmail.com", "desk_orders_password", EmployeeRole.DESK_ORDERS);
    }

    private Stream<Employee> provideAuthorizedUsers(){
        return Stream.of(cook, desk_orders);
    }

    @Test
    void whenSubscribeToTopicAndConnect_AndNotAuthorized_thenDoNotSendAllExistingOrders() {
        // setup
        when(event.getUser()).thenReturn(() -> "desk_payments@gmail.com");
        when(employeeService.getEmployeeByEmail("desk_payments@gmail.com")).thenReturn(Optional.of(desk_payments));

        // act
        queueNotifierService.sendExistingOrderQueues(event);

        // assert
        verify(websocketClient, times(0)).convertAndSendToUser(anyString(), eq(ORDER_TOPIC), anyString());
    }


    @ParameterizedTest
    @MethodSource("provideAuthorizedUsers")
    void whenSubscribeToTopicAndConnect_AndAuthorized_thenDoSendAllExistingOrders(Employee employee) {
        // setup
        String userEmail = employee.getEmail();
        ArgumentCaptor<QueueOrdersDTO> payloadCaptor = ArgumentCaptor.forClass(QueueOrdersDTO.class);
        ArgumentCaptor<String> topicDestinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userDestinationCaptor = ArgumentCaptor.forClass(String.class);
        when(event.getUser()).thenReturn(() -> userEmail);
        when(employeeService.getEmployeeByEmail(userEmail)).thenReturn(Optional.of(employee));
        when(orderManagementService.getAllOrders()).thenReturn(new QueueOrdersDTO(regularIdleOrders, priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders));

        // act
        queueNotifierService.sendExistingOrderQueues(event);

        // assert
        verify(websocketClient, times(1)).convertAndSendToUser(userDestinationCaptor.capture(), topicDestinationCaptor.capture(), payloadCaptor.capture());
        QueueOrdersDTO sentOrders = payloadCaptor.getValue();
        assertThat(userDestinationCaptor.getValue()).isEqualTo(userEmail);
        assertThat(topicDestinationCaptor.getValue()).isEqualTo("/topic/orders");
        assertThat(sentOrders.getRegularIdleOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order1.getId(), ConvertUtils.getMenuNamesFromOrder(order1)),
                        tuple(order2.getId(), ConvertUtils.getMenuNamesFromOrder(order2))
                );
        assertThat(sentOrders.getPriorityIdleOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order3.getId(), ConvertUtils.getMenuNamesFromOrder(order3))
                );
        assertThat(sentOrders.getRegularPreparingOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order4.getId(), ConvertUtils.getMenuNamesFromOrder(order4))
                );
        assertThat(sentOrders.getPriorityPreparingOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order5.getId(), ConvertUtils.getMenuNamesFromOrder(order5)),
                        tuple(order6.getId(), ConvertUtils.getMenuNamesFromOrder(order6))
                );
        assertThat(sentOrders.getRegularReadyOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order7.getId(), ConvertUtils.getMenuNamesFromOrder(order7))
                );
        assertThat(sentOrders.getPriorityReadyOrders())
                .extracting(
                        OrderCookResponseDTO::getId,
                        ConvertUtils::getMenuNamesFromDTO
                )
                .containsExactly(
                        tuple(order8.getId(), ConvertUtils.getMenuNamesFromOrder(order8))
                );
    }

}
