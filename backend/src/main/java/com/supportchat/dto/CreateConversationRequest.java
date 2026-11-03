package com.supportchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
		@NotBlank(message = "Subiectul este obligatoriu")
		@Size(max = 200, message = "Subiectul are cel mult 200 de caractere")
		String subject) {
}
