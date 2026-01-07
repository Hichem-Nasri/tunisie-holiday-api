package tn.tunisieconnect.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.user.entity.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // ⚠️ Minimum 32 caractères pour HS256
    private static final String SECRET_KEY =
            "SECRET_TUNISIE_HOLIDAY_2025_SECRET_TUNISIE_HOLIDAY_2025";

    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 heures

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("type", user.getType().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
