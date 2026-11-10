package com.supportchat.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
		mvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(jsonPath("$.title").value("Neautentificat"))
				.andExpect(jsonPath("$.instance").value("/api/auth/me"));
	}

	@Test
	void tokenModificatEsteRespins() throws Exception {
		String token = login("agent", "agent123");
		// schimbam primul caracter din semnatura (ultimul poate contine doar biti de umplutura)
		int sig = token.lastIndexOf('.') + 1;
		char c = token.charAt(sig);
		String tampered = token.substring(0, sig) + (c == 'A' ? 'B' : 'A') + token.substring(sig + 1);
		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
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
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Acces interzis"));
	}

	@Test
	void conversatieInexistentaDa404ProblemDetail() throws Exception {
		String token = login("agent", "agent123");
		mvc.perform(get("/api/conversations/999999").header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Conversatia 999999 nu exista"));
	}

	@Test
	void validareaIntoarceEroriPeCampuri() throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"x\",\"email\":\"nu-e-email\",\"password\":\"scurt\",\"displayName\":\"X\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors.username").exists())
				.andExpect(jsonPath("$.errors.email").value("Email invalid"))
				.andExpect(jsonPath("$.errors.password").exists());
	}

	String registerAndLogin(String username) throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","email":"%s@example.com",
								 "password":"parola1234","displayName":"Test"}""".formatted(username, username)))
				.andExpect(status().isCreated());
		return login(username, "parola1234");
	}

	@Test
	void agentulNuPoateDeschideConversatii() throws Exception {
		String token = login("agent", "agent123");
		mvc.perform(post("/api/conversations").header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"subject\":\"Test\"}"))
				.andExpect(status().isForbidden());
	}

	// URL-ul /messages e permis oricui e autentificat; il opreste @PreAuthorize din MessageService
	@Test
	void adminulNuScrieInConversatiaAltcuiva() throws Exception {
		String client = registerAndLogin("client_msg");
		String body = mvc.perform(post("/api/conversations").header("Authorization", "Bearer " + client)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"subject\":\"Nu merge login-ul\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(body, "$.id");

		String admin = login("admin", "admin123");
		mvc.perform(get("/api/conversations/" + id + "/messages").header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk());
		mvc.perform(post("/api/conversations/" + id + "/messages").header("Authorization", "Bearer " + admin)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"content\":\"Salut\"}"))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Acces interzis"));
	}

	@Test
	void altClientNuVedeConversatia() throws Exception {
		String owner = registerAndLogin("client_a");
		String body = mvc.perform(post("/api/conversations").header("Authorization", "Bearer " + owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"subject\":\"Privat\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(body, "$.id");

		String other = registerAndLogin("client_b");
		mvc.perform(get("/api/conversations/" + id + "/messages").header("Authorization", "Bearer " + other))
				.andExpect(status().isForbidden());
	}
}
