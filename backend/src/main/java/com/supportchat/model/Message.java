package com.supportchat.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversation_id")
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id")
	private User sender;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "sent_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant sentAt;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	public Message(Conversation conversation, User sender, String content) {
		this.conversation = conversation;
		this.sender = sender;
		this.content = content;
	}

	@PrePersist
	void onCreate() {
		sentAt = Instant.now();
	}
}
