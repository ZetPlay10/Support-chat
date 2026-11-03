package com.supportchat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supportchat.model.Conversation;
import com.supportchat.model.ConversationStatus;
import com.supportchat.model.User;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	// @EntityGraph aduce clientul si agentul in acelasi SELECT (LEFT JOIN), fara N+1.

	// coada de asteptare: cele mai vechi conversatii primele
	@EntityGraph(attributePaths = {"client", "agent"})
	List<Conversation> findByStatusOrderByCreatedAtAsc(ConversationStatus status);

	// conversatiile deschise de un client
	@EntityGraph(attributePaths = {"client", "agent"})
	List<Conversation> findByClientIdOrderByCreatedAtDesc(Long clientId);

	// conversatiile preluate de un agent
	@EntityGraph(attributePaths = {"client", "agent"})
	List<Conversation> findByAgentIdOrderByCreatedAtDesc(Long agentId);

	// admin: toate conversatiile
	@EntityGraph(attributePaths = {"client", "agent"})
	List<Conversation> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = {"client", "agent"})
	Optional<Conversation> findWithParticipantsById(Long id); // "WithParticipants" este ignorat de parser

	// Preluare atomica: daca doi agenti apasa "Preia" simultan, doar un UPDATE gaseste status = WAITING.
	// Intoarce numarul de randuri modificate (0 = deja preluata sau inchisa).
	@Modifying(clearAutomatically = true)
	@Query("""
			update Conversation c
			set c.agent = :agent, c.status = com.supportchat.model.ConversationStatus.ACTIVE
			where c.id = :id and c.status = com.supportchat.model.ConversationStatus.WAITING
			""")
	int assignIfWaiting(@Param("id") Long id, @Param("agent") User agent);
}
