package com.supportchat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supportchat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

	// istoricul unei conversatii, cu expeditorul incarcat in acelasi SELECT
	@EntityGraph(attributePaths = "sender")
	List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

	// mesajele necitite primite de un utilizator intr-o conversatie
	long countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long userId);

	// marcheaza ca citite mesajele primite de un utilizator (un singur UPDATE)
	@Modifying
	@Query("update Message m set m.read = true " +
			"where m.conversation.id = :conversationId and m.sender.id <> :userId and m.read = false")
	int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
