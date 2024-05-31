package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.OrderMenuDTO;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.exceptions.InvalidStatusChangeException;
import pt.ua.deti.springcanteen.exceptions.QueueTransferException;
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IOrderService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);
  @Mock OrderRepository orderRepository;

  @Mock OrderMenuRepository orderMenuRepository;

  @Mock MenuService menuService;

  @Mock PriceService priceService;

  @Mock OrderManagementService orderManagementService;

  @InjectMocks IOrderService orderService;

  CustomizeOrderDTO customizeOrderDTO;
  OrderMenuDTO orderMenuDTO;
  Menu menu1, menu2;

  @BeforeEach
  void setup() {
    customizeOrderDTO = new CustomizeOrderDTO();
    orderMenuDTO = new OrderMenuDTO();

    menu1 = new Menu();
    menu1.setId(1L);
    menu1.setName("menu1");

    menu2 = new Menu();
    menu2.setId(2L);
    menu2.setName("menu2");
  }

  @FunctionalInterface
  interface OrderStatusChanger {
    Optional<Order> changeOrderStatus(Long orderId);
  }

  private Stream<Arguments> provideOrderStatusChangers() {
    return Stream.of(
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId)),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId))
    );
  }

  private Stream<Arguments> provideOrderStatusChangersAndNotAvailableOldOrderStatus() {
    return Stream.of(
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId), OrderStatus.IDLE),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId), OrderStatus.PREPARING),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId), OrderStatus.READY),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId), OrderStatus.PICKED_UP),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId), OrderStatus.NOT_PAID),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId), OrderStatus.PICKED_UP)
    );
  }

  private Stream<Arguments> provideOrderStatusChangersAndAvailableOldOrderStatus() {
    return Stream.of(
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changeNotPaidOrderToNextOrderStatus(orderId), OrderStatus.NOT_PAID),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId), OrderStatus.IDLE),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId), OrderStatus.PREPARING),
      Arguments.of((OrderStatusChanger) (Long orderId) -> orderService.changePaidOrderToNextOrderStatus(orderId), OrderStatus.READY)
    );
  }

  @Test
  void whenCreateValidPaidOrder_thenShouldBePaid_andStatusIdleAndInQueue() {
    // ordering menu 1, and already paid for it
    orderMenuDTO.setMenuId(1L);
    customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
    customizeOrderDTO.setIsPaid(true);
    customizeOrderDTO.setIsPriority(false);
    when(menuService.getMenuById(1L)).thenReturn(Optional.of(menu1));
    when(orderManagementService.addNewIdleOrder(any())).thenReturn(true);

    Optional<Order> orderOpt = orderService.createOrder(customizeOrderDTO);

    // assert order
    assertThat(orderOpt.isPresent(), is(true));
    assertThat(orderOpt.get().getOrderStatus(), is(OrderStatus.IDLE));
    assertThat(orderOpt.get().isPriority(), is(false));
    assertThat(orderOpt.get().isPaid(), is(true)); // paid
    verify(orderRepository, times(1)).save(any()); // was saved in DB
    verify(orderManagementService, times(1)).addNewIdleOrder(any()); // was saved in the order queue
    verify(orderManagementService, times(0)).manageOrder(any());
  }

  @Test
  void whenCreateValidUnpaidOrder_thenShouldBeUnpaid_andStatusNotPaid_andNotInQueue() {
    // ordering menu 1, and already paid for it
    orderMenuDTO.setMenuId(1L);
    customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
    customizeOrderDTO.setIsPaid(false);
    customizeOrderDTO.setIsPriority(true);
    when(menuService.getMenuById(1L)).thenReturn(Optional.of(menu1));
    when(priceService.getOrderMenuPrice(any())).thenReturn(3.0f);

    Optional<Order> orderOpt = orderService.createOrder(customizeOrderDTO);

    assertThat(orderOpt.isPresent(), is(true));
    assertThat(orderOpt.get().getOrderStatus(), is(OrderStatus.NOT_PAID));
    assertThat(orderOpt.get().isPriority(), is(true));
    assertThat(orderOpt.get().getPrice(), is(3.0f));
    assertThat(orderOpt.get().isPaid(), is(false)); // not paid
    verify(orderRepository, times(1)).save(any()); // was saved in DB
    verify(orderManagementService, times(0))
        .addNewIdleOrder(any()); // not was saved in the order queue
    verify(orderManagementService, times(0)).manageOrder(any());
  }

  @Test
  void whenCreateOrderWithInvalidMenus_thenShouldThrow() {
    // ordering menu 99, which doesnt exist
    orderMenuDTO.setMenuId(99L);
    customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
    customizeOrderDTO.setIsPaid(false);
    customizeOrderDTO.setIsPriority(true);
    when(menuService.getMenuById(99L)).thenReturn(Optional.empty());

    Exception actualException =
        assertThrows(
            InvalidOrderException.class, () -> orderService.createOrder(customizeOrderDTO));

    assertThat(
        actualException.getMessage(),
        containsString("Order has an invalid menu that does not exist with id '99'"));
    verify(orderRepository, times(0)).save(any());
  }

  @Test
  void whenCreateOrderWithTwoMenus_thenPriceShouldBeCorrectlyCalculated() {
    // ordering menu 1 and menu 2
    orderMenuDTO.setMenuId(1L);
    OrderMenuDTO orderMenuDTO2 = new OrderMenuDTO();
    orderMenuDTO2.setMenuId(2L);
    customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO, orderMenuDTO2));
    customizeOrderDTO.setIsPaid(false);
    customizeOrderDTO.setIsPriority(true);
    when(menuService.getMenuById(anyLong()))
        .thenAnswer(
            input -> {
              if ((Long) input.getArgument(0) == 1L) return Optional.of(menu1);
              return Optional.of(menu2);
            });
    // return 5€ * menuId as the price for each menu
    when(priceService.getOrderMenuPrice(any()))
        .thenAnswer(input -> ((OrderMenu) input.getArgument(0)).getMenu().getId() * 5.0f);

    Optional<Order> orderOpt = orderService.createOrder(customizeOrderDTO);

    assertThat(orderOpt.isPresent(), is(true));
    assertThat(orderOpt.get().getOrderMenus().size(), is(2));
    // 5€ from menu 1 and 10€ from menu 2
    assertThat(orderOpt.get().getPrice(), is(15.0f));
  }

  @ParameterizedTest
  @MethodSource("provideOrderStatusChangers")
  void whenChangeOrderThatDoesntExistToNextOrderStatus_thenReturnEmptyOptional(OrderStatusChanger changer){
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    Optional<Order> orderOpt = changer.changeOrderStatus(orderId);

    verify(orderRepository, times(1)).findById(orderId);
    verify(orderManagementService, times(0)).manageOrder(any(Order.class));
    Assertions.assertThat(orderOpt).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideOrderStatusChangersAndNotAvailableOldOrderStatus")
  void whenChangeOrderWithInvalidStatusToNextOrderStatus_thenThrowInvalidStatusChangeException(OrderStatusChanger changer, OrderStatus orderStatus){
    Long orderId = 1L;
    Order order = new Order(); order.setId(1L); order.setOrderStatus(orderStatus);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThrows(InvalidStatusChangeException.class, () -> changer.changeOrderStatus(orderId));

    verify(orderRepository, times(1)).findById(orderId);
    verify(orderManagementService, times(0)).manageOrder(any(Order.class));
  }

  @ParameterizedTest
  @MethodSource("provideOrderStatusChangersAndAvailableOldOrderStatus")
  void whenChangeOrderWithValidStatusToNextOrderStatusButQueueIsFull_thenThrowQueueTransferException(OrderStatusChanger changer, OrderStatus orderStatus){
    Long orderId = 1L;
    Order order = new Order(); order.setId(1L); order.setOrderStatus(orderStatus);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderManagementService.manageOrder(order)).thenReturn(false);

    assertThrows(QueueTransferException.class, () -> changer.changeOrderStatus(orderId));

    verify(orderRepository, times(1)).findById(orderId);
    verify(orderManagementService, times(1)).manageOrder(order);
  }

  @ParameterizedTest
  @MethodSource("provideOrderStatusChangersAndAvailableOldOrderStatus")
  void whenChangeOrderWithValidStatusToNextOrderStatusAndQueueNotFull_thenReturnOptionalOfOrder(OrderStatusChanger changer, OrderStatus orderStatus){
    Long orderId = 1L;
    Order order = new Order(); order.setId(1L); order.setOrderStatus(orderStatus);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderManagementService.manageOrder(order)).thenReturn(true);

    Optional<Order> orderOpt = changer.changeOrderStatus(orderId);

    verify(orderRepository, times(1)).findById(orderId);
    verify(orderManagementService, times(1)).manageOrder(order);
    Assertions.assertThat(orderOpt)
      .isPresent()
      .hasValue(order);
  }

}
