package com.supportchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
		@NotBlank(message = "Mesajul nu poate fi gol")
		@Size(max = 5000, message = "Mesajul are cel mult 5000 de caractere")
		String content) {
}
