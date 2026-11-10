package com.supportchat.dto;

// Raspunsul la login: React pastreaza accessToken si il trimite ca "Authorization: Bearer <token>"
public record TokenResponse(String accessToken, String tokenType, long expiresIn, UserDto user) {
}
