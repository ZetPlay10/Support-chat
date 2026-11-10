package com.supportchat.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.supportchat.model.Role;

// Transforma un JWT valid (semnatura si "exp" deja verificate) in acelasi UserPrincipal
// pe care il produce si login-ul. Astfel controllerele raman cu @AuthenticationPrincipal UserPrincipal.
public class JwtUserPrincipalConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Number userId = jwt.getClaim(TokenService.CLAIM_USER_ID);
		String role = jwt.getClaimAsString(TokenService.CLAIM_ROLE);
		if (userId == null || role == null) {
			throw new BadJwtException("Token fara uid sau role");
		}
		UserPrincipal user = new UserPrincipal(userId.longValue(), jwt.getSubject(), null, Role.valueOf(role));
		// credentials = token-ul, authorities = ROLE_USER / ROLE_AGENT / ROLE_ADMIN
		return UsernamePasswordAuthenticationToken.authenticated(user, jwt, user.getAuthorities());
	}
}
