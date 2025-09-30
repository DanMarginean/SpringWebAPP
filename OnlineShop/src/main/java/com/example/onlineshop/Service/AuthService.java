package com.example.onlineshop.Service;

import com.example.onlineshop.Entity.*;
import com.example.onlineshop.Repository.RefreshTokenRepository;
import com.example.onlineshop.Repository.RoleRepository;
import com.example.onlineshop.Repository.UserRepository;
import com.example.onlineshop.Util.JwtUtil;
import com.example.onlineshop.dto.AuthResponse;
import com.example.onlineshop.dto.LoginRequest;
import com.example.onlineshop.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * ================================================
 * REFRESH TOKEN PROCESS DOCUMENTATION
 * ================================================
 *
 * 1. Login Phase:
 *    - When a user logs in successfully, the system generates:
 *        a) An **Access Token** (JWT) → Short-lived (e.g., 15 minutes).
 *        b) A **Refresh Token** (UUID stored in DB) → Long-lived (e.g., 7 days).
 *    - The access token is used to authenticate API calls.
 *    - The refresh token is stored in the database and linked to the user.
 *
 * 2. Access Token Expiration:
 *    - Once the access token expires, the client cannot call protected APIs anymore.
 *    - Instead of forcing the user to log in again, the client sends the refresh token
 *      to the `/auth/refresh` endpoint to obtain a new access token.
 *
 * 3. Refresh Endpoint Workflow:
 *    a) Client calls `/auth/refresh` with the refresh token.
 *    b) Backend validates:
 *          - Does the refresh token exist in the database?
 *          - Has it expired (expiryDate < now)?
 *    c) If valid:
 *          - A new **Access Token** (JWT) is generated.
 *          - A new **Refresh Token** (rotation) is created and stored in the database.
 *          - The old refresh token is deleted (prevent reuse).
 *    d) If invalid/expired:
 *          - The request is rejected with 401 Unauthorized.
 *
 * 4. Logout:
 *    - On logout, the server deletes the user’s refresh token(s) from the database.
 *    - This ensures the client cannot use the refresh token to obtain new access tokens.
 *
 * 5. Security Considerations:
 *    - Refresh tokens are stored securely in the database.
 *    - Tokens are rotated: each refresh creates a new one, invalidating the old.
 *    - If a refresh token leaks, it can only be used once before becoming invalid.
 *
 * ================================================
 * SUMMARY
 * ================================================
 * - Access Token (JWT): short-lived, used for API calls.
 * - Refresh Token (UUID in DB): long-lived, used to get new access tokens.
 * - Rotation: Old refresh token is deleted, new one issued at each refresh.
 * - Logout: Delete refresh tokens to block further refresh attempts.
 *
 */

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final RefreshTokenRepository refreshTokenRepository;

//    private static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000;

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Create User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(customerRole))
                .build();

        // Create Customer profile linked to this User
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .user(user)
                .build();

        user.setCustomer(customer);

        userRepository.save(user);

        return "User registered successfully with customer profile";
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create refresh token
        RefreshToken refreshToken =  jwtUtil.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }


//    public RefreshToken createRefreshToken(User user) {
//        RefreshToken refreshToken = RefreshToken.builder()
//                .user(user)
//                .token(UUID.randomUUID().toString())
//                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS))
//                .build();
//
//        return refreshTokenRepository.save(refreshToken);
//    }

    public RefreshToken verifyAndRotateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        // ✅ delete old refresh token (rotation)
        refreshTokenRepository.delete(refreshToken);

        // ✅ create new one for same user
        return jwtUtil.createRefreshToken(refreshToken.getUser());
    }

    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        // Rotate token (delete old + create new)
        refreshTokenRepository.delete(refreshToken);
        RefreshToken newRefreshToken = jwtUtil.createRefreshToken(refreshToken.getUser());


        User user = newRefreshToken.getUser();
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getUsername());

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
