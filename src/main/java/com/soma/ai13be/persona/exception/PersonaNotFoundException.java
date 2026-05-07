package com.soma.ai13be.persona.exception;

public class PersonaNotFoundException extends RuntimeException {

	public PersonaNotFoundException(Long personaId) {
		super("Persona not found: " + personaId);
	}
}
