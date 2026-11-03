package com.supportchat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.supportchat.dto.RegisterRequest;
import com.supportchat.dto.UserDto;
import com.supportchat.security.UserPrincipal;
import com.supportchat.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService auth;

	public AuthController(AuthService auth) {
		this.auth = auth;
	}

	// POST /api/auth/register  {username, email, password, displayName}
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto register(@Valid @RequestBody RegisterRequest request) {
		return auth.register(request);
	}

	// GET /api/auth/me — utilizatorul curent (React il foloseste ca sa verifice login-ul)
	@GetMapping("/me")
	public UserDto me(@AuthenticationPrincipal UserPrincipal user) {
		return auth.me(user.getId());
	}
}
