package com.supportchat.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

// Erorile de securitate apar in filtre, inainte de controller, deci ApiExceptionHandler nu le vede.
// Le scriem aici in acelasi format ProblemDetail ca restul API-ului.
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	public ProblemDetailSecurityHandler(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override // 401: lipseste token-ul sau este invalid / expirat / modificat
	public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
			throws IOException {
		res.setHeader("WWW-Authenticate", "Bearer");
		write(req, res, HttpStatus.UNAUTHORIZED, "Neautentificat",
				"Trimiteti un token valid in header-ul Authorization: Bearer <token>");
	}

	@Override // 403: token-ul este valid, dar rolul nu are voie
	public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
			throws IOException {
		write(req, res, HttpStatus.FORBIDDEN, "Acces interzis",
				"Contul dumneavoastra nu are dreptul sa faca aceasta operatie");
	}

	private void write(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String title,
			String detail) throws IOException {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		pd.setInstance(URI.create(req.getRequestURI()));
		res.setStatus(status.value());
		res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		jsonMapper.writeValue(res.getOutputStream(), pd);
	}
}
