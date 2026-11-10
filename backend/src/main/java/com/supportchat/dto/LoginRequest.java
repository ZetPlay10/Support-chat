package com.supportchat.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Username-ul este obligatoriu") String username,
		@NotBlank(message = "Parola este obligatorie") String password) {
}
