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
        "    \"email\": \"test@gmail.com\"," +
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
            .body("email", is("test@gmail.com"))
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
        // setup DB to have a user with email "testemai1l@gmail.com"
        employeeRepository.save(new Employee("testname", "testemail1@gmail.com", "testpassword", EmployeeRole.COOK));
        
        String signUpRequest = "{" +
        "    \"username\": \"testname\"," +
        "    \"email\": \"testemail1@gmail.com\"," +
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
        // create user before we try to log in
        String signUpRequest = "{" +
        "    \"username\": \"verycoolname\"," +
        "    \"email\": \"testemail2@gmail.com\"," +
        "    \"password\": \"testpassword\"," +
        "    \"role\": \"COOK\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup");

        String signInRequest = "{" +
        "    \"email\": \"testemail2@gmail.com\"," +
        "    \"password\": \"testpassword\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signInRequest)
        .when()
            .post("api/auth/signin")
        .then()
            .statusCode(HttpStatus.SC_OK)
                .and()
            .body("username", is("verycoolname"))
                .and()
            .body("email", is("testemail2@gmail.com"))
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
    void givenInvalidEmail_whenLogin_thenReturn403() {
        // trying to login with credentials that don't correspond to any existing user
        String signInRequest = "{" +
        "    \"email\": \"no_one@gmail.com\"," +
        "    \"password\": \"1234\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signInRequest)
        .when()
            .post("api/auth/signin")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    
    }

    @Test
    void givenValidEmailWrongPassword_whenLogin_thenReturn403() {
        // create user before we try to log in
        String signUpRequest = "{" +
        "    \"username\": \"hello_world_test\"," +
        "    \"email\": \"testemail3@gmail.com\"," +
        "    \"password\": \"testpassword\"," +
        "    \"role\": \"COOK\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signUpRequest)
        .when()
            .post("api/auth/signup");

        // setup request with wrong password
        String signInRequest = "{" +
        "    \"email\": \"testemail3@gmail.com\"," +
        "    \"password\": \"wrongpassword\"}";

        given()
            .contentType(ContentType.JSON)
            .body(signInRequest)
        .when()
            .post("api/auth/signin")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
