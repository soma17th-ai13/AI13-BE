package com.soma.ai13be.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soma.ai13be.chat.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
}
