package com.example.onlineshop.Controller;

import com.example.onlineshop.Entity.User;
import com.example.onlineshop.Repository.UserRepository;
import com.example.onlineshop.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Long customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .customerId(customerId)
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();

        return ResponseEntity.ok(response);
    }
}
