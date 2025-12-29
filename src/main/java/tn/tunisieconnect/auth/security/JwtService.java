package tn.tunisieconnect.auth.security;

import org.springframework.stereotype.Service;
import tn.tunisieconnect.user.entity.User;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET_KEY = "SECRET_TUNISIE_HOLIDAY_2025";
    private final long EXPIRATION = 1000 * 60 * 60 * 24; // 24h

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("type", user.getType().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
