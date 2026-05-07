package com.soma.ai13be.domain.knowledge.exception;

public class KnowledgeNodeNotFoundException extends RuntimeException {

	public KnowledgeNodeNotFoundException(Long nodeId) {
		super("Knowledge node not found: " + nodeId);
	}
}
