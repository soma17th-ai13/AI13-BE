package com.soma.ai13be.domain.persona.exception;

public class BuiltInPersonaDeletionException extends RuntimeException {

	public BuiltInPersonaDeletionException(Long personaId) {
		super("Built-in persona cannot be deleted: " + personaId);
	}
}
