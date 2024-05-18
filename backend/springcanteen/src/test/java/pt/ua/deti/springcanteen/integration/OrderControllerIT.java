package pt.ua.deti.springcanteen.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.persistence.EntityManager;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
class OrderControllerIT {
    @Autowired
    EntityManager entityManager;
    
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
        // order with drink as Lemonade (2€) + main dish as 'Sandwich' (6€) with 1 extra ham (+2.5€)
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
            .body("orderMenus.size()", is(1))
                .and()
            .body("orderMenus[0].menu.name", is("Sandwich & Drink"))
                .and()
            .body("orderMenus[0].menu.price", is(10.5f));
    }

    @Test
    void whenCreateOrder_with2Menus_thenReturnCorrectResponse() {
        // order for 2 menus - 'Russian Salad & Water' and 'Veggie Wrap'
        //      Russian Salad & Water has main dish 'Russian Salad' (3.5€) with 0 eggs and drink 'Water' (1.2€)
        //      Veggie Wrap has main dish 'Veggie Wrap' (2.9€) and drink 'Orange Juice' (3€)
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
        "                    \"item_id\": 9" +
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
        "                            \"quantity\": 0" +
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
        "            \"menu_id\": 4," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 7" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 4," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 2," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 3," +
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
        "        }" +
        "    ]" +
        "}";

        // expect 2 menus, first one with price (3.5€ + 1.2€), second one w ith price (2.9€ + 3€)
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest)
        .when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_CREATED)
                .and()
            .body("orderMenus.size()", is(2))
                .and()
            .body("orderMenus.menu.name", containsInAnyOrder("Russian Salad & Water", "Veggie Wrap"))
                .and()
            .body("orderMenus.menu.price", containsInAnyOrder(4.7f, 5.9f));
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
            .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    void whenCreateOrder_with1ValidAnd1InvalidMenu_shouldFailWithStatus422() {
        // ordering a menu with id 99
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
        "                    \"item_id\": 9" +
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
        "                            \"quantity\": 0" +
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
        "            \"menu_id\": 99," +
        "            \"customization\": {" +
        "                \"customized_drink\": {" +
        "                    \"item_id\": 7" +
        "                }," +
        "                \"customized_main_dish\": {" +
        "                    \"item_id\": 4," +
        "                    \"customized_ingredients\": [" +
        "                        {" +
        "                            \"ingredient_id\": 2," +
        "                            \"quantity\": 1" +
        "                        }," +
        "                        {" +
        "                            \"ingredient_id\": 3," +
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
        "        }" +
        "    ]" +
        "}";
        
        given()
            .contentType(ContentType.JSON)
            .body(orderRequest)
        .when()
            .post("api/orders")
        .then()
            .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
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
            .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }
}
