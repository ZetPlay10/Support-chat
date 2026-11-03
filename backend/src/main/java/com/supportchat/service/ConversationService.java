package com.supportchat.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportchat.dto.ConversationDto;
import com.supportchat.exception.ConflictException;
import com.supportchat.exception.ForbiddenException;
import com.supportchat.exception.NotFoundException;
import com.supportchat.model.Conversation;
import com.supportchat.model.ConversationStatus;
import com.supportchat.model.Role;
import com.supportchat.repository.ConversationRepository;
import com.supportchat.repository.UserRepository;
import com.supportchat.security.UserPrincipal;

// Service-urile intorc DTO-uri: cu open-in-view=false, entitatile LAZY nu mai pot fi citite in controller
@Service
@Transactional(readOnly = true)
public class ConversationService {

	private final ConversationRepository conversations;
	private final UserRepository users;

	public ConversationService(ConversationRepository conversations, UserRepository users) {
		this.conversations = conversations;
		this.users = users;
	}

	@Transactional
	public ConversationDto create(UserPrincipal user, String subject) {
		// getReferenceById nu face SELECT: avem nevoie doar de cheia straina
		Conversation c = new Conversation(subject.trim(), users.getReferenceById(user.getId()));
		return ConversationDto.from(conversations.save(c));
	}

	// Clientul isi vede conversatiile deschise, agentul pe cele preluate, adminul pe toate
	public List<ConversationDto> myConversations(UserPrincipal user) {
		List<Conversation> result = switch (user.getRole()) {
			case USER -> conversations.findByClientIdOrderByCreatedAtDesc(user.getId());
			case AGENT -> conversations.findByAgentIdOrderByCreatedAtDesc(user.getId());
			case ADMIN -> conversations.findAllByOrderByCreatedAtDesc();
		};
		return result.stream().map(ConversationDto::from).toList();
	}

	public List<ConversationDto> waitingQueue() {
		return conversations.findByStatusOrderByCreatedAtAsc(ConversationStatus.WAITING).stream()
				.map(ConversationDto::from)
				.toList();
	}

	public ConversationDto get(Long id, UserPrincipal user) {
		Conversation c = find(id);
		// agentii pot vedea si conversatiile din coada, inainte sa le preia
		boolean inQueue = user.getRole() == Role.AGENT && c.getStatus() == ConversationStatus.WAITING;
		if (!inQueue) {
			checkAccess(c, user);
		}
		return ConversationDto.from(c);
	}

	@Transactional
	public ConversationDto assign(Long id, UserPrincipal agent) {
		find(id); // 404 daca nu exista
		if (conversations.assignIfWaiting(id, users.getReferenceById(agent.getId())) == 0) {
			throw new ConflictException("Conversatia " + id + " a fost deja preluata sau inchisa");
		}
		return ConversationDto.from(find(id));
	}

	@Transactional
	public ConversationDto close(Long id, UserPrincipal user) {
		Conversation c = find(id);
		checkAccess(c, user);
		if (c.getStatus() == ConversationStatus.CLOSED) {
			throw new ConflictException("Conversatia " + id + " este deja inchisa");
		}
		// Entitatea e "managed": modificarile se scriu la commit (dirty checking), fara save()
		c.setStatus(ConversationStatus.CLOSED);
		c.setClosedAt(Instant.now());
		return ConversationDto.from(c);
	}

	Conversation find(Long id) {
		return conversations.findWithParticipantsById(id)
				.orElseThrow(() -> new NotFoundException("Conversatia " + id + " nu exista"));
	}

	// Doar clientul, agentul conversatiei si adminul au acces
	static void checkAccess(Conversation c, UserPrincipal user) {
		if (user.getRole() != Role.ADMIN && !c.hasParticipant(user.getId())) {
			throw new ForbiddenException("Nu faceti parte din conversatia " + c.getId());
		}
	}
}
