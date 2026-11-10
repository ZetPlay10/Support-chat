package com.supportchat.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportchat.dto.MessageDto;
import com.supportchat.exception.ConflictException;
import com.supportchat.exception.ForbiddenException;
import com.supportchat.model.Conversation;
import com.supportchat.model.ConversationStatus;
import com.supportchat.model.Message;
import com.supportchat.repository.MessageRepository;
import com.supportchat.repository.UserRepository;
import com.supportchat.security.UserPrincipal;

@Service
@Transactional(readOnly = true)
public class MessageService {

	private final MessageRepository messages;
	private final UserRepository users;
	private final ConversationService conversationService;

	public MessageService(MessageRepository messages, UserRepository users,
			ConversationService conversationService) {
		this.messages = messages;
		this.users = users;
		this.conversationService = conversationService;
	}

	@PreAuthorize("isAuthenticated()")
	public List<MessageDto> history(Long conversationId, UserPrincipal user) {
		ConversationService.checkAccess(conversationService.find(conversationId), user);
		return messages.findByConversationIdOrderBySentAtAsc(conversationId).stream()
				.map(MessageDto::from)
				.toList();
	}

	// Folosit acum de REST; mai tarziu si de controllerul WebSocket
	@Transactional
	@PreAuthorize("hasAnyRole('USER', 'AGENT')") // adminul doar citeste
	public MessageDto send(Long conversationId, UserPrincipal user, String content) {
		Conversation c = conversationService.find(conversationId);
		// adminul poate citi, dar nu scrie in conversatiile altora
		if (!c.hasParticipant(user.getId())) {
			throw new ForbiddenException("Nu faceti parte din conversatia " + conversationId);
		}
		if (c.getStatus() == ConversationStatus.CLOSED) {
			throw new ConflictException("Conversatia " + conversationId + " este inchisa");
		}
		Message m = messages.save(new Message(c, users.getReferenceById(user.getId()), content.trim()));
		return MessageDto.from(m);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('USER', 'AGENT')")
	public int markAsRead(Long conversationId, UserPrincipal user) {
		Conversation c = conversationService.find(conversationId);
		if (!c.hasParticipant(user.getId())) {
			throw new ForbiddenException("Nu faceti parte din conversatia " + conversationId);
		}
		return messages.markAsRead(conversationId, user.getId());
	}
}
