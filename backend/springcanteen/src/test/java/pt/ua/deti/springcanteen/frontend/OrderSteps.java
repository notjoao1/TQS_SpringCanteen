 package pt.ua.deti.springcanteen.frontend;

 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;

 import io.cucumber.java.en.When;

 public class OrderSteps {
     private WebDriver driver;

     @When("I navigate to {string}")
     public void i_navigate_to(String url) {
         driver = new FirefoxDriver();
         driver.get(url);
     }

     // changed the id in frontend, continue...
 }