package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.service.impl.IQueueNotifierService;

/*
 * Need to initialize Spring Boot context, since it handles passing events
 * that occur (we will simulate a websocket event in this test class)
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class QueueNotifierServiceTest {
    @Mock
    SimpMessagingTemplate websocketClient;

    @Mock
    OrderManagementService orderManagementService;

    @InjectMocks
    IQueueNotifierService queueNotifierService;

    OrderCookResponseDTO orderCookResponseDTORegular, orderCookResponseDTOPriority;

    Order order1, order2;

    OrderMenu orderMenu1, orderMenu2, orderMenu3;

    Menu menu1, menu2, menu3;
    

    @BeforeEach
    void setup() {
        menu1 = new Menu(1L, "Beef with Rice", null, null, null, null);
        menu2 = new Menu(2L, "Vegan Rissois", null, null, null, null);
        menu3 = new Menu(3L, "Meat of pig Alentejana style", null, null, null, null);
        orderMenu1 = new OrderMenu(null, menu1, "{}");
        orderMenu2 = new OrderMenu(null, menu2, "{}");
        orderMenu3 = new OrderMenu(null, menu3, "{}");
        order1  = new Order(1L, OrderStatus.IDLE, false, 10.0f, false, null, null, Set.of(orderMenu1, orderMenu2));
        order2 = new Order(2L, OrderStatus.IDLE, true, 5.0f, false, null, null, Set.of(orderMenu3));
        orderCookResponseDTORegular = OrderCookResponseDTO.fromOrderEntity(order1);
        orderCookResponseDTOPriority = OrderCookResponseDTO.fromOrderEntity(order2);
    
    }

    // TODO: Work this out later
//    @Test
//    void whenSubscribeToTopic_thenSendExistingIdleOrders() {
//        when(orderManagementService.getAllIdleOrders()).thenReturn(new QueueOrdersDTO(
//                List.of(orderCookResponseDTORegular), List.of(orderCookResponseDTOPriority))
//        );
//        // setup
//        SessionSubscribeEvent event = new SessionSubscribeEvent(this.getClass(), new MockMessage());
//
//        // act
//        queueNotifierService.sendExistingOrderQueues(event);
//
//        ArgumentCaptor<QueueOrdersDTO> payloadCaptor = ArgumentCaptor.forClass(QueueOrdersDTO.class);
//        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
//
//        verify(websocketClient).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
//
//        assertThat(destinationCaptor.getValue(), is("/topic/orders"));
//        QueueOrdersDTO sentOrders = payloadCaptor.getValue();
//        assertThat(sentOrders.getRegularOrders().size(), is(1));
//        // 2 menus in the regular order
//        assertThat(sentOrders.getRegularOrders().get(0).getOrderMenus().size(), is(2));
//        assertThat(sentOrders.getRegularOrders().get(0).getId(), is(1L));
//        assertThat(sentOrders.getPriorityOrders().size(), is(1));
//        // 1 menu in the priority order
//        assertThat(sentOrders.getPriorityOrders().get(0).getId(), is(2L));
//        assertThat(sentOrders.getPriorityOrders().get(0).getOrderMenus().size(), is(1));
//    }


    private class MockMessage implements Message<byte[]> {

        @Override
        public byte[] getPayload() {
            return "this doesn't matter".getBytes();
        }

        @Override
        public MessageHeaders getHeaders() {
            return null;
        }
    
        
    }
}
