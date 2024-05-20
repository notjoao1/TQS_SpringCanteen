package pt.ua.deti.springcanteen.frontend.customize;

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


 public class CustomizeSteps {
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

    @And("I click to customize the menu {string}")
    public void i_click_customize_menu(String number) {
        driver.findElement(By.id("customize-menu-" + number)).click();
    }

    @And("I increase the quantity of item {string} by {string}")
    public void i_increase_quantity(String item, String quantity) {
        driver.findElement(By.id("increment-" + item)).click();
        for (int i = 0; i < Integer.parseInt(quantity) - 1; i++) {
            driver.findElement(By.id("increment-" + item)).click();
        }
    }

    @And("I decrease the quantity of item {string} by {string}")
    public void i_decrease_quantity(String item, String quantity) {
        driver.findElement(By.id("decrement-" + item)).click();
        for (int i = 0; i < Integer.parseInt(quantity) - 1; i++) {
            driver.findElement(By.id("decrement-" + item)).click();
        }
    }

    @Then("I should see the total price of {string}")
    public void i_should_see_total_price(String price) {
        assertThat(driver.findElement(By.id("total-price")).getText(), containsString(price));
        driver.quit();
    }
 }