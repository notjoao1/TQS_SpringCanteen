package pt.ua.deti.springcanteen.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setup() {
        order1 = new Order();
        order1.setId(1L);

        updatedOrder1 = new Order();
        updatedOrder1.setId(1L);

        // this has to be done, since Mockito doesn't know where to inject what
        // using constructor injection, only field injection
        orderManagementService.setRegularIdleOrders(regularIdleOrders);
        orderManagementService.setPriorityIdleOrders(priorityIdleOrders);
        orderManagementService.setRegularPreparingOrders(regularPreparingOrders);
        orderManagementService.setPriorityPreparingOrders(priorityPreparingOrders);
        orderManagementService.setRegularReadyOrders(regularReadyOrders);
        orderManagementService.setPriorityReadyOrders(priorityReadyOrders);
    }

    @Test
    void whenManageOrderWithStatusNOT_PAID_thenNewStatusIDLE_andSentNewOrderThroughWS() {
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.IDLE);
        updatedOrder1.setPriority(false);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(regularIdleOrders.add(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB; add to regular IDLE orders queue;
        //           send message through websockets notifying new order exists;
        verify(regularIdleOrders, times(1)).add(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).add(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendNewOrder(any());
    }

    @Test
    void whenManageOrderWithStatusIDLE_thenNewStatusPREPARING_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.IDLE);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.PREPARING);
        updatedOrder1.setPriority(false);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(regularPreparingOrders.add(any())).thenReturn(true);
        when(regularIdleOrders.remove(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB; add to regular PREPARING orders queue;
        //           remove from regular IDLE orders queue;
        //           send message through websockets notifying status update to PREPARING
        verify(regularIdleOrders, times(1)).remove(any());
        verify(regularPreparingOrders, times(1)).add(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(priorityIdleOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).add(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.PREPARING);
    }

    @Test
    void whenManageOrderWithStatusPREPARING_thenNewStatusREADY_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.PREPARING);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.READY);
        updatedOrder1.setPriority(false);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(regularReadyOrders.add(any())).thenReturn(true);
        when(regularPreparingOrders.remove(any())).thenReturn(true);


        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB, add to regular READY orders queue
        //           remove from regular PREPARING orders queue
        //           send message through websockets notifying status update to READY
        verify(regularReadyOrders, times(1)).add(any());
        verify(regularPreparingOrders, times(1)).remove(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, priorityIdleOrders, priorityPreparingOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).add(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.READY);
    }

    @Test
    void whenManageOrderWithStatusREADY_thenNewStatusPICKED_UP_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.READY);
        order1.setPriority(false);
        updatedOrder1.setOrderStatus(OrderStatus.PICKED_UP);
        updatedOrder1.setPriority(false);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(regularReadyOrders.remove(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB
        //           remove from regular PREPARING orders queue
        //           send message through websockets notifying status update to PICKED_UP
        verify(regularReadyOrders, times(1)).remove(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, priorityIdleOrders, regularPreparingOrders, priorityPreparingOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).add(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, OrderStatus.READY);
    }

    @Test
    void whenManagePRIORITYOrderWithStatusNOT_READY_thenNewStatusIDLE_andSentStatusUpdateThroughWS() {
        order1.setOrderStatus(OrderStatus.NOT_PAID);
        order1.setPriority(true);
        updatedOrder1.setOrderStatus(OrderStatus.IDLE);
        updatedOrder1.setPriority(true);

        when(orderRepository.save(any())).thenReturn(updatedOrder1);
        when(priorityIdleOrders.add(any())).thenReturn(true);

        // act
        orderManagementService.manageOrder(order1);

        // should -> change order status and save it to DB
        //           add to priority IDLE orders queue
        //           send message through websockets notifying status update to IDLE
        verify(priorityIdleOrders, times(1)).add(any());
        List<Queue<OrderEntry>> unwantedCallQueues = List.of(regularIdleOrders, regularPreparingOrders, priorityPreparingOrders, regularReadyOrders, priorityReadyOrders);
        for (Queue<OrderEntry> unwantedQueue : unwantedCallQueues) {
            verify(unwantedQueue, times(0)).add(any());
            verify(unwantedQueue, times(0)).remove(any());
        }
        verify(orderRepository, times(1)).save(any());
        verify(orderNotifierService, times(1)).sendNewOrder(any());
    }
}
