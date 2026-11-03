package com.supportchat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Cererea e valida, dar starea curenta nu o permite (ex.: conversatie deja preluata)
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
	public ConflictException(String message) { super(message); }
}
