package com.example.onlineshop;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void encodePassword() {
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("BCrypt hash: " + encodedPassword);
    }
}