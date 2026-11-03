package com.supportchat.dto;

import com.supportchat.model.Role;
import com.supportchat.model.User;

// Ce trimitem despre un utilizator in JSON (fara email si parola)
public record UserDto(Long id, String username, String displayName, Role role, boolean online) {

	public static UserDto from(User u) {
		return new UserDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole(), u.isOnline());
	}
}
