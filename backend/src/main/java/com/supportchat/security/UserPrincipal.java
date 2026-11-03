package com.supportchat.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.supportchat.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Utilizatorul autentificat; il primim in controllere cu @AuthenticationPrincipal
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

	private final Long id;
	private final String username;
	private final String password;
	private final Role role;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// hasRole("AGENT") verifica autoritatea "ROLE_AGENT"
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}
}
