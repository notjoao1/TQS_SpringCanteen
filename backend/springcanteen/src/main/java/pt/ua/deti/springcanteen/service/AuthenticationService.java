package pt.ua.deti.springcanteen.service;

import java.util.Optional;

import pt.ua.deti.springcanteen.dto.*;

public interface AuthenticationService {
    Optional<JwtAuthenticationResponseDTO> signup(SignUpRequestDTO request);

    JwtAuthenticationResponseDTO signin(SignInRequestDTO request);

    Optional<JwtRefreshResponseDTO> refreshToken(JwtRefreshRequestDTO request);
}
