package com.example.onlineshop.config;

import com.example.onlineshop.Entity.*;
import com.example.onlineshop.Repository.RoleRepository;
import com.example.onlineshop.Repository.UserRepository;
import com.example.onlineshop.Util.JwtUtil;
import com.example.onlineshop.dto.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        // 🔹 If user doesn’t exist, create one with ROLE_CUSTOMER
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            User newUser = User.builder()
                    .username(email)
                    .email(email)
                    .password("") // password not used for Google accounts
                    .roles(Set.of(customerRole))
                    .build();

            Customer customer = Customer.builder()
                    .firstName(name)
                    .lastName(name)
                    .phoneNumber(null)
                    .user(newUser)
                    .build();

            newUser.setCustomer(customer);


            return userRepository.save(newUser);
        });

        // 🔹 Generate access + refresh token
        String accessToken = jwtUtil.generateAccessToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                .toList()
                )
        );

        RefreshToken refreshToken = jwtUtil.createRefreshToken(user);

        // 🔹 Build AuthResponse DTO
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken.getToken());

        // 🔹 Return JSON
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }
}