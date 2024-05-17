 package pt.ua.deti.springcanteen.frontend.customize;

 import io.cucumber.java.en.Then;
 import io.github.bonigarcia.wdm.WebDriverManager;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;

 import io.cucumber.java.en.And;
 import io.cucumber.java.en.When;

 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;


 public class CustomizeSteps {
     private WebDriver driver;

     @When("I navigate to {string}")
     public void i_navigate_to(String url) {
         WebDriverManager.firefoxdriver().setup();
         driver = new FirefoxDriver();
         driver.get(url);
     }

     // changed the id in frontend, continue...
     @And("I select the menu number {string}")
        public void i_select_menu(String number) {
            driver.findElement(By.cssSelector("#add-menu-" + number + " path")).click();
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
    public void i_click_confirm() throws InterruptedException {
        Thread.sleep(1000);
        driver.findElement(By.id("confirm-selection")).click();
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