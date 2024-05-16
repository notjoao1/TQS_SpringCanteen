 package pt.ua.deti.springcanteen.frontend;

 import io.cucumber.java.en.Then;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;

 import io.cucumber.java.en.And;
 import io.cucumber.java.en.When;

 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.CoreMatchers.containsString;


 public class OrderSteps {
     private WebDriver driver;

     @When("I navigate to {string}")
     public void i_navigate_to(String url) {
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
    public void i_click_confirm() {
        driver.findElement(By.id("confirm-selection")).click();
    }
    
    @Then("I should see the message \"Successfully added menu to order.\"")
    public void i_should_see_message() {
        assertThat(driver.findElement(By.id("message")).getText(), containsString("Successfully added menu to order."));
        driver.quit();
    }
 }