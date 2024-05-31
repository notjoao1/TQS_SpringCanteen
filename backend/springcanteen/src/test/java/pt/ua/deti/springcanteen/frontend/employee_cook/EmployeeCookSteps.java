package pt.ua.deti.springcanteen.frontend.employee_cook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;

public class EmployeeCookSteps {
  private static WebDriver driver;
  private static Wait<WebDriver> wait;
  private static Logger logger = LoggerFactory.getLogger(EmployeeCookSteps.class);

  // POST request to /api/auth/signup to create a cook employee
  static void createCookEmployee(RestTemplate restTemplate) {
    String postData = "{ \"username\": \"hellocook123\", \"password\": \"cook123\", \"email\": \"testcook@gmail.com\", \"role\": \"COOK\"}";
    String url = "http://localhost:8080/api/auth/signup";
    logger.info("Calling {} to create user...", url);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(postData, headers);
    logger.info("{}", postData);
    restTemplate.postForEntity(url, request, String.class);
    logger.info("Successfully created a cook employee with credentials: email - testcook@gmail.com; password - cook123");
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

    String url = "http://localhost:8080/api/orders";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(orderRequest, headers);

    logger.info("Calling {} to create order...", url);
    restTemplate.postForEntity(url, request, String.class);
    logger.info("Successfully created a paid priority order...");
  }

  @BeforeAll
  public static void setupEmployeeAndOrder() {
    //WebDriverManager.firefoxdriver().setup();
    WebDriverManager.chromedriver().setup();
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    //driver = new FirefoxDriver(options);
    driver = new ChromeDriver();
    // wait up to 10 seconds
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    // setup wait
    wait = new WebDriverWait(driver, Duration.ofSeconds(3));

    RestTemplate restTemplate = new RestTemplate();
    createCookEmployee(restTemplate);
    createOrder(restTemplate);
  }


  @Given("I navigate to the sign in page")
  public void i_navigate_to_the_sign_in_page() {
    driver.get("http://localhost:5173/signin");
  }

  @When("I submit username and password")
  public void i_submit_username_and_password() {
    driver.findElement(By.id("email")).sendKeys("testcook@gmail.com");
    driver.findElement(By.id("password")).sendKeys("cook123");
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
    driver.get("http://localhost:5173/employee/orders");
  }

  @Then("I should see the single existing priority idle order")
  public void i_should_see_the_single_existing_priority_idle_order() {
    // at this point, there should be 1 priority idle order, which was created at the start of this file
    assertThat(driver.findElement(By.id("priority-idle-order-1")).isDisplayed()).isTrue();
  }

}
