package com.soma.ai13be.domain.knowledge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soma.ai13be.domain.knowledge.entity.KnowledgeNode;

public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, Long> {

	List<KnowledgeNode> findAllByOrderByCreatedAtDesc();

	List<KnowledgeNode> findByDomainNameOrderByCreatedAtDesc(String domainName);
}
