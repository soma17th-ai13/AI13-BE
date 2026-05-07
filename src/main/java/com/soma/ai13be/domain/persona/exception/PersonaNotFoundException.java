package com.soma.ai13be.domain.persona.exception;

public class PersonaNotFoundException extends RuntimeException {

	public PersonaNotFoundException(Long personaId) {
		super("Persona not found: " + personaId);
	}
}
