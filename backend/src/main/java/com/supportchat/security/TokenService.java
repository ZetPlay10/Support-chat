package com.supportchat.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

// Emite token-ul JWT dupa un login reusit.
// Payload-ul e doar codat (Base64URL), nu criptat: nu punem in el parole sau date personale.
@Service
public class TokenService {

	public static final String ISSUER = "support-chat";
	public static final String CLAIM_USER_ID = "uid";
	public static final String CLAIM_ROLE = "role";

	private final JwtEncoder encoder;
	private final Duration validity;

	public TokenService(JwtEncoder encoder, @Value("${app.jwt.validity:PT1H}") Duration validity) {
		this.encoder = encoder;
		this.validity = validity;
	}

	public String generate(UserPrincipal user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject(user.getUsername())
				.issuedAt(now)
				.expiresAt(now.plus(validity))
				// id-ul si rolul ajung in token, ca sa refacem UserPrincipal fara sa citim baza de date
				.claim(CLAIM_USER_ID, user.getId())
				.claim(CLAIM_ROLE, user.getRole().name())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long validitySeconds() {
		return validity.toSeconds();
	}
}
