package com.supportchat.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import com.supportchat.model.Role;
import com.supportchat.service.ConversationService;

// A doua linie de aparare: service-ul refuza apelul chiar daca nu trece prin regulile pe URL
// (de exemplu cand va fi apelat din controllerul WebSocket)
@SpringBootTest
class MethodSecurityTest {

	@Autowired
	ConversationService conversations;

	@Test
	void faraAutentificareServiceulRefuza() {
		assertThatThrownBy(() -> conversations.waitingQueue())
				.isInstanceOf(AuthenticationCredentialsNotFoundException.class);
	}

	@Test
	@WithMockUser(roles = "USER")
	void clientulNuVedeCoadaNiciDirectDinService() {
		assertThatThrownBy(() -> conversations.waitingQueue())
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithMockUser(roles = "AGENT")
	void agentulNuDeschideConversatiiNiciDirectDinService() {
		UserPrincipal agent = new UserPrincipal(1L, "agent", null, Role.AGENT);
		assertThatThrownBy(() -> conversations.create(agent, "Test"))
				.isInstanceOf(AccessDeniedException.class);
	}
}
