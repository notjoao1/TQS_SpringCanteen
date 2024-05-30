package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.ua.deti.springcanteen.dto.JwtAuthenticationResponseDTO;
import pt.ua.deti.springcanteen.dto.JwtRefreshRequestDTO;
import pt.ua.deti.springcanteen.dto.JwtRefreshResponseDTO;
import pt.ua.deti.springcanteen.dto.SignInRequestDTO;
import pt.ua.deti.springcanteen.dto.SignUpRequestDTO;
import pt.ua.deti.springcanteen.entities.Employee;
import pt.ua.deti.springcanteen.entities.EmployeeRole;
import pt.ua.deti.springcanteen.repositories.EmployeeRepository;
import pt.ua.deti.springcanteen.service.impl.IAuthenticationService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
  @Mock PasswordEncoder passwordEncoder;

  @Mock EmployeeRepository employeeRepository;

  @Mock UserDetails userDetails;

  @Mock JwtService jwtService;

  @Mock EmployeeService employeeService;

  @Mock AuthenticationManager authenticationManager;

  @InjectMocks IAuthenticationService authenticationService;

  SignUpRequestDTO signUpRequestDTO;

  SignInRequestDTO signInRequestDTO;

  JwtRefreshRequestDTO jwtRefreshRequestDTO;

  @Test
  void givenSignUpNonExistentCredentials_thenShouldCreateNewEmployee() {
    signUpRequestDTO =
        new SignUpRequestDTO("person1", "person1@gmail.com", "secure", EmployeeRole.DESK_PAYMENTS);
    when(employeeRepository.findByEmail("person1@gmail.com"))
        .thenReturn(Optional.empty()); // no users with this email
    when(jwtService.generateToken(any())).thenReturn("verysecuretoken");
    when(jwtService.generateRefreshToken(any())).thenReturn("verysecurerefreshtoken");

    Optional<JwtAuthenticationResponseDTO> optResponse =
        authenticationService.signup(signUpRequestDTO);

    assertThat(optResponse.isPresent(), is(true));
    JwtAuthenticationResponseDTO response = optResponse.get();
    assertThat(response.getToken(), is("verysecuretoken"));
    assertThat(response.getRefreshToken(), is("verysecurerefreshtoken"));
    assertThat(response.getEmail(), is("person1@gmail.com"));
    assertThat(response.getUserRole(), is(EmployeeRole.DESK_PAYMENTS.toString()));
    verify(employeeRepository, times(1)).save(any());
  }

  @Test
  void givenSignUpExistentCredentials_thenShouldReturnEmpty() {
    signUpRequestDTO =
        new SignUpRequestDTO("person1", "person1@gmail.com", "secure", EmployeeRole.COOK);
    when(employeeRepository.findByEmail("person1@gmail.com"))
        .thenReturn(
            Optional.of(
                new Employee(
                    "1", "person1@gmail.com", "1", EmployeeRole.COOK))); // no users with this email

    Optional<JwtAuthenticationResponseDTO> optResponse =
        authenticationService.signup(signUpRequestDTO);

    assertThat(optResponse.isEmpty(), is(true));
    // nothing saved to db
    verify(employeeRepository, times(0)).save(any());
  }

  @Test
  void givenSignInExistentCredentials_thenShouldReturnValidResponse() {
    signInRequestDTO = new SignInRequestDTO("person1@gmail.com", "password123");
    when(employeeRepository.findByEmail("person1@gmail.com"))
        .thenReturn(
            Optional.of(new Employee("1", "person1@gmail.com", "password123", EmployeeRole.COOK)));
    when(jwtService.generateToken(any())).thenReturn("verysecuretoken");
    when(jwtService.generateRefreshToken(any())).thenReturn("verysecurerefreshtoken");

    JwtAuthenticationResponseDTO response = authenticationService.signin(signInRequestDTO);

    assertThat(response.getEmail(), is("person1@gmail.com"));
    assertThat(response.getToken(), is("verysecuretoken"));
    assertThat(response.getRefreshToken(), is("verysecurerefreshtoken"));
    assertThat(response.getUserRole(), is(EmployeeRole.COOK.toString()));
  }

  @Test
  void givenValidRefreshToken_thenShouldReturnNewToken() {
    jwtRefreshRequestDTO = new JwtRefreshRequestDTO("somerefreshtoken");
    when(employeeService.userDetailsService())
        .thenReturn(
            new UserDetailsService() {
              @Override
              public UserDetails loadUserByUsername(String username)
                  throws UsernameNotFoundException {
                return userDetails;
              }
            });
    when(jwtService.isRefreshTokenValid(userDetails, "somerefreshtoken")).thenReturn(true);
    when(jwtService.generateToken(userDetails)).thenReturn("newtoken");

    Optional<JwtRefreshResponseDTO> optResponse =
        authenticationService.refreshToken(jwtRefreshRequestDTO);

    assertThat(optResponse.isPresent(), is(true));
    JwtRefreshResponseDTO response = optResponse.get();
    assertThat(response.getAccessToken(), is("newtoken"));
  }

  @Test
  void givenInvalidRefreshToken_thenShouldReturnEmpty() {
    jwtRefreshRequestDTO = new JwtRefreshRequestDTO("somerefreshtoken");
    when(employeeService.userDetailsService())
        .thenReturn(
            new UserDetailsService() {
              @Override
              public UserDetails loadUserByUsername(String username)
                  throws UsernameNotFoundException {
                return userDetails;
              }
            });
    when(jwtService.isRefreshTokenValid(userDetails, "somerefreshtoken")).thenReturn(false);

    Optional<JwtRefreshResponseDTO> optResponse =
        authenticationService.refreshToken(jwtRefreshRequestDTO);

    assertThat(optResponse.isEmpty(), is(true));
  }
}
