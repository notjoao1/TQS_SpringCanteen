package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.OrderMenuDTO;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.repositories.OrderMenuRepository;
import pt.ua.deti.springcanteen.repositories.OrderRepository;
import pt.ua.deti.springcanteen.service.impl.IOrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMenuRepository orderMenuRepository;

    @Mock
    MenuService menuService;

    @Mock
    PriceService priceService;

    @InjectMocks
    IOrderService orderService;

    CustomizeOrderDTO customizeOrderDTO;
    OrderMenuDTO orderMenuDTO;
    Menu menu1;

    @BeforeEach
    void setup() {
        customizeOrderDTO = new CustomizeOrderDTO();
        orderMenuDTO = new OrderMenuDTO();

        menu1 = new Menu();
        menu1.setId(1L);
        menu1.setName("menu1");
    }

    @Test
    void whenCreateValidPaidOrder_thenShouldBePaid_andStatusIdle() {
        // ordering menu 1, and already paid for it
        orderMenuDTO.setMenu_id(1L);
        customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
        customizeOrderDTO.setIsPaid(true);
        customizeOrderDTO.setIsPriority(false);
        when(menuService.getMenuById(1L)).thenReturn(Optional.of(menu1));

        Optional<Order> orderOpt = orderService.createOrder(customizeOrderDTO);
        
        assertThat(orderOpt.isPresent(), is(true));
        assertThat(orderOpt.get().getOrderStatus(), is(OrderStatus.IDLE));
        assertThat(orderOpt.get().isPriority(), is(false));
        assertThat(orderOpt.get().isPaid(), is(true)); // paid
    }


    @Test
    void whenCreateValidUnpaidOrder_thenShouldBeUnpaid_andStatusIdle() {
        // ordering menu 1, and already paid for it
        orderMenuDTO.setMenu_id(1L);
        customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
        customizeOrderDTO.setIsPaid(false);
        customizeOrderDTO.setIsPriority(true);
        when(menuService.getMenuById(1L)).thenReturn(Optional.of(menu1));

        Optional<Order> orderOpt = orderService.createOrder(customizeOrderDTO);
        
        assertThat(orderOpt.isPresent(), is(true));
        assertThat(orderOpt.get().getOrderStatus(), is(OrderStatus.IDLE));
        assertThat(orderOpt.get().isPriority(), is(true));
        assertThat(orderOpt.get().isPaid(), is(false)); // not paid
    }

    @Test
    void whenCreateOrderWithInvalidMenus_thenShouldThrow() {
        // ordering menu 2, which doesnt exist
        orderMenuDTO.setMenu_id(2L);
        customizeOrderDTO.setOrderMenus(Set.of(orderMenuDTO));
        customizeOrderDTO.setIsPaid(false);
        customizeOrderDTO.setIsPriority(true);
        when(menuService.getMenuById(2L)).thenReturn(Optional.empty());

        Exception actualException = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(customizeOrderDTO));

        assertThat(actualException.getMessage(), containsString("Order has an invalid menu that does not exist with id '2'"));
    }
}
