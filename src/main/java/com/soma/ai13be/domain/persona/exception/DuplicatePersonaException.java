package com.soma.ai13be.domain.persona.exception;

/**
 * 같은 도메인 이름의 페르소나가 이미 있을 때 발생합니다.
 */
public class DuplicatePersonaException extends RuntimeException {

	public DuplicatePersonaException(String domainName) {
		super("Persona already exists for domain: " + domainName);
	}
}
