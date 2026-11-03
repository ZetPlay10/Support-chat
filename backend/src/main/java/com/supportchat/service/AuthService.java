package com.supportchat.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportchat.dto.RegisterRequest;
import com.supportchat.dto.UserDto;
import com.supportchat.exception.ConflictException;
import com.supportchat.exception.NotFoundException;
import com.supportchat.model.Role;
import com.supportchat.model.User;
import com.supportchat.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	// Inregistrarea creeaza doar clienti; agentii si adminii sunt creati de un admin
	@Transactional
	public UserDto register(RegisterRequest request) {
		String username = request.username().trim();
		String email = request.email().trim().toLowerCase();
		if (users.existsByUsernameIgnoreCase(username)) {
			throw new ConflictException("Username-ul " + username + " este deja folosit");
		}
		if (users.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Email-ul " + email + " este deja folosit");
		}
		User user = users.save(new User(username, email, passwordEncoder.encode(request.password()),
				request.displayName().trim(), Role.USER));
		return UserDto.from(user);
	}

	public UserDto me(Long userId) {
		return users.findById(userId)
				.map(UserDto::from)
				.orElseThrow(() -> new NotFoundException("Utilizatorul " + userId + " nu exista"));
	}
}
