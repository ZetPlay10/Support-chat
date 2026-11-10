package com.supportchat.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Toate erorile din controllere si service-uri ajung la React in acelasi format (RFC 9457):
// {"type", "title", "status", "detail", "instance"} cu Content-Type: application/problem+json.
// Erorile 401/403 din filtrele de securitate le scrie ProblemDetailSecurityHandler.
// Nu prindem aici AccessDeniedException: o lasam sa ajunga la filtrele Spring Security.
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail notFound(NotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Resursa inexistenta", ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail conflict(ConflictException ex) {
		return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
	}

	// 403 din regulile de business (ex.: nu faceti parte din conversatie)
	@ExceptionHandler(ForbiddenException.class)
	public ProblemDetail forbidden(ForbiddenException ex) {
		return problem(HttpStatus.FORBIDDEN, "Acces interzis", ex.getMessage());
	}

	// POST /api/auth/login cu username inexistent sau parola gresita: acelasi mesaj in ambele cazuri,
	// ca sa nu se poata afla ce conturi exista
	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail authenticationFailed(AuthenticationException ex) {
		return problem(HttpStatus.UNAUTHORIZED, "Autentificare esuata", "Username sau parola gresita");
	}

	// @Valid esuat: adaugam "errors": {camp: mesaj}, ca formularul React sa afiseze eroarea langa camp
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
		ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Date invalide", "Verificati campurile trimise");
		pd.setProperty("errors", errors);
		return handleExceptionInternal(ex, pd, headers, status, request);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		return pd;
	}
}
