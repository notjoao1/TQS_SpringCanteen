package pt.ua.deti.springcanteen.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import pt.ua.deti.springcanteen.ConvertUtils;
import pt.ua.deti.springcanteen.dto.QueueOrdersDTO;
import pt.ua.deti.springcanteen.dto.response.cookresponse.OrderCookResponseDTO;
import pt.ua.deti.springcanteen.service.OrderManagementService;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
class OrderControllerIT {

  @Container
  public static PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:latest")
          .withUsername("testname")
          .withPassword("testpassword")
          .withDatabaseName("sc_test");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.password", container::getPassword);
    registry.add("spring.datasource.username", container::getUsername);
  }

  @SpyBean OrderManagementService orderManagementServiceSpy;

  @LocalServerPort int serverPort;

  private static final int QUEUE_CAPACITY = 120;

  @BeforeEach
  void setup() {
    RestAssured.port = serverPort;
    ReflectionTestUtils.setField(
        orderManagementServiceSpy, "regularIdleOrders", new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    ReflectionTestUtils.setField(
        orderManagementServiceSpy, "priorityIdleOrders", new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    ReflectionTestUtils.setField(
        orderManagementServiceSpy,
        "regularPreparingOrders",
        new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    ReflectionTestUtils.setField(
        orderManagementServiceSpy,
        "priorityPreparingOrders",
        new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    ReflectionTestUtils.setField(
        orderManagementServiceSpy, "regularReadyOrders", new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    ReflectionTestUtils.setField(
        orderManagementServiceSpy, "priorityReadyOrders", new ArrayBlockingQueue<>(QUEUE_CAPACITY));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenCreateUnpaidOrderSuccessfully_thenReturnCorrectResponse_AndNotAddToIdleQueue(
      boolean priority) {
    // order with drink as Lemonade (2€) + main dish as 'Sandwich' (3€) with 1 extra ham (+2.5€)
    String orderRequest =
        String.format(
            "{"
                + "    \"kioskId\": 1,"
                + "    \"isPaid\": false,"
                + "    \"isPriority\": %b,"
                + "    \"nif\": \"123456789\","
                + "    \"orderMenus\": ["
                + "        {"
                + "            \"menuId\": 1,"
                + "            \"customization\": {"
                + "                \"customizedDrink\": {"
                + "                    \"itemId\": 8"
                + "                },"
                + "                \"customizedMainDish\": {"
                + "                    \"itemId\": 1,"
                + "                    \"customizedIngredients\": ["
                + "                        {"
                + "                            \"ingredientId\": 1,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 3,"
                + "                            \"quantity\": 2"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 4,"
                + "                            \"quantity\": 2"
                + "                        }"
                + "                    ]"
                + "                }"
                + "            }"
                + "        }"
                + "    ]"
                + "}",
            priority);

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
        .body("orderMenus[0].menu.price", is(7.5f));

    verify(orderManagementServiceSpy, times(0)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    assertThat(
            List.of(
                allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders()))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenCreatePaidOrderSuccessfully_thenReturnCorrectResponse_AndAddToIdleQueue(
      boolean priority) {
    // order with drink as Lemonade (2€) + main dish as 'Sandwich' (3€) with 1 extra ham (+2.5€)
    String orderRequest =
        String.format(
            "{"
                + "    \"kioskId\": 1,"
                + "    \"isPaid\": true,"
                + "    \"isPriority\": %b,"
                + "    \"nif\": \"123456789\","
                + "    \"orderMenus\": ["
                + "        {"
                + "            \"menuId\": 1,"
                + "            \"customization\": {"
                + "                \"customizedDrink\": {"
                + "                    \"itemId\": 8"
                + "                },"
                + "                \"customizedMainDish\": {"
                + "                    \"itemId\": 1,"
                + "                    \"customizedIngredients\": ["
                + "                        {"
                + "                            \"ingredientId\": 1,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 3,"
                + "                            \"quantity\": 2"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 4,"
                + "                            \"quantity\": 2"
                + "                        }"
                + "                    ]"
                + "                }"
                + "            }"
                + "        }"
                + "    ]"
                + "}",
            priority);

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
        .body("orderMenus[0].menu.price", is(7.5f));

    verify(orderManagementServiceSpy, times(1)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    List<OrderCookResponseDTO> orderQueueCookResponseDTO =
        priority ? allOrders.getPriorityIdleOrders() : allOrders.getRegularIdleOrders();
    assertThat(orderQueueCookResponseDTO)
        .extracting(
            (OrderCookResponseDTO order) -> order.getOrderMenus().size(),
            ConvertUtils::getMenuNamesFromDTO,
            ConvertUtils::getMenuPricesFromDTO)
        .containsExactly(tuple(1, List.of("Sandwich & Drink"), List.of(7.5f)));
    assertThat(
            Stream.of(
                    allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                    allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                    allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders())
                .filter(
                    orderCookResponseDTOS -> orderCookResponseDTOS != orderQueueCookResponseDTO))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenCreatePaidOrder_with2Menus_thenReturnCorrectResponse(boolean priority) {
    // order for 2 menus - 'Russian Salad & Water' and 'Veggie Wrap'
    //      Russian Salad & Water has main dish 'Russian Salad' (4.0€) with 0 eggs and drink 'Water'
    // (1.2€)
    //      Veggie Wrap has main dish 'Veggie Wrap' (3.5€) and drink 'Orange Juice' (3€)
    String orderRequest =
        String.format(
            "{"
                + "    \"kioskId\": 1,"
                + "    \"isPaid\": true,"
                + "    \"isPriority\": %s,"
                + "    \"nif\": \"123456789\","
                + "    \"orderMenus\": ["
                + "        {"
                + "            \"menuId\": 2,"
                + "            \"customization\": {"
                + "                \"customizedDrink\": {"
                + "                    \"itemId\": 9"
                + "                },"
                + "                \"customizedMainDish\": {"
                + "                    \"itemId\": 2,"
                + "                    \"customizedIngredients\": ["
                + "                        {"
                + "                            \"ingredientId\": 3,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 5,"
                + "                            \"quantity\": 0"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 6,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 7,"
                + "                            \"quantity\": 1"
                + "                        }"
                + "                    ]"
                + "                }"
                + "            }"
                + "        },"
                + "        {"
                + "            \"menuId\": 4,"
                + "            \"customization\": {"
                + "                \"customizedDrink\": {"
                + "                    \"itemId\": 7"
                + "                },"
                + "                \"customizedMainDish\": {"
                + "                    \"itemId\": 4,"
                + "                    \"customizedIngredients\": ["
                + "                        {"
                + "                            \"ingredientId\": 2,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 3,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 6,"
                + "                            \"quantity\": 1"
                + "                        },"
                + "                        {"
                + "                            \"ingredientId\": 7,"
                + "                            \"quantity\": 1"
                + "                        }"
                + "                    ]"
                + "                }"
                + "            }"
                + "        }"
                + "    ]"
                + "}",
            priority);

    // expect 2 menus, first one with price (4.0€ + 1.2€), second one with price (3.5€ + 3€)
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
        .body("price", is(11.7f))
        .and()
        .body("orderMenus.menu.name", containsInAnyOrder("Russian Salad & Water", "Veggie Wrap"))
        .and()
        .body("orderMenus.menu.price", containsInAnyOrder(5.2f, 6.5f));

    verify(orderManagementServiceSpy, times(1)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    List<OrderCookResponseDTO> orderQueueCookResponseDTO =
        priority ? allOrders.getPriorityIdleOrders() : allOrders.getRegularIdleOrders();
    assertThat(orderQueueCookResponseDTO)
        .extracting(
            (OrderCookResponseDTO order) -> order.getOrderMenus().size(),
            ConvertUtils::getMenuNamesFromDTO,
            ConvertUtils::getMenuPricesFromDTO)
        .containsExactly(
            tuple(2, List.of("Russian Salad & Water", "Veggie Wrap"), List.of(5.2f, 6.5f)));
    assertThat(
            Stream.of(
                    allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                    allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                    allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders())
                .filter(
                    orderCookResponseDTOS -> orderCookResponseDTOS != orderQueueCookResponseDTO))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }

  @Test
  void whenCreateOrder_withInvalidMenu_shouldFailWithStatus422_AndNotAddToIdleQueue() {
    // ordering a menu with id 99
    String orderRequest =
        "{"
            + "    \"kioskId\": 1,"
            + "    \"isPaid\": false,"
            + "    \"isPriority\": false,"
            + "    \"nif\": \"123456789\","
            + "    \"orderMenus\": ["
            + "        {"
            + "            \"menuId\": 99,"
            + "            \"customization\": {"
            + "                \"customizedDrink\": {"
            + "                    \"itemId\": 2"
            + "                },"
            + "                \"customizedMainDish\": {"
            + "                    \"itemId\": 1,"
            + "                    \"customizedIngredients\": ["
            + "                        {"
            + "                            \"ingredientId\": 1,"
            + "                            \"quantity\": 4"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 2,"
            + "                            \"quantity\": 3"
            + "                        }"
            + "                    ]"
            + "                }"
            + "            }"
            + "        }"
            + "    ]"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(orderRequest)
        .when()
        .post("api/orders")
        .then()
        .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    verify(orderManagementServiceSpy, times(0)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    assertThat(
            List.of(
                allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders()))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }

  @Test
  void whenCreateOrder_with1ValidAnd1InvalidMenu_shouldFailWithStatus422_AndNotAddToIdleQueue() {
    // ordering a menu with id 99
    String orderRequest =
        "{"
            + "    \"kioskId\": 1,"
            + "    \"isPaid\": false,"
            + "    \"isPriority\": false,"
            + "    \"nif\": \"123456789\","
            + "    \"orderMenus\": ["
            + "        {"
            + "            \"menuId\": 2,"
            + "            \"customization\": {"
            + "                \"customizedDrink\": {"
            + "                    \"itemId\": 9"
            + "                },"
            + "                \"customizedMainDish\": {"
            + "                    \"itemId\": 2,"
            + "                    \"customizedIngredients\": ["
            + "                        {"
            + "                            \"ingredientId\": 3,"
            + "                            \"quantity\": 1"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 5,"
            + "                            \"quantity\": 0"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 6,"
            + "                            \"quantity\": 1"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 7,"
            + "                            \"quantity\": 1"
            + "                        }"
            + "                    ]"
            + "                }"
            + "            }"
            + "        },"
            + "        {"
            + "            \"menuId\": 99,"
            + "            \"customization\": {"
            + "                \"customizedDrink\": {"
            + "                    \"itemId\": 7"
            + "                },"
            + "                \"customizedMainDish\": {"
            + "                    \"itemId\": 4,"
            + "                    \"customizedIngredients\": ["
            + "                        {"
            + "                            \"ingredientId\": 2,"
            + "                            \"quantity\": 1"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 3,"
            + "                            \"quantity\": 1"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 6,"
            + "                            \"quantity\": 1"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 7,"
            + "                            \"quantity\": 1"
            + "                        }"
            + "                    ]"
            + "                }"
            + "            }"
            + "        }"
            + "    ]"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(orderRequest)
        .when()
        .post("api/orders")
        .then()
        .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    verify(orderManagementServiceSpy, times(0)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    assertThat(
            List.of(
                allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders()))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }

  @Test
  void
      whenCreateOrder_withInvalidIngredientsForMenu_shouldFailWithStatus422_AndNotAddToIdleQueue() {
    // order for menu 1 with main dish 'Beef with Rice', but with an ingredient
    // id 99, which doesn't exist
    String orderRequest =
        "{"
            + "    \"kioskId\": 1,"
            + "    \"isPaid\": false,"
            + "    \"isPriority\": false,"
            + "    \"nif\": \"123456789\","
            + "    \"orderMenus\": ["
            + "        {"
            + "            \"menuId\": 1,"
            + "            \"customization\": {"
            + "                \"customizedDrink\": {"
            + "                    \"itemId\": 4"
            + "                },"
            + "                \"customizedMainDish\": {"
            + "                    \"itemId\": 1,"
            + "                    \"customizedIngredients\": ["
            + "                        {"
            + "                            \"ingredientId\": 99,"
            + "                            \"quantity\": 4"
            + "                        },"
            + "                        {"
            + "                            \"ingredientId\": 2,"
            + "                            \"quantity\": 3"
            + "                        }"
            + "                    ]"
            + "                }"
            + "            }"
            + "        }"
            + "    ]"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(orderRequest)
        .when()
        .post("api/orders")
        .then()
        .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    verify(orderManagementServiceSpy, times(0)).addNewIdleOrder(any());
    QueueOrdersDTO allOrders = orderManagementServiceSpy.getAllOrders();
    assertThat(allOrders).isNotNull();
    assertThat(
            List.of(
                allOrders.getRegularIdleOrders(), allOrders.getPriorityIdleOrders(),
                allOrders.getRegularPreparingOrders(), allOrders.getPriorityPreparingOrders(),
                allOrders.getRegularReadyOrders(), allOrders.getPriorityReadyOrders()))
        .isNotEmpty()
        .allSatisfy(queue -> assertThat(queue).isNotNull().isEmpty());
  }
}
