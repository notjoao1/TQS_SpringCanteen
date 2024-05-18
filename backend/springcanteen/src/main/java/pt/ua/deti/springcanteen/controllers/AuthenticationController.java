package pt.ua.deti.springcanteen.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import pt.ua.deti.springcanteen.dto.*;
import pt.ua.deti.springcanteen.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Create an account.")
    @ApiResponse(responseCode = "200", description = "Account created successfully." , content = { @Content(mediaType = "application/json",
      schema = @Schema(implementation = JwtAuthenticationResponseDTO.class)) })
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO req) {
        return ResponseEntity.ok(authenticationService.signup(req));
    }

    @Operation(summary = "Authenticate an account and log in.")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated and logged user in.", content = { @Content(mediaType = "application/json", 
      schema = @Schema(implementation = JwtAuthenticationResponseDTO.class)) })
    @PostMapping("signin")
    public ResponseEntity<JwtAuthenticationResponseDTO> signIn(@RequestBody @Valid SignInRequestDTO req) {
        return ResponseEntity.ok(authenticationService.signin(req));
    }

    @Operation(summary = "Refresh JWT access token using a JWT refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully refreshed JWT with given request token.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = JwtRefreshResponseDTO.class)) }),
            @ApiResponse(responseCode = "403", description = "Invalid refresh token", content = @Content),
    })
    @PostMapping("refreshToken")
    public ResponseEntity<JwtRefreshResponseDTO> refreshToken(@RequestBody @Valid JwtRefreshRequestDTO req) {
        JwtRefreshResponseDTO res = authenticationService.refreshToken(req);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(authenticationService.refreshToken(req));
    }

}
