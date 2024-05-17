package pt.ua.deti.springcanteen.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
class OrderControllerIT {
    static final Logger logger = LoggerFactory.getLogger(OrderControllerIT.class);
    
    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("testname")
            .withPassword("testpassword")
            .withDatabaseName("sc_test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    @LocalServerPort
    int serverPort;

    @BeforeEach
    void setup() {
        RestAssured.port = serverPort;
    }

    @Test
    void whenCreateOrderSuccessfully_thenReturnCorrectResponse() {
        // order with drink as Lemonade (2€) + main dish as 'Sandwich' (4.5€) with 1 extra ham (+2.5€)
        String orderRequest = "{" +
        "    \"kiosk_id\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menu_id\": 1," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 8" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 1," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 1," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 3," +
        "                            \"quantity\": 2" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 4," +
        "                            \"quantity\": 2" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";        
        
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest)
        .when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_CREATED)
                .and()
            .body("order_menus.size()", is(1))
                .and()
            .body("order_menus[0].name", is("Sandwich & Drink"))
                .and()
            .body("order_menus[0].price", is(9f));
    }

    @Test
    void whenCreateOrder_with2Menus_thenReturnCorrectResponse() {
        // order for 2 menus - 'lunch menu' and 'breakfast menu'
        //      lunch menu has main dish 'Potato chips with Beef and lettuce' (8€) with 0 potatoes and drink 'Water' (1€)
        //      breakfast menu has main dish 'Pancakes' (1.5f) and drink 'Orange Juice' (2€)
        String orderRequest = "{" +
        "    \"kiosk_id\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menu_id\": 2," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 4" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 2," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 3," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 5," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 6," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 7," +
        "                            \"quantity\": 1" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }," +
        "        {" +
        "            \"menu_id\": 2," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 6" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 3," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 5," +
        "                            \"quantity\": 2" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 6," +
        "                            \"quantity\": 2" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";

        // expect 2 menus, first one with price (8€ + 1€), second one w ith price (1.50€ + 2€)
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest)
        .when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_CREATED)
                .and()
            .body("order_menus.size()", is(2))
                .and()
            .body("order_menus.name", containsInAnyOrder("Potato chips with beef and lettuce", "Pancakes"))
                .and()
            .body("order_menus.price", containsInAnyOrder(9.0f, 3.5f));
    }

    @Test
    void whenCreateOrder_withInvalidMenu_shouldFailWithStatus422() {
        // ordering a menu with id 99
        String orderRequest = "{" +
        "    \"kiosk_id\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menu_id\": 99," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 2" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 1," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 1," +
        "                            \"quantity\": 4" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 2," +
        "                            \"quantity\": 3" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";        
        
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest)
        .when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .and()
            .body(is(emptyOrNullString()));
    }

    @Test
    void whenCreateOrder_withInvalidIngredientsForMenu_shouldFailWithStatus422() {
        // order for menu 1 with main dish 'Beef with Rice', but with an ingredient
        // id 99, which doesn't exist
        String orderRequest = "{" +
        "    \"kiosk_id\": 1," +
        "    \"isPaid\": false," +
        "    \"isPriority\": false," +
        "    \"nif\": \"123456789\"," +
        "    \"orderMenus\": [" +
        "        {" +
        "            \"menu_id\": 1," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 4" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 1," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 99," +
        "                            \"quantity\": 4" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 2," +
        "                            \"quantity\": 3" +
        "                        }" +
        "                    ]" +
        "                }" +
        "            }" +
        "        }" +
        "    ]" +
        "}";        
        
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest).when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .and()
            .body(is(emptyOrNullString()));

    }
}
