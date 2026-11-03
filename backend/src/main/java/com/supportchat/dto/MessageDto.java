package com.supportchat.dto;

import java.time.Instant;

import com.supportchat.model.Message;

public record MessageDto(
		Long id,
		Long conversationId,
		Long senderId,
		String senderName,
		String content,
		Instant sentAt,
		boolean read) {

	public static MessageDto from(Message m) {
		return new MessageDto(m.getId(), m.getConversation().getId(),
				m.getSender().getId(), m.getSender().getDisplayName(),
				m.getContent(), m.getSentAt(), m.isRead());
	}
}
