package pt.ua.deti.springcanteen.frontend.employee_desk_payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import org.springframework.web.client.RestTemplate;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DeskPaymentsEmployeeSteps {
  private static WebDriver driver;
  private static JavascriptExecutor js;
  private static Wait<WebDriver> wait;
  private static Logger logger = LoggerFactory.getLogger(DeskPaymentsEmployeeSteps.class);
  private static final String BASE_HOSTNAME = "localhost";
  private int numberNotPaidOrders = 0;

  // POST request to /api/auth/signup to create a desk payments employee
  static void createDeskPaymentsEmployee(RestTemplate restTemplate) {
    String postData = "{ \"username\": \"payments123\", \"password\": \"payments123\", \"email\":"
        + " \"testpayments@gmail.com\", \"role\": \"DESK_PAYMENTS\"}";
    String url = String.format("http://%s/api/auth/signup", BASE_HOSTNAME);
    logger.info("Calling {} to create user...", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(postData, headers);
    logger.info("{}", postData);
    restTemplate.postForEntity(url, request, String.class);
    logger.info(
        "Successfully created a desk payments employee with credentials: email - testpayments@gmail.com;"
            + " password - payments123");
  }

  static void createOrder(RestTemplate restTemplate) {
    // create not paid order
    String orderRequest = "{"
        + "    \"kioskId\": 1,"
        + "    \"isPaid\": false,"
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

    String url = String.format("http://%s/api/orders", BASE_HOSTNAME);

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
    // setup wait
    wait = new WebDriverWait(driver, Duration.ofSeconds(3));

    RestTemplate restTemplate = new RestTemplate();
    createDeskPaymentsEmployee(restTemplate);
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

  @When("I submit username and password")
  public void i_submit_username_and_password() {
    driver.findElement(By.id("email")).sendKeys("testpayments@gmail.com");
    driver.findElement(By.id("password")).sendKeys("payments123");
  }

  @And("I click the sign in button")
  public void i_click_the_sign_in_button() {
    driver.findElement(By.id("signin-button")).click();
  }

  @Then("I should be logged in")
  public void i_should_be_logged_in() {
    String actualPageText = driver.findElement(By.id("welcome-back-text")).getText();
    assertThat(actualPageText).isEqualTo("Welcome back, payments123, get back to handling Desk Payments,");
  }

  @When("I navigate to the Desk payments page")
  public void i_navigate_to_the_desk_payments_page() {
    driver.get("http://localhost/employee/payments");
  }

  @Then("I should see the existing not yet paid orders")
  public void i_should_see_the_existing_not_yet_paid_orders() {
    // get children elements
    List<WebElement> notPaidOrders = driver.findElement(By.cssSelector(".MuiTableBody-root"))
        .findElements(By.cssSelector(".MuiTableRow-root"));
    assertThat(notPaidOrders.size()).isNotZero();
  }

  @And("I click the 'Request Payment' button for the first not yet paid order")
  public void i_click_the_request_payment_button_for_the_first_not_yet_paid_order() {
    // before paying, get number of unpaid orders for later
    List<WebElement> notPaidOrders = driver.findElement(By.cssSelector(".MuiTableBody-root"))
        .findElements(By.cssSelector(".MuiTableRow-root"));
    numberNotPaidOrders = notPaidOrders.size();
    driver.findElement(By.id("not-paid-order-request-pay-1")).click();
  }

  @And("I click the 'Confirm' button")
  public void i_click_the_confirm_button() {
    wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.id("confirm-payment-button")));
    driver.findElement(By.id("confirm-payment-button")).click();
  }

  @Then("I should see the confirmation snackbar")
  public void then_i_should_see_the_confirmation_snackbar() {
    wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"notistack-snackbar\"]")));
    assertThat(driver.findElement(By.xpath("//*[@id=\"notistack-snackbar\"]")).isDisplayed())
        .isTrue();
  }

  @And("the desk payments table should have one less order to be paid")
  public void the_desk_payments_table_should_have_one_less_order_to_be_paid() {
    List<WebElement> notPaidOrders = driver.findElement(By.cssSelector(".MuiTableBody-root"))
        .findElements(By.cssSelector(".MuiTableRow-root"));
    assertThat(notPaidOrders).hasSize(numberNotPaidOrders - 1);
  }

}
