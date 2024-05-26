package pt.ua.deti.springcanteen.service.impl;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import pt.ua.deti.springcanteen.dto.*;
import pt.ua.deti.springcanteen.entities.Employee;
import pt.ua.deti.springcanteen.repositories.EmployeeRepository;
import pt.ua.deti.springcanteen.service.AuthenticationService;
import pt.ua.deti.springcanteen.service.EmployeeService;
import pt.ua.deti.springcanteen.service.JwtService;

@Service
@RequiredArgsConstructor
public class IAuthenticationService implements AuthenticationService {
        private final PasswordEncoder passwordEncoder;
        private final EmployeeRepository employeeRepository;
        private final JwtService jwtService;
        private final EmployeeService employeeService;
        private final AuthenticationManager authenticationManager;

        @Override
        public Optional<JwtAuthenticationResponseDTO> signup(SignUpRequestDTO request) {
                if (employeeRepository.findByEmail(request.getEmail()).isPresent())
                        return Optional.empty();
                Employee user = new Employee(
                        request.getUsername(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getRole()
                );
                employeeRepository.save(user);
                return Optional.of(JwtAuthenticationResponseDTO.fromEmployeeEntityAndTokens(user, jwtService.generateToken(user), jwtService.generateRefreshToken(user)));
        }

        @Override
        public JwtAuthenticationResponseDTO signin(SignInRequestDTO request) {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())
                );
                Employee user = employeeRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
                return JwtAuthenticationResponseDTO.fromEmployeeEntityAndTokens(user, jwtService.generateToken(user), jwtService.generateRefreshToken(user));
        }

        @Override
        public Optional<JwtRefreshResponseDTO> refreshToken(JwtRefreshRequestDTO request) {
                String userEmail = jwtService.extractSubject(request.getRefreshToken());
                UserDetails userDetails = employeeService.userDetailsService().loadUserByUsername(userEmail);

                if (!jwtService.isRefreshTokenValid(userDetails, request.getRefreshToken()))
                        return Optional.empty();
                return Optional.of(new JwtRefreshResponseDTO(jwtService.generateToken(userDetails)));
        }

}
