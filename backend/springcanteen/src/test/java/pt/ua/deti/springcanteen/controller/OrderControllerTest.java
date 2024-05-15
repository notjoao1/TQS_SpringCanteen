package pt.ua.deti.springcanteen.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.deti.springcanteen.controllers.OrderController;
import pt.ua.deti.springcanteen.service.impl.IOrderService;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @BeforeEach
    void setup() {

    }


}
