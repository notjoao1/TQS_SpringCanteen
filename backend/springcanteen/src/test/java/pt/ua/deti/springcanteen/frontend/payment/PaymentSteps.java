package pt.ua.deti.springcanteen.frontend.payment;

import io.cucumber.java.en.Then;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;


 public class PaymentSteps {
     private WebDriver driver;
     private Wait<WebDriver> wait;

     @When("I navigate to {string}")
     public void i_navigate_to(String url) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new FirefoxDriver(options);
        // wait up to 10 seconds
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(url);
        // setup wait
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
        WebElement confirmSelection = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirm-selection")));

        Actions actions = new Actions(driver);
        actions.moveToElement(confirmSelection).click().perform();
    }

    @And("I click on \"Customize and pay\"")
    public void i_click_customize() {
        driver.findElement(By.id("customize-and-pay")).click();
    }

    
    @And("I should see the message \"Successfully added menu to order.\"")
    public void i_should_see_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("snackbar-add-menu-to-order"), "Successfully added menu to order."));
        assertThat(driver.findElement(By.id("snackbar-add-menu-to-order")).getText(), containsString("Successfully added menu to order."));
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
 }