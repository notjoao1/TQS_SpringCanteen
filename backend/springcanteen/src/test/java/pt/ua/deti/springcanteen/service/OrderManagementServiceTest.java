package pt.ua.deti.springcanteen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pt.ua.deti.springcanteen.ConvertUtils.getMenuNamesFromOrder;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pt.ua.deti.springcanteen.ConvertUtils;
import pt.ua.deti.springcanteen.dto.OrderEntry;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IOrderManagementService;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {
  @Mock private Queue<OrderEntry> regularIdleOrders;
  @Mock private Queue<OrderEntry> priorityIdleOrders;
  @Mock private Queue<OrderEntry> regularPreparingOrders;
  @Mock private Queue<OrderEntry> priorityPreparingOrders;
  @Mock private Queue<OrderEntry> regularReadyOrders;
  @Mock private Queue<OrderEntry> priorityReadyOrders;

  @Mock OrderRepository orderRepository;
  @Mock OrderNotifierService orderNotifierService;

  @InjectMocks private IOrderManagementService orderManagementService;

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

  private static Stream<Arguments> providePriorityAndAllOrderStatusArgumentsForIdlePreparing() {
    return Stream.of(
        Arguments.of(true, OrderStatus.IDLE, OrderStatus.PREPARING),
        Arguments.of(false, OrderStatus.IDLE, OrderStatus.PREPARING),
        Arguments.of(true, OrderStatus.PREPARING, OrderStatus.READY),
        Arguments.of(false, OrderStatus.PREPARING, OrderStatus.READY));
  }

  private static Stream<Arguments>
      providePriorityAndOldOrderStatusArgumentsForIdlePreparingReady() {
    return Stream.of(
        Arguments.of(true, OrderStatus.IDLE),
        Arguments.of(false, OrderStatus.IDLE),
        Arguments.of(true, OrderStatus.PREPARING),
        Arguments.of(false, OrderStatus.PREPARING),
        Arguments.of(true, OrderStatus.READY),
        Arguments.of(false, OrderStatus.READY));
  }

  private static Stream<Arguments> provideAllPriorityAndOrderStatusArgumentsButIdle() {
    return Stream.of(
        Arguments.of(true, OrderStatus.NOT_PAID),
        Arguments.of(false, OrderStatus.NOT_PAID),
        Arguments.of(true, OrderStatus.PREPARING),
        Arguments.of(false, OrderStatus.PREPARING),
        Arguments.of(true, OrderStatus.READY),
        Arguments.of(false, OrderStatus.READY),
        Arguments.of(true, OrderStatus.PICKED_UP),
        Arguments.of(false, OrderStatus.PICKED_UP));
  }

  private Queue<OrderEntry> getQueueFromPriorityAndStatus(
      boolean priority, OrderStatus orderStatus) {
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
    return Stream.of(
            regularIdleOrders,
            priorityIdleOrders,
            regularPreparingOrders,
            priorityPreparingOrders,
            regularReadyOrders,
            priorityReadyOrders)
        .filter(queue -> !queues.contains(queue))
        .toList();
  }

  @ParameterizedTest
  @MethodSource("provideAllPriorityAndOrderStatusArgumentsButIdle")
  void whenAddNewIdleOrderWithNotIdleOrder_thenOrderNotAdded_andNotSentNewOrderThroughWS(
      boolean priority, OrderStatus orderStatus) {
    boolean result;
    order1.setOrderStatus(orderStatus);
    order1.setPriority(priority);

    // act
    result = orderManagementService.addNewIdleOrder(order1);

    // should -> add to IDLE orders queue depending on priority;
    //           send message through websockets notifying new order exists;
    assertFalse(result);
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of())) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderNotifierService, times(0)).sendNewOrder(any());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenQueueNotFullAndAddNewIdleOrder_thenIdleOrderAdded_andSentNewOrderThroughWS(
      boolean priority) {
    boolean result;
    Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
    order1.setOrderStatus(OrderStatus.IDLE);
    order1.setPriority(priority);

    when(idleOrdersQueue.offer(any())).thenReturn(true);

    // act
    result = orderManagementService.addNewIdleOrder(order1);

    // should -> add to IDLE orders queue depending on priority;
    //           send message through websockets notifying new order exists;
    assertTrue(result);
    verify(idleOrdersQueue, times(1)).offer(new OrderEntry(order1.getId(), order1.getOrderStatus()));
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(idleOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderNotifierService, times(1)).sendNewOrder(any());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenQueueFullAndAddNewIdleOrder_thenIdleOrderNotAdded_andNotSentNewOrderThroughWS(
      boolean priority) {
    boolean result;
    Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
    order1.setOrderStatus(OrderStatus.IDLE);
    order1.setPriority(priority);

    when(idleOrdersQueue.offer(any())).thenReturn(false);

    // act
    result = orderManagementService.addNewIdleOrder(order1);

    // should -> try but not add to IDLE orders queue;
    //           not send message through websockets notifying new order exists;
    assertFalse(result);
    verify(idleOrdersQueue, times(1)).offer(new OrderEntry(order1.getId(), order1.getOrderStatus()));
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(idleOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderNotifierService, times(0)).sendNewOrder(any());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void
      whenQueueNotFullAndManageOrderWithStatusNOT_PAID_thenOrderAdded_andNewStatusIDLE_andSentNewOrderThroughWS(
          boolean priority) {
    boolean result;
    Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
    order1.setOrderStatus(OrderStatus.NOT_PAID);
    order1.setPriority(priority);
    updatedOrder1.setOrderStatus(OrderStatus.IDLE);
    updatedOrder1.setPriority(priority);

    when(orderRepository.save(any())).thenReturn(updatedOrder1);
    when(idleOrdersQueue.offer(any())).thenReturn(true);

    // act
    result = orderManagementService.manageOrder(order1);

    // should -> change order status and save it to DB; add to IDLE orders queue depending on
    // priority;
    //           send message through websockets notifying new order exists;
    assertTrue(result);
    verify(idleOrdersQueue, times(1)).offer(argThat(
      orderEntry -> orderEntry.getId().equals(order1.getId()) && orderEntry.getOrderStatus() == OrderStatus.IDLE
    ));
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(idleOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderRepository, times(1)).save(argThat(
      order -> order.getId().equals(order1.getId()) && order.getOrderStatus() == OrderStatus.IDLE
    ));
    verify(orderNotifierService, times(1)).sendNewOrder(argThat(
      order -> order.getId().equals(order1.getId()) && order.getOrderStatus() == OrderStatus.IDLE
    ));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenQueueFullAndManageOrderWithStatusNOT_PAID_thenOrderNotAdded_andMessageNotSentThroughWS(
      boolean priority) {
    boolean result;
    Queue<OrderEntry> idleOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.IDLE);
    order1.setOrderStatus(OrderStatus.NOT_PAID);
    order1.setPriority(priority);

    when(idleOrdersQueue.offer(any())).thenReturn(false);

    // act
    result = orderManagementService.manageOrder(order1);

    // should -> not change order status and not save it to DB and not add to IDLE orders queue;
    //           not send message through websockets notifying new order exists;
    assertFalse(result);
    verify(idleOrdersQueue, times(1)).offer(argThat(
      orderEntry -> orderEntry.getId().equals(order1.getId()) && orderEntry.getOrderStatus() == OrderStatus.IDLE
    ));
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
  void
      whenQueueNotFullAndManageOrderWithCertainStatus_thenOrderAdded_andNewStatusSet_andSentStatusUpdateThroughWS(
          boolean priority, OrderStatus oldOrderStatus, OrderStatus newOrderStatus) {
    boolean result;
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
    result = orderManagementService.manageOrder(order1);

    // should -> change order status and save it to DB; add to next orders queue according to
    // priority;
    //           remove from old orders queue;
    //           send message through websockets notifying status update
    OrderEntry oldOrderEntry = new OrderEntry(order1.getId(), oldOrderStatus);
    assertTrue(result);
    verify(oldOrdersQueue, times(1)).contains(oldOrderEntry);
    verify(nextOrdersQueue, times(1)).offer(argThat(
      orderEntry -> orderEntry.getId().equals(order1.getId()) && orderEntry.getOrderStatus() == newOrderStatus
    ));
    verify(oldOrdersQueue, times(1)).remove(oldOrderEntry);
    for (Queue<OrderEntry> unwantedQueue :
        getUnwantedQueues(List.of(oldOrdersQueue, nextOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderRepository, times(1)).save(argThat(
      order -> order.getId().equals(order1.getId()) && order.getOrderStatus() == newOrderStatus
    ));
    verify(orderNotifierService, times(1)).sendOrderStatusUpdates(1L, newOrderStatus, priority);
  }

  @ParameterizedTest
  @MethodSource("providePriorityAndAllOrderStatusArgumentsForIdlePreparing")
  void
      whenQueueFullAndManageOrderWithWithCertainStatus_thenOrderNotAdded_andNotNewStatus_andNotSentStatusUpdateThroughWS(
          boolean priority, OrderStatus oldOrderStatus, OrderStatus newOrderStatus) {
    boolean result;
    Queue<OrderEntry> oldOrdersQueue = getQueueFromPriorityAndStatus(priority, oldOrderStatus);
    Queue<OrderEntry> nextOrdersQueue = getQueueFromPriorityAndStatus(priority, newOrderStatus);
    order1.setOrderStatus(oldOrderStatus);
    order1.setPriority(priority);
    updatedOrder1.setOrderStatus(newOrderStatus);
    updatedOrder1.setPriority(priority);

    when(oldOrdersQueue.contains(any(OrderEntry.class))).thenReturn(true);
    when(nextOrdersQueue.offer(any(OrderEntry.class))).thenReturn(false);

    // act
    result = orderManagementService.manageOrder(order1);

    // should -> not change order status and not save it to DB; not add to next orders queue
    // according to priority;
    //           stay in the same queue; not remove from old orders queue;
    //           not send message through websockets notifying status update
    OrderEntry oldOrderEntry = new OrderEntry(order1.getId(), oldOrderStatus);
    assertFalse(result);
    verify(oldOrdersQueue, times(1)).contains(oldOrderEntry);
    verify(nextOrdersQueue, times(1)).offer(argThat(
      orderEntry -> orderEntry.getId().equals(order1.getId()) && orderEntry.getOrderStatus() == newOrderStatus
    ));
    verify(oldOrdersQueue, times(0)).remove(any(OrderEntry.class));
    for (Queue<OrderEntry> unwantedQueue :
        getUnwantedQueues(List.of(oldOrdersQueue, nextOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderRepository, times(0)).save(any());
    verify(orderNotifierService, times(0))
        .sendOrderStatusUpdates(1L, OrderStatus.PREPARING, priority);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenManageOrderWithStatusREADY_thenNewStatusPICKED_UP_andSentStatusUpdateThroughWS(
      boolean priority) {
    boolean result;
    Queue<OrderEntry> readyOrdersQueue = getQueueFromPriorityAndStatus(priority, OrderStatus.READY);
    order1.setOrderStatus(OrderStatus.READY);
    order1.setPriority(priority);
    updatedOrder1.setOrderStatus(OrderStatus.PICKED_UP);
    updatedOrder1.setPriority(priority);

    when(readyOrdersQueue.remove(any(OrderEntry.class))).thenReturn(true);
    when(orderRepository.save(any())).thenReturn(updatedOrder1);

    // act
    result = orderManagementService.manageOrder(order1);

    // should -> change order status and save it to DB
    //           remove from READY orders queue depending on priority
    //           send message through websockets notifying status update to PICKED_UP
    assertTrue(result);
    verify(readyOrdersQueue, times(1)).remove(new OrderEntry(order1.getId(), OrderStatus.READY));
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(readyOrdersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(orderRepository, times(1)).save(argThat(
      order -> order.getId().equals(order1.getId()) && order.getOrderStatus() == OrderStatus.PICKED_UP
    ));
    verify(orderNotifierService, times(1))
        .sendOrderStatusUpdates(1L, OrderStatus.PICKED_UP, priority);
  }

  @ParameterizedTest
  @MethodSource("providePriorityAndOldOrderStatusArgumentsForIdlePreparingReady")
  void whenManageOrderEntryThatDoesntExist_thenDoNothing(
      boolean priority, OrderStatus oldOrderStatus) {
    boolean result;
    Queue<OrderEntry> ordersQueue = getQueueFromPriorityAndStatus(priority, oldOrderStatus);
    order1.setOrderStatus(oldOrderStatus);
    order1.setPriority(priority);

    if (oldOrderStatus == OrderStatus.READY)
      when(ordersQueue.remove(any(OrderEntry.class))).thenReturn(false);
    else when(ordersQueue.contains(any(OrderEntry.class))).thenReturn(false);

    // act
    result = orderManagementService.manageOrder(order1);

    assertFalse(result);
    if (oldOrderStatus == OrderStatus.READY) {
      verify(ordersQueue, times(1)).remove(new OrderEntry(order1.getId(), oldOrderStatus));
      verify(ordersQueue, times(0)).contains(any(OrderEntry.class));
    } else {
      verify(ordersQueue, times(1)).contains(new OrderEntry(order1.getId(), oldOrderStatus));
      verify(ordersQueue, times(0)).remove(any(OrderEntry.class));
    }
    verify(ordersQueue, times(0)).offer(any(OrderEntry.class));
    for (Queue<OrderEntry> unwantedQueue : getUnwantedQueues(List.of(ordersQueue))) {
      verify(unwantedQueue, times(0)).contains(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).offer(any(OrderEntry.class));
      verify(unwantedQueue, times(0)).remove(any(OrderEntry.class));
    }
  }

  @Test
  void whenGetAllOrders_thenReturnAllOrders() {
    // test getAllOrders + convertToOrderList
    // setup
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
    Order order2 =
        new Order(
            2L, OrderStatus.IDLE, true, 10.0f, false, null, null, Set.of(orderMenu1, orderMenu2));
    Order order3 =
        new Order(3L, OrderStatus.IDLE, true, 5.0f, false, null, null, Set.of(orderMenu3));
    Order order4 =
        new Order(4L, OrderStatus.IDLE, true, 5.0f, true, null, null, Set.of(orderMenu2));
    Order order5 =
        new Order(5L, OrderStatus.PREPARING, true, 5.0f, false, null, null, Set.of(orderMenu3));
    Order order6 =
        new Order(
            6L,
            OrderStatus.PREPARING,
            true,
            5.0f,
            true,
            null,
            null,
            Set.of(orderMenu1, orderMenu2));
    Order order7 =
        new Order(
            7L,
            OrderStatus.PREPARING,
            true,
            5.0f,
            true,
            null,
            null,
            Set.of(orderMenu2, orderMenu4));
    Order order8 =
        new Order(8L, OrderStatus.READY, true, 5.0f, false, null, null, Set.of(orderMenu3));
    Order order9 =
        new Order(
            9L, OrderStatus.READY, true, 5.0f, true, null, null, Set.of(orderMenu3, orderMenu4));
    Stream.of(order2, order3, order4, order5, order6, order7, order8, order9)
        .forEach(
            order -> when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order)));
    when(regularIdleOrders.stream())
        .thenReturn(
            Stream.of(OrderEntry.fromOrderEntity(order2), OrderEntry.fromOrderEntity(order3)));
    when(priorityIdleOrders.stream()).thenReturn(Stream.of(OrderEntry.fromOrderEntity(order4)));
    when(regularPreparingOrders.stream()).thenReturn(Stream.of(OrderEntry.fromOrderEntity(order5)));
    when(priorityPreparingOrders.stream())
        .thenReturn(
            Stream.of(OrderEntry.fromOrderEntity(order6), OrderEntry.fromOrderEntity(order7)));
    when(regularReadyOrders.stream()).thenReturn(Stream.of(OrderEntry.fromOrderEntity(order8)));
    when(priorityReadyOrders.stream()).thenReturn(Stream.of(OrderEntry.fromOrderEntity(order9)));

    // act
    QueueOrdersDTO result = orderManagementService.getAllOrders();

    // assert
    assertThat(result.getRegularIdleOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(
            tuple(order2.getId(), getMenuNamesFromOrder(order2)),
            tuple(order3.getId(), getMenuNamesFromOrder(order3)));
    assertThat(result.getPriorityIdleOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(tuple(order4.getId(), getMenuNamesFromOrder(order4)));
    assertThat(result.getRegularPreparingOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(tuple(order5.getId(), getMenuNamesFromOrder(order5)));
    assertThat(result.getPriorityPreparingOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(
            tuple(order6.getId(), getMenuNamesFromOrder(order6)),
            tuple(order7.getId(), getMenuNamesFromOrder(order7)));
    assertThat(result.getRegularReadyOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(tuple(order8.getId(), getMenuNamesFromOrder(order8)));
    assertThat(result.getPriorityReadyOrders())
        .extracting(OrderCookResponseDTO::getId, ConvertUtils::getMenuNamesFromDTO)
        .containsExactly(tuple(order9.getId(), getMenuNamesFromOrder(order9)));
  }
}
