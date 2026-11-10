package com.supportchat.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Fluxul real: utilizatorii din V2, token-uri adevarate semnate de TokenService
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // utilizatorii creati in teste se sterg (rollback) la final
class JwtAuthIntegrationTest {

	@Autowired
	MockMvc mvc;

	String login(String username, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}

	@Test
	void loginCuParolaCorectaIntoarceTokenFolositLaMe() throws Exception {
		String token = login("agent", "agent123");
		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("agent"))
				.andExpect(jsonPath("$.role").value("AGENT"));
	}

	@Test
	void parolaGresitaDa401() throws Exception {
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"agent\",\"password\":\"gresita\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Autentificare esuata"));
	}

	@Test
	void faraTokenDa401() throws Exception {
		mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenModificatEsteRespins() throws Exception {
		String token = login("agent", "agent123");
		// schimbam primul caracter din semnatura (ultimul poate contine doar biti de umplutura)
		int sig = token.lastIndexOf('.') + 1;
		char c = token.charAt(sig);
		String tampered = token.substring(0, sig) + (c == 'A' ? 'B' : 'A') + token.substring(sig + 1);
		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void agentulVedeCoada() throws Exception {
		String token = login("agent", "agent123");
		mvc.perform(get("/api/conversations/queue").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void clientulInregistratNuVedeCoada() throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"client_test","email":"client_test@example.com",
								 "password":"parola1234","displayName":"Client Test"}"""))
				.andExpect(status().isCreated());
		String token = login("client_test", "parola1234");
		mvc.perform(get("/api/conversations/queue").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}
}
