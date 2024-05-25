package pt.ua.deti.springcanteen.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IOrderManagementService;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {
    @Mock
    private Queue<OrderEntry> regularIdleOrders;
    @Mock
    private Queue<OrderEntry> priorityIdleOrders;
    @Mock
    private Queue<OrderEntry> regularPreparingOrders;
    @Mock
    private Queue<OrderEntry> priorityPreparingOrders;
    @Mock
    private Queue<OrderEntry> regularReadyOrders;
    @Mock
    private Queue<OrderEntry> priorityReadyOrders;
    
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderNotifierService orderNotifierService;

    @InjectMocks
    private IOrderManagementService orderManagementService;

    private Order order1, updatedOrder1;
    private static final Logger logger = LoggerFactory.getLogger(OrderManagementServiceTest.class);

    @BeforeEach
    void setup() {
        order1 = new Order();
        order1.setId(1L);

        updatedOrder1 = new Order();
        updatedOrder1.setId(1L);

        // runs before beforeEach
        // this has to be done, since Mockito doesn't know where to inject what
        // using constructor injection, only field injection
        orderManagementService.setRegularIdleOrders(regularIdleOrders);
        orderManagementService.setPriorityIdleOrders(priorityIdleOrders);
        orderManagementService.setRegularPreparingOrders(regularPreparingOrders);
        orderManagementService.setPriorityPreparingOrders(priorityPreparingOrders);
        orderManagementService.setRegularReadyOrders(regularReadyOrders);
        orderManagementService.setPriorityReadyOrders(priorityReadyOrders);
    }


    private static Stream<Arguments> providePriorityAndAllOrderStatusArgumentsForIdlePreparing(){
        return Stream.of(
                Arguments.of(true, OrderStatus.IDLE, OrderStatus.PREPARING),
                Arguments.of(false, OrderStatus.IDLE, OrderStatus.PREPARING),
                Arguments.of(true, OrderStatus.PREPARING, OrderStatus.READY),
                Arguments.of(false, OrderStatus.PREPARING, OrderStatus.READY)
        );
    }

    private static Stream<Arguments> providePriorityAndOldOrderStatusArgumentsForIdlePreparingReady(){
        return Stream.of(
                Arguments.of(true, OrderStatus.IDLE),
                Arguments.of(false, OrderStatus.IDLE),
                Arguments.of(true, OrderStatus.PREPARING),
                Arguments.of(false, OrderStatus.PREPARING),
                Arguments.of(true, OrderStatus.READY),
                Arguments.of(false, OrderStatus.READY)
        );
    }

    private Queue<OrderEntry> getQueueFromPriorityAndStatus(boolean priority, OrderStatus orderStatus) {
        return switch (orderStatus) {
            case IDLE -> priority ? priorityIdleOrders : regularIdleOrders;
            case PREPARING -> priority ? priorityPreparingOrders : regularPreparingOrders;
            case READY -> priority ? priorityReadyOrders : regularReadyOrders;
            default -> {
                logger.error("Invalid Order Status {}", orderStatus);
                throw new IllegalArgumentException("Invalid Order Status");
            }
        };
    }

    private List<Queue<OrderEntry>> getUnwantedQueues(List<Queue<OrderEntry>> queues) {
        return Stream.of(regularIdleOrders, priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders)
                .filter(queue -> !queues.contains(queue))
                .toList();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenQueueNotFullAndManageOrderWithStatusNOT_PAID_thenOrderAdded_andNewStatusIDLE_andSentNewOrderThroughWS(boolean priority) {
        Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(priority);
        updatedOrder1.setOrderStatus(OrderStatus.IDLE);
        updatedOrder1.setPriority(priority);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(idleOrdersQueue.offer(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB; add to IDLE orders queue depending on priority;
        //           send message through websockets notifying new order exists;
        verify(idleOrdersQueue, times(1)).offer(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(idleOrdersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendNewOrder(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenQueueFullAndManageOrderWithStatusNOT_PAID_thenOrderNotAdded_andMessageNotSentThroughWS(boolean priority) {
        Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(priority);

        when(idleOrdersQueue.offer(any())).thenReturn(false);

        // act
        orderManagementService.manageOrder(order1);

        // should -> not change order status and not save it to DB and not add to IDLE orders queue;
        //           not send message through websockets notifying new order exists;
        verify(idleOrdersQueue, times(1)).offer(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(idleOrdersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(0)).save(any());
        verify(orderNotifierService, times(0)).sendNewOrder(any());
    }

    @ParameterizedTest
    @MethodSource("providePriorityAndAllOrderStatusArgumentsForIdlePreparing")
    void whenQueueNotFullAndManageOrderWithCertainStatus_thenOrderAdded_andNewStatusSet_andSentStatusUpdateThroughWS(
            boolean priority, OrderStatus oldOrderStatus, OrderStatus newOrderStatus
    ) {
        Queue<OrderEntry> oldOrdersQueue = getQueueFromPriorityAndStatus(priority, oldOrderStatus);
        Queue<OrderEntry> nextOrdersQueue = getQueueFromPriorityAndStatus(priority, newOrderStatus);
        order1.setOrderStatus(oldOrderStatus);
        order1.setPriority(priority);
        updatedOrder1.setOrderStatus(newOrderStatus);
        updatedOrder1.setPriority(priority);

        when(oldOrdersQueue.contains(any(OrderEntry.class))).thenReturn(true);
        when(nextOrdersQueue.offer(any(OrderEntry.class))).thenReturn(true);
        when(oldOrdersQueue.remove(any(OrderEntry.class))).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(updatedOrder1);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB; add to next orders queue according to priority;
        //           remove from old orders queue;
        //           send message through websockets notifying status update
        verify(oldOrdersQueue, times(1)).contains(any(OrderEntry.class));
        verify(nextOrdersQueue, times(1)).offer(any(OrderEntry.class));
        verify(oldOrdersQueue, times(1)).remove(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(oldOrdersQueue, nextOrdersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, newOrderStatus);
    }

    @ParameterizedTest
    @MethodSource("providePriorityAndAllOrderStatusArgumentsForIdlePreparing")
    void whenQueueFullAndManageOrderWithStatusIDLE_thenOrderAdded_andNewStatusPREPARING_andSentStatusUpdateThroughWS(
            boolean priority, OrderStatus oldOrderStatus, OrderStatus newOrderStatus
    ) {
        Queue<OrderEntry> oldOrdersQueue = getQueueFromPriorityAndStatus(priority, oldOrderStatus);
        Queue<OrderEntry> nextOrdersQueue = getQueueFromPriorityAndStatus(priority, newOrderStatus);
        order1.setOrderStatus(oldOrderStatus);
        order1.setPriority(priority);
        updatedOrder1.setOrderStatus(newOrderStatus);
        updatedOrder1.setPriority(priority);

        when(oldOrdersQueue.contains(any(OrderEntry.class))).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> not change order status and not save it to DB; not add to next orders queue according to priority;
        //           stay in the same queue; not remove from old orders queue;
        //           not send message through websockets notifying status update
        verify(oldOrdersQueue, times(1)).contains(any(OrderEntry.class));
        verify(nextOrdersQueue, times(1)).offer(any(OrderEntry.class));
        verify(oldOrdersQueue, times(0)).remove(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(oldOrdersQueue, nextOrdersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(0)).save(any());
        verify(orderNotifierService, times(0)).sendOrderStatusUpdates(1L, OrderStatus.PREPARING);
    }

    @ParameterizedTest
    @MethodSource("providePriorityAndOldOrderStatusArgumentsForIdlePreparingReady")
    void whenManageOrderEntryThatDoesntExist_thenDoNothing(
            boolean priority, OrderStatus oldOrderStatus
    ) {
        Queue<OrderEntry> ordersQueue = getQueueFromPriorityAndStatus(priority, oldOrderStatus);
        order1.setOrderStatus(oldOrderStatus);
        order1.setPriority(priority);

        if (oldOrderStatus == OrderStatus.READY)
            when(ordersQueue.remove(any(OrderEntry.class))).thenReturn(false);
        else
            when(ordersQueue.contains(any(OrderEntry.class))).thenReturn(false);

        // act
        orderManagementService.manageOrder(order1);

        if (oldOrderStatus == OrderStatus.READY){
            verify(ordersQueue, times(1)).remove(any(OrderEntry.class));
            verify(ordersQueue, times(0)).contains(any(OrderEntry.class));
        } else {
            verify(ordersQueue, times(1)).contains(any(OrderEntry.class));
            verify(ordersQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(ordersQueue, times(0)).offer(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(ordersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenManageOrderWithStatusREADY_thenNewStatusPICKED_UP_andSentStatusUpdateThroughWS(boolean priority) {
        Queue<OrderEntry> readyOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.READY);
        order1.setOrderStatus(OrderStatus.READY);
        order1.setPriority(priority);
        updatedOrder1.setOrderStatus(OrderStatus.PICKED_UP);
        updatedOrder1.setPriority(priority);

        when(readyOrdersQueue.remove(any(OrderEntry.class))).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(updatedOrder1);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB
        //           remove from READY orders queue depending on priority
        //           send message through websockets notifying status update to PICKED_UP
        verify(readyOrdersQueue, times(1)).remove(any(OrderEntry.class));
        for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(readyOrdersQueue))) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.PICKED_UP);
    }

}
