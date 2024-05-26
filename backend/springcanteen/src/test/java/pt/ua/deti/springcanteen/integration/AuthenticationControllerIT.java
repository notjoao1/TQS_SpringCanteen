package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import pt.ua.deti.springcanteen.entities.Employee;
import pt.ua.deti.springcanteen.entities.EmployeeRole;
import pt.ua.deti.springcanteen.repositories.EmployeeRepository;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
class AuthenticationControllerIT {
    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("testname")
            .withPassword("testpassword")
            .withDatabaseName("sc_test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }


    @LocalServerPort
    int serverPort;

    @Autowired
    EmployeeRepository employeeRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = serverPort;
    }

    @Test
    void givenValidCredentials_whenCreateEmployee_thenEmployeeCreatedAndTokenReturned() {
        String signUpRequest = "{" +
        "    \"username\": \"hello_world_test\"," +
        "    \"email\": \"test_email@gmail.com\"," +
        "    \"password\": \"person\"," +
        "    \"role\": \"COOK\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup")
        .then()
            .statusCode(HttpStatus.SC_CREATED)
                .and()
            .body("username", is("hello_world_test"))
                .and()
            .body("email", is("test_email@gmail.com"))
                .and()
            .body("userRole", is("COOK"))
                .and()
            .body("$", not(hasKey("password")))
                .and()
            .body("$", hasKey("refreshToken"))
                .and()
            .body("$", hasKey("token"));
    }
    
    @Test
    void givenInvalidCredentials_whenCreateEmployee_thenReturn403() {
        // setup DB to have a user with email "testemail@gmail.com"
        employeeRepository.save(new Employee("testname", "testemail@gmail.com", "testpassword", EmployeeRole.COOK));
        
        String signUpRequest = "{" +
        "    \"username\": \"testname\"," +
        "    \"email\": \"testemail@gmail.com\"," +
        "    \"password\": \"a\"," +
        "    \"role\": \"COOK\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnEmployeeAndToken() {
        // setup DB to have a user with email "testemail@gmail.com"
        employeeRepository.save(new Employee("testname", "testemail@gmail.com", "testpassword", EmployeeRole.COOK));

        String signUpRequest = "{" +
        "    \"email\": \"testemail@gmail.com\"," +
        "    \"password\": \"a\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup")
        .then()
            .statusCode(HttpStatus.SC_CREATED)
                .and()
            .body("username", is("testname"))
                .and()
            .body("email", is("testemail@gmail.com"))
                .and()
            .body("userRole", is("COOK"))
                .and()
            .body("$", not(hasKey("password")))
                .and()
            .body("$", hasKey("refreshToken"))
                .and()
            .body("$", hasKey("token"));
        
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturn403() {
        // trying to login with credentials that don't correspond to any existing user
        String signUpRequest = "{" +
        "    \"email\": \"no_one@gmail.com\"," +
        "    \"password\": \"1234\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    
    }

}
