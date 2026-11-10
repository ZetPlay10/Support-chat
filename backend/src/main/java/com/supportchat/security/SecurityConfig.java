package com.supportchat.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ProblemDetailSecurityHandler errors)
			throws Exception {
		http
				// API stateless: fara sesiune si fara cookie-uri, deci CSRF nu se aplica
				// (token-ul pleaca explicit in header-ul Authorization, nu automat de la browser)
				.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
						.requestMatchers("/api/conversations/queue").hasAnyRole("AGENT", "ADMIN")
						.requestMatchers("/api/conversations/*/assign").hasRole("AGENT")
						.anyRequest().authenticated())
				// Authorization: Bearer <token> -> semnatura + "exp" + "iss" verificate -> UserPrincipal
				.oauth2ResourceServer(rs -> rs
						.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtUserPrincipalConverter()))
						.authenticationEntryPoint(errors) // 401: token invalid / expirat
						.accessDeniedHandler(errors)) // 403
				// 401 cand lipseste token-ul, 403 cand rolul nu ajunge, ambele ca ProblemDetail
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(errors)
						.accessDeniedHandler(errors));
		return http.build();
	}

	@Bean
	ProblemDetailSecurityHandler problemDetailSecurityHandler(JsonMapper jsonMapper) {
		return new ProblemDetailSecurityHandler(jsonMapper);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Acelasi AuthenticationManager pe care l-ar folosi Spring (DbUserDetailsService + BCrypt);
	// AuthController il apeleaza la POST /api/auth/login
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) { // HS256 cere o cheie de minim 256 de biti
			throw new IllegalStateException("app.jwt.secret: minim 32 de octeti");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey key) { // semneaza
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey key) { // verifica semnatura, "exp" si "iss"
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
		return decoder;
	}
}
