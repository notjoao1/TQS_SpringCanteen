package pt.ua.deti.springcanteen.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pt.ua.deti.springcanteen.service.JwtService;

@Service
public class IJwtService implements JwtService {
    @Value("${jwt.secret_key}")
    private String jwtSigningKey;
    @Value("${jwt.expirationtime.access}")
    private int accessExpTime; // 20 minutes
    @Value("${jwt.expirationtime.refresh}")
    private int refreshExpTime; // 2 days

    @Override
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails user) {
        return generateToken(new HashMap<>(), user, accessExpTime);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails user) {
        final String username = user.getUsername();
        return extractSubject(token).equals(username) && !isTokenExpired(token);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails user, int expirationTime) {
        return Jwts.builder().claims().empty().add(extraClaims).subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime)).and()
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSigningKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> extraClaims = new HashMap<>();
        // extra claim to distinguish it from the access token with this claim
        extraClaims.put("type", "refresh");
        return this.generateToken(extraClaims, user, refreshExpTime);
    }

    @Override
    public boolean isRefreshTokenValid(UserDetails user, String refreshToken) {
        final Claims refClaims = extractAllClaims(refreshToken);
        return refClaims.containsKey("type") && refClaims.get("type").equals("refresh")
                && !isTokenExpired(refreshToken);
    }

}
