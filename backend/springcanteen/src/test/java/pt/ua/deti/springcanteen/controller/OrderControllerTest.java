package pt.ua.deti.springcanteen.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import pt.ua.deti.springcanteen.controllers.OrderController;
import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.Ingredient;
import pt.ua.deti.springcanteen.entities.MainDish;
import pt.ua.deti.springcanteen.entities.MainDishIngredients;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.Order;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.entities.OrderStatus;
import pt.ua.deti.springcanteen.service.EmployeeService;
import pt.ua.deti.springcanteen.service.JwtService;
import pt.ua.deti.springcanteen.service.impl.IOrderService;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest{
    @Autowired
    MockMvc mockMvc;
    @MockBean
    JwtService jwtService;
    @MockBean
    EmployeeService employeeService;

    @MockBean
    private IOrderService orderService;

    Menu menu1, menu2;
    Drink drink1, drink2, drink3;
    MainDish mainDish1, mainDish2, mainDish3;
    Ingredient ingredient1, ingredient2, ingredient3, ingredient4, ingredient5, ingredient6;

    @BeforeEach
    void setup() {
        // mainDish 1 -> Rice with Beef
        ingredient1 = new Ingredient(1L, "Rice", 0.5f, 100);
        ingredient2 = new Ingredient(2L, "Beef", 2.5f, 200);

        // mainDish 2 -> Potato chips with Beef and lettuce
        ingredient3 = new Ingredient(3L, "Potato", 0.5f, 60);
        ingredient4 = new Ingredient(4L, "Lettuce", 0.25f, 10);

        // mainDish 3 ->  Pancakes
        ingredient5 = new Ingredient(5L, "Flour", 0.25f, 20);
        ingredient6 = new Ingredient(6L, "Egg", 0.5f, 100);

        drink1 = new Drink();
        drink1.setId(1L);
        drink1.setName("Water");
        drink1.setPrice(1.0f);

        drink2 = new Drink();
        drink2.setId(2L);
        drink2.setName("Coke");
        drink2.setPrice(3.0f);

        drink3 = new Drink();
        drink3.setId(3L);
        drink3.setName("Orange Juice");
        drink3.setPrice(2.0f);

        // menu 1:  2 drink options -> Water/Coke
        //          2 main dish options -> Rice with Beef/Potato chips with beef and lettuce
        menu1 = new Menu();
        menu1.setId(1L);
        menu1.setName("Lunch Menu");

        // menu 2:  2 drink options -> Water/Orange Juice
        //          1 main dish option -> Pancakes
        menu2 = new Menu();
        menu2.setId(2L);
        menu2.setName("Breakfast Menu");

        Set<Drink> drinkMenu1 = new HashSet<>();
        drinkMenu1.add(drink1);
        drinkMenu1.add(drink2);

        Set<Drink> drinkMenu2 = new HashSet<>();
        drinkMenu2.add(drink1);
        drinkMenu2.add(drink3);

        menu1.setDrinkOptions(drinkMenu1);
        menu2.setDrinkOptions(drinkMenu2);

        mainDish1 = new MainDish();
        mainDish1.setId(1L);
        mainDish1.setName("Rice with Beef");
        mainDish1.setPrice(9.0f);

        mainDish2 = new MainDish();
        mainDish2.setId(2L);
        mainDish2.setName("Potato chips with beef and lettuce");
        mainDish2.setPrice(10.0f);

        mainDish3 = new MainDish();
        mainDish3.setId(3L);
        mainDish3.setName("Pancakes");
        mainDish3.setPrice(3.0f);

        // ingredients for Rice with beef
        Set<MainDishIngredients> mainDishIngredients1 = new HashSet<>();
        mainDishIngredients1.add(new MainDishIngredients(1L, mainDish1, ingredient1, 4));
        mainDishIngredients1.add(new MainDishIngredients(2L, mainDish1, ingredient2, 2));

        // ingredients for Potato chips with beef and lettuce
        Set<MainDishIngredients> mainDishIngredients2 = new HashSet<>();
        mainDishIngredients2.add(new MainDishIngredients(3L, mainDish2, ingredient3, 4));
        mainDishIngredients2.add(new MainDishIngredients(4L, mainDish2, ingredient2, 2));
        mainDishIngredients2.add(new MainDishIngredients(5L, mainDish2, ingredient4, 4));

        // ingredients for Pancakes
        Set<MainDishIngredients> mainDishIngredients3 = new HashSet<>();
        mainDishIngredients3.add(new MainDishIngredients(6L, mainDish3, ingredient5, 2));
        mainDishIngredients3.add(new MainDishIngredients(7L, mainDish3, ingredient6, 2));
    
        mainDish1.setMainDishIngredients(mainDishIngredients1);
        mainDish2.setMainDishIngredients(mainDishIngredients2);
        mainDish3.setMainDishIngredients(mainDishIngredients3);

        Set<MainDish> mainDishMenu1 = new HashSet<>();
        mainDishMenu1.add(mainDish1);
        mainDishMenu1.add(mainDish2);

        Set<MainDish> mainDishMenu2 = new HashSet<>();
        mainDishMenu2.add(mainDish3);

        menu1.setMainDishOptions(mainDishMenu1);
        menu2.setMainDishOptions(mainDishMenu2);
    }

    @Test
    void whenCreateOrderSuccessfully_thenReturnCorrectResponse() {
        OrderMenu orderMenu1 = new OrderMenu(null, menu1, null);
        orderMenu1.setCalculatedPrice(13.5f);
        Optional<Order> returnOrder = Optional.of(new Order(1L, OrderStatus.IDLE, false, orderMenu1.getCalculatedPrice(), false, "123456789", null, Set.of(orderMenu1)));
        when(orderService.createOrder(any())).thenReturn(returnOrder);

        // order with drink as Coke (3€) + main dish as 'Rice with Beef' (9€) with 1 extra beef (1.5€)
        String orderRequest = "{" +
        "    \"kioskId\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menuId\": 1," +
        "            \"customization\": {" +
        "                \"customizedDrink\": {" +
        "                    \"itemId\": 2" +
        "                }," +
        "                \"customizedMainDish\": {" +
        "                    \"itemId\": 1," +
        "                    \"customizedIngredients\": [" +
        "                        {" +
        "                            \"ingredientId\": 1," +
        "                            \"quantity\": 4" +
        "                        }," +
        "                        {" +
        "                            \"ingredientId\": 2," +
        "                            \"quantity\": 3" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";        
        
        RestAssuredMockMvc
            .given()
                .mockMvc(mockMvc)
                    .contentType(ContentType.JSON)
                    .body(orderRequest).when()
                    .post("api/orders")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                        .and()
                    .body("orderMenus.size()", is(1))
                        .and()
                    .body("price", is(orderMenu1.getCalculatedPrice()))
                        .and()
                    .body("orderMenus[0].menu.name", is(menu1.getName()))
                        .and()
                    .body("orderMenus[0].menu.price", is(13.5f));
    }

    @Test
    void whenCreateOrder_with2Menus_thenReturnCorrectResponse() {
        OrderMenu orderMenu1 = new OrderMenu(null, menu1, null);
        orderMenu1.setCalculatedPrice(11.0f);
        OrderMenu orderMenu2 = new OrderMenu(null, menu2, null);
        orderMenu2.setCalculatedPrice(5.0f);
        Optional<Order> returnOrder = Optional.of(new Order(1L, OrderStatus.IDLE, false, orderMenu1.getCalculatedPrice() + orderMenu2.getCalculatedPrice(), false, "123456789", null, Set.of(orderMenu1, orderMenu2)));
        when(orderService.createOrder(any())).thenReturn(returnOrder);

        // order for 2 menus - 'lunch menu' and 'breakfast menu'
        //      lunch menu has main dish 'Potato chips with Beef and lettuce' (10€) with 0 potatoes and drink 'Water' (1€)
        //      breakfast menu has main dish 'Pancakes' (3€) and drink 'Orange Juice' (2€)
        String orderRequest = "{" +
        "    \"kioskId\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menuId\": 1," +
        "            \"customization\": {" +
        "                \"customizedDrink\": {" +
        "                    \"itemId\": 1" +
        "                }," +
        "                \"customizedMainDish\": {" +
        "                    \"itemId\": 2," +
        "                    \"customizedIngredients\": [" +
        "                        {" +
        "                            \"ingredientId\": 3," +
        "                            \"quantity\": 0" +
        "                        }," +
        "                        {" +
        "                            \"ingredientId\": 4," +
        "                            \"quantity\": 2" +
        "                        }," +
        "                        {" +
        "                            \"ingredientId\": 55," +
        "                            \"quantity\": 4" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }," +
        "        {" +
        "            \"menuId\": 2," +
        "            \"customization\": {" +
        "                \"customizedDrink\": {" +
        "                    \"itemId\": 3" +
        "                }," +
        "                \"customizedMainDish\": {" +
        "                    \"itemId\": 3," +
        "                    \"customizedIngredients\": [" +
        "                        {" +
        "                            \"ingredientId\": 5," +
        "                            \"quantity\": 2" +
        "                        }," +
        "                        {" +
        "                            \"ingredientId\": 6," +
        "                            \"quantity\": 2" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";

        // expect 2 menus, first one with price (10€ + 1€), second one w ith price (3€ + 2€)
        RestAssuredMockMvc
            .given()
                .mockMvc(mockMvc)
                    .contentType(ContentType.JSON)
                    .body(orderRequest).when()
                    .post("api/orders")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                        .and()
                    .body("orderMenus.size()", is(2))
                        .and()
                    .body("price", is(orderMenu1.getCalculatedPrice() + orderMenu2.getCalculatedPrice()))
                        .and()
                    .body("orderMenus.menu.name", containsInAnyOrder(menu1.getName(), menu2.getName()))
                        .and()
                    .body("orderMenus.menu.price", containsInAnyOrder(11.0f, 5.0f));
    }
}
