package com.supportchat.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.supportchat.repository.UserRepository;

// Spring Security il foloseste la login ca sa incarce utilizatorul din baza de date
@Service
public class DbUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	public DbUserDetailsService(UserRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return users.findByUsername(username)
				.map(u -> new UserPrincipal(u.getId(), u.getUsername(), u.getPasswordHash(), u.getRole()))
				.orElseThrow(() -> new UsernameNotFoundException(username));
	}
}
