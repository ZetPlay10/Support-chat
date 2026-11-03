package com.supportchat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supportchat.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	// login: cautam utilizatorul dupa username
	Optional<User> findByUsername(String username);

	// inregistrare: refuzam username-uri / email-uri deja folosite
	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);
}
