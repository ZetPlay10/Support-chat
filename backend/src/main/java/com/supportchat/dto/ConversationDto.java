package com.supportchat.dto;

import java.time.Instant;

import com.supportchat.model.Conversation;
import com.supportchat.model.ConversationStatus;

public record ConversationDto(
		Long id,
		String subject,
		ConversationStatus status,
		UserDto client,
		UserDto agent, // null cat timp asteapta un agent
		Instant createdAt,
		Instant closedAt) {

	public static ConversationDto from(Conversation c) {
		return new ConversationDto(c.getId(), c.getSubject(), c.getStatus(),
				UserDto.from(c.getClient()),
				c.getAgent() == null ? null : UserDto.from(c.getAgent()),
				c.getCreatedAt(), c.getClosedAt());
	}
}
