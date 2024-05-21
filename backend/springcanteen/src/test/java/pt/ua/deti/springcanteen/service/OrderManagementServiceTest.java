package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.service.impl.IOrderManagementService;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {
    @Mock
    private Queue<Order> regularOrders;

    @Mock
    private Queue<Order> priorityOrders;

    @InjectMocks
    private IOrderManagementService orderManagementService;

    private Order order1;

    @BeforeEach
    void setup() {
        order1 = new Order();
    }

    @Test
    void whenAddPriorityOrder_thenOrderAddedToPriorityQueue() {
        order1.setPriority(true);

        orderManagementService.manageOrder(order1);

        verify(priorityOrders, times(1)).add(order1);
        verify(regularOrders, times(0)).add(any());
    }

    @Test
    void whenAddRegularOrder_thenOrderAddedToRegularQueue() {
        order1.setPriority(false);

        orderManagementService.manageOrder(order1);

        verify(regularOrders, times(1)).add(order1);
        verify(priorityOrders, times(0)).add(any());
    }
}
