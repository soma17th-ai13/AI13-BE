package com.soma.kg.repository;

import com.soma.kg.entity.KnowledgeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, Long> {
    List<KnowledgeNode> findByDomainIdOrderByCreatedAtDesc(Long domainId);
}
