package com.example.onlineshop.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserProfileResponse {
    Long userId;
    String username;
    String email;
    Long customerId;
    List<String> roles;
}
