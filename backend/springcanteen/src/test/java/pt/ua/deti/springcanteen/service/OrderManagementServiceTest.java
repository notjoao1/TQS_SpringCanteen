package pt.ua.deti.springcanteen.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
        logger.info("runs2");
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


    private static Stream<Arguments> provideQueueAndPriorityArguments(){
        return Stream.of(
                Arguments.of(true),
                Arguments.of(false)
        );
    }

    private Queue<OrderEntry> getOldQueueFromPriorityAndStatus(boolean priority, OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PREPARING -> priority ? priorityIdleOrders : regularIdleOrders;
            case READY -> priority ? priorityPreparingOrders : regularPreparingOrders;
            default -> {
                logger.info("Invalid Order Status {}", orderStatus);
                throw new IllegalArgumentException("Invalid Order Status");
            }
        };
    }

    private Queue<OrderEntry> getNextQueueFromPriorityAndStatus(boolean priority, OrderStatus orderStatus) {
        return switch (orderStatus) {
            case IDLE -> priority ? priorityPreparingOrders : regularPreparingOrders;
            case PREPARING -> priority ? priorityReadyOrders : regularReadyOrders;
            default -> {
                logger.info("Invalid Order Status {}", orderStatus);
                throw new IllegalArgumentException("Invalid Order Status");
            }
        };
    }

    @ParameterizedTest
    @MethodSource("provideQueueAndPriorityArguments")
    void whenQueueNotFullAndManageOrderWithStatusNOT_PAID_thenOrderAdded_andNewStatusIDLE_andSentNewOrderThroughWS(
            boolean priority
    ) {
        Queue<OrderEntry> idleOrdersQueue = priority ? priorityIdleOrders : regularIdleOrders;
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(priority);
        updatedOrder1.setOrderStatus(OrderStatus.IDLE);
        updatedOrder1.setPriority(priority);

        logger.info("{}", priority);
        logger.info("{}", idleOrdersQueue); // this is null
        logger.info("{}", regularIdleOrders.size()); // this isn't
        logger.info("{}", orderManagementService);


        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(idleOrdersQueue.offer(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB; offer to regular IDLE orders queue;
        //           send message through websockets notifying new order exists;
        verify(idleOrdersQueue, times(1)).offer(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendNewOrder(any());
    }

    @Test
    void whenQueueFullAndManageOrderWithStatusNOT_PAID_thenOrderNotAdded_andMessageNotSentThroughWS() {
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(false);

        when(regularIdleOrders.offer(any())).thenReturn(false);

        // act
        orderManagementService.manageOrder(order1);

        // should -> not change order status and not save it to DB and not offer to IDLE orders queue;
        //           not send message through websockets notifying new order exists;
        verify(regularIdleOrders, times(1)).offer(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(0)).save(any());
        verify(orderNotifierService, times(0)).sendNewOrder(any());
    }

    @Test
    void whenQueueNotFullAndManageOrderWithStatusIDLE_thenOrderAdded_andNewStatusPREPARING_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.IDLE);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.PREPARING);
        updatedOrder1.setPriority(false);

        when(regularIdleOrders.contains(any(OrderEntry.class))).thenReturn(true);
        when(regularPreparingOrders.offer(any(OrderEntry.class))).thenReturn(true);
        when(regularIdleOrders.remove(any(OrderEntry.class))).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(updatedOrder1);

        // act
        orderManagementService.manageOrder(order1);

        // should ->
        //           change order status and save it to DB; add to regular PREPARING orders queue;
        //           remove from regular IDLE orders queue;
        //           send message through websockets notifying status update to PREPARING
        verify(regularIdleOrders, times(1)).contains(any(OrderEntry.class));
        verify(regularPreparingOrders, times(1)).offer(any(OrderEntry.class));
        verify(regularIdleOrders, times(1)).remove(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(priorityIdleOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.PREPARING);
    }

    @Test
    void whenQueueFullAndManageOrderWithStatusIDLE_thenOrderAdded_andNewStatusPREPARING_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.IDLE);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.PREPARING);
        updatedOrder1.setPriority(false);

        when(regularIdleOrders.contains(any(OrderEntry.class))).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should ->
        //           change order status and save it to DB; add to regular PREPARING orders queue;
        //           remove from regular IDLE orders queue;
        //           send message through websockets notifying status update to PREPARING
        verify(regularIdleOrders, times(1)).contains(any(OrderEntry.class));
        verify(regularPreparingOrders, times(1)).offer(any(OrderEntry.class));
        verify(regularIdleOrders, times(0)).remove(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(priorityIdleOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(0)).save(any());
        verify(orderNotifierService, times(0)).sendOrderStatusUpdates(1L, OrderStatus.PREPARING);
    }

    @Test
    void whenQueueNotFullAndManageOrderWithStatusPREPARING_thenNewStatusREADY_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.PREPARING);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.READY);
        updatedOrder1.setPriority(false);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(regularPreparingOrders.contains(any(OrderEntry.class))).thenReturn(true);
        when(regularReadyOrders.offer(any(OrderEntry.class))).thenReturn(true);
        when(regularPreparingOrders.remove(any(OrderEntry.class))).thenReturn(true);


        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB, add to regular READY orders queue
        //           remove from regular PREPARING orders queue
        //           send message through websockets notifying status update to READY
        verify(regularPreparingOrders, times(1)).contains(any(OrderEntry.class));
        verify(regularReadyOrders, times(1)).offer(any(OrderEntry.class));
        verify(regularPreparingOrders, times(1)).remove(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, priorityIdleOrders, priorityPreparingOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.READY);
    }

    @Test
    void whenQueueFullAndManageOrderWithStatusPREPARING_thenNewStatusREADY_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.PREPARING);
        order1.setPriority(false);

        when(regularPreparingOrders.contains(any(OrderEntry.class))).thenReturn(false);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB, add to regular READY orders queue
        //           remove from regular PREPARING orders queue
        //           send message through websockets notifying status update to READY
        verify(regularPreparingOrders, times(1)).contains(any(OrderEntry.class));
        verify(regularReadyOrders, times(0)).offer(any(OrderEntry.class));
        verify(regularPreparingOrders, times(0)).remove(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, priorityIdleOrders, priorityPreparingOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(0)).save(any());
        verify(orderNotifierService, times(0)).sendOrderStatusUpdates(1L, OrderStatus.READY);
    }

    @Test
    void whenManageOrderWithStatusREADY_thenNewStatusPICKED_UP_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.READY);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.PICKED_UP);
        updatedOrder1.setPriority(false);

        when(regularReadyOrders.remove(any(OrderEntry.class))).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(updatedOrder1);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB
        //           remove from regular PREPARING orders queue
        //           send message through websockets notifying status update to PICKED_UP
        verify(regularReadyOrders, times(1)).remove(any(OrderEntry.class));
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
            verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.PICKED_UP);
    }

    @Test
    void whenManagePRIORITYOrderWithStatusNOT_READY_thenNewStatusIDLE_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(true);
        updatedOrder1.setOrderStatus(OrderStatus.IDLE);
        updatedOrder1.setPriority(true);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(priorityIdleOrders.offer(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB
        //           add to priority IDLE orders queue
        //           send message through websockets notifying status update to IDLE
        verify(priorityIdleOrders, times(1)).offer(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).offer(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendNewOrder(any());
    }
}
