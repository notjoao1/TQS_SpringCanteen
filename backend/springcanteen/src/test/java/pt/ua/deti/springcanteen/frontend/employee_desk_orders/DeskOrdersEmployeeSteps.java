package pt.ua.deti.springcanteen.frontend.employee_desk_orders;

import static org.assertj.core.api.Assertions.assertThat;
import static pt.ua.deti.springcanteen.integration.WebsocketUtils.connectAsyncWithHeaders;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import pt.ua.deti.springcanteen.dto.JwtAuthenticationResponseDTO;
import pt.ua.deti.springcanteen.dto.OrderUpdateRequestDTO;
import pt.ua.deti.springcanteen.dto.response.clientresponse.OrderClientResponseDTO;

public class DeskOrdersEmployeeSteps {
  private static WebDriver driver;
  private static JavascriptExecutor js;
  private static Wait<WebDriver> wait;
  private static Logger logger = LoggerFactory.getLogger(DeskOrdersEmployeeSteps.class);
  private static final String BASE_BACKEND_URL = "localhost";

  static void setupOrderAndEmployee(RestTemplate restTemplate) throws Exception {
    String jwt = createDeskOrdersEmployee(restTemplate);
    long orderId = createOrder(restTemplate);
    updateOrderToReady(orderId, jwt);
  }

  // POST request to /api/auth/signup to create a desk orders employee
  static String createDeskOrdersEmployee(RestTemplate restTemplate) {
    String postData =
        "{ \"username\": \"deskorders123\", \"password\": \"deskorders123\", \"email\":"
            + " \"testdeskorders@gmail.com\", \"role\": \"DESK_ORDERS\"}";
    String url = String.format("http://%s/api/auth/signup", BASE_BACKEND_URL);
    logger.info("Calling {} to create user...", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(postData, headers);
    logger.info("{}", postData);
    JwtAuthenticationResponseDTO res = restTemplate.postForEntity(url, request, JwtAuthenticationResponseDTO.class).getBody();
    logger.info(
        "Successfully created a cook employee with credentials: email - testdeskorders@gmail.com;"
            + " password - deskorders123");
    return res.getToken();
  }

  static long createOrder(RestTemplate restTemplate) throws Exception {
    String orderRequest =
        "{"
            + "    \"kioskId\": 1,"
            + "    \"isPaid\": true,"
            + "    \"isPriority\": true,"
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
            + "}";

    String url = String.format("http://%s/api/orders", BASE_BACKEND_URL);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(orderRequest, headers);

    logger.info("Calling {} to create order...", url);
    OrderClientResponseDTO response = restTemplate.postForEntity(url, request, OrderClientResponseDTO.class).getBody();
    logger.info("Successfully created a paid priority order...");
    return response.getId();
  }

  static void updateOrderToReady(long orderId, String jwt) throws InterruptedException, ExecutionException, TimeoutException {

    WebSocketClient client = new StandardWebSocketClient();
    WebSocketStompClient stompClient = new WebSocketStompClient(client);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    String url = String.format("ws://%s/websocket", BASE_BACKEND_URL);
    logger.info("Updating order status to READY...");

    StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
          OrderUpdateRequestDTO payload = new OrderUpdateRequestDTO();
          payload.setOrderId(orderId);
          logger.info("Sending payload: {}", payload);
          session.send("/app/order_updates", payload);
          session.send("/app/order_updates", payload);
          logger.info("Payload for updating orders sent...");
        }
    };

    StompHeaders stompHeaders = new StompHeaders();
    stompHeaders.set("Authorization", "Bearer " + jwt);

    connectAsyncWithHeaders(url, stompClient, stompHeaders);
    stompClient.connectAsync(url, sessionHandler).get(10, TimeUnit.SECONDS);

  }

  @BeforeAll
  public static void setup() throws Exception {
    WebDriverManager.firefoxdriver().setup();
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    driver = new FirefoxDriver(options);
    js = (JavascriptExecutor) driver;
    // wait up to 10 seconds
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    // setup wait
    wait = new WebDriverWait(driver, Duration.ofSeconds(3));

    RestTemplate restTemplate = new RestTemplate();
    setupOrderAndEmployee(restTemplate);
  }

  

  @Given("I have a clean local storage")
  public void i_have_a_clean_local_storage() {
    driver.get("http://localhost/");
    js.executeScript("window.localStorage.clear()");
  }

  @And("I navigate to the sign in page")
  public void i_navigate_to_the_sign_in_page() {
    driver.get("http://localhost/signin");
  }

  @When("I submit username and password")
  public void i_submit_username_and_password() {
    driver.findElement(By.id("email")).sendKeys("testdeskorders@gmail.com");
    driver.findElement(By.id("password")).sendKeys("deskorders123");
  }

  @And("I click the sign in button")
  public void i_click_the_sign_in_button() {
    driver.findElement(By.id("signin-button")).click();
  }

  @Then("I should be logged in")
  public void i_should_be_logged_in() {
    String actualPageText = driver.findElement(By.id("welcome-back-text")).getText();
    assertThat(actualPageText).isEqualTo("Welcome back, deskorders123, get back to confirming ready orders,");
  }

  @When("I navigate to the ready orders page")
  public void i_navigate_to_the_ready_orders_page() {
    driver.get("http://localhost/employee/ready_orders");
  }

  @Then("I should see the single existing priority ready order")
  public void i_should_see_the_single_existing_priority_ready_order() {
    assertThat(driver.findElement(By.id("priority-ready-order-1")).isDisplayed()).isTrue();
  }

  @And("I click the Confirm pick up button for the first ready order")
  public void i_click_the_confirm_pick_up_button_for_the_first_ready_order() {
    driver.findElement(By.id("priority-ready-button-1")).click();
  }

  @Then("I should see the confirmation snackbar")
  public void i_should_see_the_confirmation_snackbar() {
    wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"notistack-snackbar\"]")));
    assertThat(driver.findElement(By.xpath("//*[@id=\"notistack-snackbar\"]")).isDisplayed())
        .isTrue();  
  }

}
