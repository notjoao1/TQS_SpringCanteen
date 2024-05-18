package pt.ua.deti.springcanteen.service;

import pt.ua.deti.springcanteen.dto.*;

public interface AuthenticationService {
    JwtAuthenticationResponseDTO signup(SignUpRequestDTO request);

    JwtAuthenticationResponseDTO signin(SignInRequestDTO request);

    JwtRefreshResponseDTO refreshToken(JwtRefreshRequestDTO request);
}
