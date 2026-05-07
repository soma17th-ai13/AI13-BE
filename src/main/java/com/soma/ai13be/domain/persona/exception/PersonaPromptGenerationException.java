package com.soma.ai13be.domain.persona.exception;

/**
 * Solar가 비어 있거나 사용할 수 없는 페르소나 프롬프트를 반환했을 때 발생합니다.
 */
public class PersonaPromptGenerationException extends RuntimeException {

	public PersonaPromptGenerationException(String domainName) {
		super("Failed to generate persona prompt for domain: " + domainName);
	}
}
