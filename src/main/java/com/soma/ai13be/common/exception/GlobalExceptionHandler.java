package com.soma.ai13be.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.soma.ai13be.common.dto.ErrorResponse;
import com.soma.ai13be.domain.knowledge.exception.KnowledgeNodeNotFoundException;
import com.soma.ai13be.domain.persona.exception.BuiltInPersonaDeletionException;
import com.soma.ai13be.domain.persona.exception.DuplicatePersonaException;
import com.soma.ai13be.domain.persona.exception.PersonaNotFoundException;
import com.soma.ai13be.domain.persona.exception.PersonaPromptGenerationException;

/**
 * 도메인 예외를 HTTP 응답으로 변환하는 전역 예외 처리기입니다.
 * 컨트롤러는 비즈니스 예외를 직접 처리하지 않고 이 클래스에 위임합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicatePersonaException.class)
	public ResponseEntity<ErrorResponse> handleDuplicatePersona(DuplicatePersonaException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(PersonaPromptGenerationException.class)
	public ResponseEntity<ErrorResponse> handlePromptGenerationFailure(PersonaPromptGenerationException exception) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(PersonaNotFoundException.class)
	public ResponseEntity<ErrorResponse> handlePersonaNotFound(PersonaNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(BuiltInPersonaDeletionException.class)
	public ResponseEntity<ErrorResponse> handleBuiltInPersonaDeletion(BuiltInPersonaDeletionException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(KnowledgeNodeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleKnowledgeNodeNotFound(KnowledgeNodeNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception) {
		return ResponseEntity.status(exception.getStatusCode())
			.body(new ErrorResponse(exception.getReason()));
	}
}
