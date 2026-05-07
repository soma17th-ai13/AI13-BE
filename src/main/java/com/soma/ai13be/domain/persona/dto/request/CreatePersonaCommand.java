package com.soma.ai13be.domain.persona.dto.request;

/**
 * 사용자가 새 페르소나를 만들 때 입력하는 최소 요청값입니다.
 * systemPrompt는 서버가 Solar를 호출해 생성하므로 클라이언트에서 받지 않습니다.
 */
public record CreatePersonaCommand(
	String domainName
) {
}
