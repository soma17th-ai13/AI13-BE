package com.soma.ai13be.discussion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soma.ai13be.discussion.entity.AgentDiscussion;

public interface AgentDiscussionRepository extends JpaRepository<AgentDiscussion, Long> {
}
