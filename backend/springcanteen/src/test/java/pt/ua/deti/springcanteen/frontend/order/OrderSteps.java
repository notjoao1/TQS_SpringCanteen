package pt.ua.deti.springcanteen.frontend.order;

import io.cucumber.java.en.Then;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

public class OrderSteps {
  private WebDriver driver;
  private Wait<WebDriver> wait;

  @When("I navigate to {string}")
  public void i_navigate_to(String url) {
    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    options.setCapability("goog:loggingPrefs", logPrefs);
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    driver.get(url);
    wait = new WebDriverWait(driver, Duration.ofSeconds(3));
  }

  // changed the id in frontend, continue...
  @And("I select the menu number {string}")
  public void i_select_menu(String number) {
    driver.findElement(By.id("add-menu-" + number)).click();
  }

  @And("I select the Main Dish number {string}")
  public void i_select_main_dish(String number) {
    driver.findElement(By.id("select-main-dish")).click();
    driver.findElement(By.id("main-dish-" + number)).click();
  }

  @And("I select the Drink number {string}")
  public void i_select_drink(String number) {
    driver.findElement(By.id("select-drink")).click();
    driver.findElement(By.id("drink-" + number)).click();
  }

  @And("I click on \"Confirm selection\"")
  public void i_click_confirm() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-selection")));
    WebElement confirmSelection =
        wait.until(ExpectedConditions.elementToBeClickable(By.id("confirm-selection")));

    Actions actions = new Actions(driver);
    actions.moveToElement(confirmSelection).click().perform();
  }

  @And("I click to \"View order\"")
  public void i_click_view_order() {
    driver.findElement(By.id("view-order")).click();
  }

  @And("I click on \"Cancel selection\"")
  public void i_click_cancel() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cancel-selection")));
    WebElement cancelSelection =
        wait.until(ExpectedConditions.elementToBeClickable(By.id("cancel-selection")));

    Actions actions = new Actions(driver);
    actions.moveToElement(cancelSelection).click().perform();
  }

  @And("I remove the menu number {string}")
  public void i_remove_menu(String number) {
    driver.findElement(By.id("remove-menu-" + number)).click();
  }

  @Then("I should see the message \"Successfully added menu to order.\"")
  public void i_should_see_message() {
    captureConsoleLogs();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.id("snackbar-add-menu-to-order"), "Successfully added menu to order."));
    assertThat(
        driver.findElement(By.id("snackbar-add-menu-to-order")).getText(),
        containsString("Successfully added menu to order."));
    driver.quit();
  }

  @Then("I should see the {int} menus added to the order")
  public void i_should_see_order(int number) {
    if (number == 0) {
      assertThat(driver.findElements(By.id("order-drawer-item-1")).size(), is(0));
    } else {
      // checks that order-drawer-item-1 and order-drawer-item-2 are visible
      for (int i = 1; i <= number; i++) {
        assertThat(driver.findElement(By.id("order-drawer-item-" + i)).isDisplayed(), is(true));
      }
    }
    driver.quit();
  }

  @And("I click on \"Customize and pay\"")
  public void i_click_on_customize_and_pay() {
    driver.findElement(By.id("customize-and-pay")).click();
  }

  @And("I fill in the NIF with {string}")
  public void i_fill_nif(String nif) {
    driver.findElement(By.id("nif-input")).sendKeys(nif);
  }

  @And("I check the Priority Queue checkbox")
  public void i_check_the_priority_queue_checkbox() {
    driver.findElement(By.id("priority-queue-checkbox")).click();
  }

  @And("I click on \"Confirm order\"")
  public void i_click_on_confirm_order() {
    driver.findElement(By.id("confirm-order-button")).click();
  }

  @Then("I should see my order number as {string}")
  public void i_should_see_my_order_number_as(String orderNumber) {
    String actualOrderNumber = driver.findElement(By.id("order-number-text")).getText();
    assertThat(actualOrderNumber, is("ORDER: " + orderNumber));
  }

  @And("I should see total cost as {string}€")
  public void i_should_see_total_cost_as(String totalCost) {
    String actualCost = driver.findElement(By.id("order-cost-text")).getText();
    assertThat(actualCost, containsString(totalCost));
  }

  @And("I should see that my order is a priority order")
  public void i_should_see_that_my_order_is_a_priority_order() {
    String priorityOrderElementText = driver.findElement(By.id("priority-order")).getText();
    assertThat(priorityOrderElementText, is("Priority Order"));
  }

  @And("I click on remove for menu number {string}")
  public void i_click_on_remove_for_menu_number(String removeMenuNumber) {
    driver.findElement(By.id("remove-menu-customize-" + removeMenuNumber)).click();
  }

  @And("I fill in the name on the card with {string}")
  public void i_fill_name(String name) {
    driver.findElement(By.id("name-on-card-input")).sendKeys(name);
  }

  @And("I fill in the card number with {string}")
  public void i_fill_card_number(String number) {
    driver.findElement(By.id("card-number-input")).sendKeys(number);
  }

  @And("I fill in the expiration date with {string}")
  public void i_fill_expiration_date(String date) {
    driver.findElement(By.id("expiration-date-input")).sendKeys(date);
  }

  @Then("I should see the menu number {string} and not the menu number {string}")
  public void i_should_see_the_menu_number_and_not_the_menu_number(
      String menuNumberExisting, String menuNumberGone) {
    assertThat(
        driver.findElement(By.id("customize-menu-" + menuNumberExisting)).isDisplayed(), is(true));
    assertThat(driver.findElements(By.id("customize-menu-" + menuNumberGone)).size(), is(0));
  }

  public void captureConsoleLogs() {
    System.out.println("GETTING LOGS FROM THAT RUN!!!!!");
    LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
    Iterator<LogEntry> logIterator = logs.iterator();
    while (logIterator.hasNext()) {
        LogEntry logEntry = logIterator.next();
        System.out.println("Console log: " + logEntry.getMessage());
    }
  }
}
