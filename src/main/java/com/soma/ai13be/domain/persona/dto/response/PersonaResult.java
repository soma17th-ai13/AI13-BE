package com.soma.ai13be.domain.persona.dto.response;

import java.time.Instant;

import com.soma.ai13be.domain.persona.entity.Persona;

/**
 * 페르소나 조회/생성 API 응답입니다.
 * 토론 실행 시 프론트가 선택 가능한 페르소나 목록을 구성할 때 사용합니다.
 */
public record PersonaResult(
	Long id,
	String domainName,
	String name,
	String systemPrompt,
	boolean builtIn,
	boolean enabled,
	Instant createdAt,
	Instant updatedAt
) {

	public static PersonaResult from(Persona persona) {
		return new PersonaResult(
			persona.getId(),
			persona.getDomainName(),
			persona.getName(),
			persona.getSystemPrompt(),
			persona.isBuiltIn(),
			persona.isEnabled(),
			persona.getCreatedAt(),
			persona.getUpdatedAt()
		);
	}
}
