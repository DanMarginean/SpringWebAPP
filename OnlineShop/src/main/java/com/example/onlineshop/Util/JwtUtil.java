package com.example.onlineshop.Util;

import com.example.onlineshop.Entity.RefreshToken;
import com.example.onlineshop.Entity.User;
import com.example.onlineshop.Repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final String SECRET = "super_secret_key_that_is_definitely_longer_than_64_characters_1234567890_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long ACCESS_EXPIRATION_MS = 15 * 60 * 1000; // 15 min
    private static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000;


    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
