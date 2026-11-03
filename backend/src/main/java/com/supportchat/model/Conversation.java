package com.supportchat.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;

	@Column(nullable = false, length = 200)
	private String subject;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ConversationStatus status = ConversationStatus.WAITING;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_id")
	private User client;

	// null cat timp conversatia asteapta un agent
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id")
	private User agent;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant createdAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	// Partea "inversa" a relatiei: cheia straina este in tabela messages.
	@OneToMany(mappedBy = "conversation")
	@OrderBy("sentAt")
	@Setter(AccessLevel.NONE)
	private List<Message> messages = new ArrayList<>();

	public Conversation(String subject, User client) {
		this.subject = subject;
		this.client = client;
	}

	// getId() pe un proxy LAZY nu face SELECT: id-ul este deja cunoscut
	public boolean hasParticipant(Long userId) {
		return client.getId().equals(userId)
				|| (agent != null && agent.getId().equals(userId));
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
