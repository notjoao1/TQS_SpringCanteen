package pt.ua.deti.springcanteen.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import pt.ua.deti.springcanteen.config.JwtAuthenticationFilter;
import pt.ua.deti.springcanteen.controllers.AuthenticationController;
import pt.ua.deti.springcanteen.service.JwtService;

@WebMvcTest(AuthenticationController.class)
@ContextConfiguration(classes = {AuthenticationController.class, JwtService.class, JwtAuthenticationFilter.class})
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContextSetup;

    @Test
    @WithMockUser
    void givenNonProtectedEndpoint_whenBadRequest_thenReturn400(){
        RestAssuredMockMvc.webAppContextSetup(webAppContextSetup);
        RestAssuredMockMvc.
                given().
                    mockMvc(mockMvc).
                when().
                    post("api/signup").
                then().
                    statusCode(HttpStatus.SC_BAD_REQUEST);
    }




}
