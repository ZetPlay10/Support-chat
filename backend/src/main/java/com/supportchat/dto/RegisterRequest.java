package com.supportchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Username-ul este obligatoriu")
		@Size(min = 3, max = 50, message = "Username-ul are intre 3 si 50 de caractere")
		@Pattern(regexp = "[A-Za-z0-9._-]+", message = "Username-ul poate contine doar litere, cifre, . _ -")
		String username,

		@NotBlank(message = "Email-ul este obligatoriu")
		@Email(message = "Email invalid")
		@Size(max = 255)
		String email,

		@NotBlank(message = "Parola este obligatorie")
		@Size(min = 8, max = 72, message = "Parola are intre 8 si 72 de caractere") // BCrypt foloseste max. 72 de octeti
		String password,

		@NotBlank(message = "Numele afisat este obligatoriu")
		@Size(max = 100, message = "Numele afisat are cel mult 100 de caractere")
		String displayName) {
}
