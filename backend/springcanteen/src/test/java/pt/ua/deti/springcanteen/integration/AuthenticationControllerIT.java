package pt.ua.deti.springcanteen.integration;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerIT {

    @LocalServerPort
    int serverPort;

    @BeforeEach
    void setup() {
        RestAssured.port = serverPort;
    }

    @Test
    void givenNonProtectedEndpoint_whenBadRequest_thenReturn400(){
        given().
        when().
            post("api/signup").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
    }


}
