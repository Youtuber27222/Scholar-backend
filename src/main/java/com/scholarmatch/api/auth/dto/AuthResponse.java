package com.scholarmatch.api.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, UserDto user) {
}
