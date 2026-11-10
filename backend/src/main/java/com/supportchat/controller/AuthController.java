package com.supportchat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.supportchat.dto.LoginRequest;
import com.supportchat.dto.RegisterRequest;
import com.supportchat.dto.TokenResponse;
import com.supportchat.dto.UserDto;
import com.supportchat.security.TokenService;
import com.supportchat.security.UserPrincipal;
import com.supportchat.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService auth;
	private final AuthenticationManager authenticationManager;
	private final TokenService tokens;

	public AuthController(AuthService auth, AuthenticationManager authenticationManager, TokenService tokens) {
		this.auth = auth;
		this.authenticationManager = authenticationManager;
		this.tokens = tokens;
	}

	// POST /api/auth/register  {username, email, password, displayName}
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto register(@Valid @RequestBody RegisterRequest request) {
		return auth.register(request);
	}

	// POST /api/auth/login  {username, password}  ->  {accessToken, tokenType, expiresIn, user}
	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		// DbUserDetailsService incarca utilizatorul, BCrypt compara parola
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		return new TokenResponse(tokens.generate(user), "Bearer", tokens.validitySeconds(), auth.me(user.getId()));
	}

	// GET /api/auth/me — utilizatorul curent (React il foloseste ca sa verifice login-ul)
	@GetMapping("/me")
	public UserDto me(@AuthenticationPrincipal UserPrincipal user) {
		return auth.me(user.getId());
	}

	// Utilizator inexistent sau parola gresita: acelasi raspuns, ca sa nu aflam ce conturi exista
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProblemDetail loginFailed() {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Username sau parola gresita");
		pd.setTitle("Autentificare esuata");
		return pd;
	}
}
