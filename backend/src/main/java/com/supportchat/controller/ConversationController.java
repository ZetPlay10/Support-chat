package com.supportchat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.supportchat.dto.ConversationDto;
import com.supportchat.dto.CreateConversationRequest;
import com.supportchat.dto.MessageDto;
import com.supportchat.dto.SendMessageRequest;
import com.supportchat.security.UserPrincipal;
import com.supportchat.service.ConversationService;
import com.supportchat.service.MessageService;

import jakarta.validation.Valid;

// @RestController: metodele intorc date, transformate automat in JSON
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

	private final ConversationService conversations;
	private final MessageService messages;

	public ConversationController(ConversationService conversations, MessageService messages) {
		this.conversations = conversations;
		this.messages = messages;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ConversationDto create(@Valid @RequestBody CreateConversationRequest request,
			@AuthenticationPrincipal UserPrincipal user) {
		return conversations.create(user, request.subject());
	}

	@GetMapping
	public List<ConversationDto> myConversations(@AuthenticationPrincipal UserPrincipal user) {
		return conversations.myConversations(user);
	}

	@GetMapping("/queue")
	public List<ConversationDto> waitingQueue() {
		return conversations.waitingQueue();
	}

	@GetMapping("/{id}")
	public ConversationDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
		return conversations.get(id, user);
	}

	@PostMapping("/{id}/assign")
	public ConversationDto assign(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
		return conversations.assign(id, user);
	}

	@PostMapping("/{id}/close")
	public ConversationDto close(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
		return conversations.close(id, user);
	}

	@GetMapping("/{id}/messages")
	public List<MessageDto> messages(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
		return messages.history(id, user);
	}

	@PostMapping("/{id}/messages")
	@ResponseStatus(HttpStatus.CREATED)
	public MessageDto send(@PathVariable Long id, @Valid @RequestBody SendMessageRequest request,
			@AuthenticationPrincipal UserPrincipal user) {
		return messages.send(id, user, request.content());
	}

	@PostMapping("/{id}/read")
	public Map<String, Integer> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
		return Map.of("marked", messages.markAsRead(id, user));
	}
}
