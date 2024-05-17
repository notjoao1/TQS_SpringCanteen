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

    @And("I click to \"View order\"")
    public void i_click_view_order() {
        driver.findElement(By.id("view-order")).click();
    }

    @And("I click on \"Cancel selection\"")
    public void i_click_cancel() throws InterruptedException {
        Thread.sleep(1000);
        driver.findElement(By.id("cancel-selection")).click();
    }

    @And("I remove the menu number {string}")
    public void i_remove_menu(String number) {
        driver.findElement(By.id("remove-menu-" + number)).click();
    }
    
    @Then("I should see the message \"Successfully added menu to order.\"")
    public void i_should_see_message() throws InterruptedException {
        Thread.sleep(1000); // wait for the message to appear
        assertThat(driver.findElement(By.id("snackbar-add-menu-to-order")).getText(), containsString("Successfully added menu to order."));
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
 }