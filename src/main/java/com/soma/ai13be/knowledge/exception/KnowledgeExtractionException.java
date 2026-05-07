package com.soma.ai13be.knowledge.exception;

public class KnowledgeExtractionException extends RuntimeException {

	public KnowledgeExtractionException(String message) {
		super(message);
	}

	public KnowledgeExtractionException(String message, Throwable cause) {
		super(message, cause);
	}
}
