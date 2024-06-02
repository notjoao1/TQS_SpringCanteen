package pt.ua.deti.springcanteen.frontend.digital_signage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import pt.ua.deti.springcanteen.dto.JwtAuthenticationResponseDTO;

public class DigitalSignageSteps {
  private static WebDriver driver;
  private static JavascriptExecutor js;
  private static Logger logger = LoggerFactory.getLogger(DigitalSignageSteps.class);

  // POST request to /api/auth/signup to create a cook employee
  static void createCookEmployee(RestTemplate restTemplate) {
    String postData =
        "{ \"username\": \"hellocook123\", \"password\": \"cook123\", \"email\":"
            + " \"testcook@gmail.com\", \"role\": \"COOK\"}";
    String url = "http://localhost/api/auth/signup";
    logger.info("Calling {} to create user...", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(postData, headers);
    logger.info("{}", postData);
    restTemplate.postForEntity(url, request, String.class);
    logger.info(
        "Successfully created a cook employee with credentials: email - testcook@gmail.com;"
            + " password - cook123");
  }

  // POST request to /api/auth/signup to create a desk orders employee
  static String createDeskOrdersEmployee(RestTemplate restTemplate) {
    String postData =
        "{ \"username\": \"deskorders123\", \"password\": \"deskorders123\", \"email\":"
            + " \"testdeskorders@gmail.com\", \"role\": \"DESK_ORDERS\"}";
    String url = "http://localhost/api/auth/signup";
    logger.info("Calling {} to create user...", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(postData, headers);
    logger.info("{}", postData);
    JwtAuthenticationResponseDTO res =
        restTemplate.postForEntity(url, request, JwtAuthenticationResponseDTO.class).getBody();
    logger.info(
        "Successfully created a cook employee with credentials: email - testdeskorders@gmail.com;"
            + " password - deskorders123");
    return res.getToken();
  }

  static void createOrder(RestTemplate restTemplate) {
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

    String url = "http://localhost/api/orders";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(orderRequest, headers);

    logger.info("Calling {} to create order...", url);
    restTemplate.postForEntity(url, request, String.class);
    logger.info("Successfully created a paid priority order...");
  }

  @BeforeAll
  public static void setup() {
    WebDriverManager.firefoxdriver().setup();
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    driver = new FirefoxDriver(options);
    js = (JavascriptExecutor) driver;
    // wait up to 10 seconds
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

    RestTemplate restTemplate = new RestTemplate();
    createCookEmployee(restTemplate);
    createDeskOrdersEmployee(restTemplate);	
    createOrder(restTemplate);
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

  @When("I submit my cook username and password")
  public void i_submit_username_and_password() {
    driver.findElement(By.id("email")).sendKeys("testcook@gmail.com");
    driver.findElement(By.id("password")).sendKeys("cook123");
  }

  @When("I submit my desk employee username and password")
  public void i_submit_my_desk_employee_username_and_password() {
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
    assertThat(actualPageText).isEqualTo("Welcome back Cook hellocook123!");
  }

  @When("I navigate to the cook orders page")
  public void i_navigate_to_the_cook_orders_page() {
    driver.get("http://localhost/employee/orders");
  }

  @And("I navigate to the Digital Signage page")
  public void i_navigate_to_the_digital_signage_page() {
    driver.get("http://localhost/signage/digital-signage");
  }

  @Then("I should find one order in the \"Preparing\" side")
  public void i_should_find_one_order_in_the_preparing_side() {

    assertThat(driver.findElement(By.id("preparing-1")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("delivery-1")).isDisplayed()).isFalse();
  }

  @Then("I should find one order in the \"Delivery\" side")
  public void i_should_find_one_order_in_the_delivery_side() {
    assertThat(driver.findElement(By.id("preparing-1")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("delivery-1")).isDisplayed()).isTrue();
  }

  @When("I navigate to the ready orders page")
  public void i_navigate_to_the_ready_orders_page() {
    driver.get("http://localhost/employee/ready_orders");
  }

  @And("I click the Confirm pick up button for the first ready order")
  public void i_click_the_confirm_pick_up_button_for_the_first_ready_order() {
    driver.findElement(By.id("priority-ready-button-1")).click();
  }

  @Then("I should not find any order")
  public void i_should_not_find_any_order() {
    assertThat(driver.findElement(By.id("preparing-1")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("delivery-1")).isDisplayed()).isFalse();
  }

}
