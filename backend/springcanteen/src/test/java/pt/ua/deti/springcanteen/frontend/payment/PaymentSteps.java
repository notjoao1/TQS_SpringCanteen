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

    @And("I fill in the NIF with {string}")
    public void i_fill_nif(String nif) {
        driver.findElement(By.id("nif-input")).sendKeys(nif);
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


    @And("I click on \"Confirm order\"")
    public void i_click_confirm_order() {
        driver.findElement(By.id("confirm-order-button")).click();
    }
    
    @And("I should see the message \"Successfully added menu to order.\"")
    public void i_should_see_message() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("snackbar-add-menu-to-order"), "Successfully added menu to order."));
        assertThat(driver.findElement(By.id("snackbar-add-menu-to-order")).getText(), containsString("Successfully added menu to order."));
    }

    @And("I select to pay in the {string}")
    public void i_select_pay_desk(String payment_locale) {
        driver.findElement(By.id(payment_locale)).click();
    }

    @Then("I should see the error {string}")
    public void i_should_see_error(String error) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("error-alert"), error));
        assertThat(driver.findElement(By.id("error-alert")).getText(), containsString(error));

        driver.quit();
    }

    @Then("I should not see any errors")
    public void i_should_not_see_errors() {
        assertThat(driver.findElements(By.id("error-alert")).size(), is(0));
        driver.quit();
    }
 }