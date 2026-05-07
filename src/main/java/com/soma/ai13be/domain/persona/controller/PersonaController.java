package com.soma.ai13be.domain.persona.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.soma.ai13be.domain.persona.dto.request.CreatePersonaCommand;
import com.soma.ai13be.domain.persona.dto.request.UpdatePersonaCommand;
import com.soma.ai13be.domain.persona.dto.response.PersonaResult;
import com.soma.ai13be.domain.persona.entity.Persona;
import com.soma.ai13be.domain.persona.service.PersonaService;

import lombok.RequiredArgsConstructor;

/**
 * 페르소나 관리 API입니다.
 */
@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {

	private final PersonaService personaService;

	@PostMapping
	public ResponseEntity<PersonaResult> create(@RequestBody CreatePersonaCommand command) {
		// 입력값이 null이거나 공백인 경우 예외 처리
		if (command == null || !StringUtils.hasText(command.domainName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "domainName must not be blank");
		}

		Persona persona = personaService.create(command.domainName());

		return ResponseEntity.status(HttpStatus.CREATED).body(PersonaResult.from(persona));
	}

	@GetMapping
	public ResponseEntity<List<PersonaResult>> findAll() {
		return ResponseEntity.ok(
			personaService.findAll().stream()
				.map(PersonaResult::from)
				.toList()
		);
	}

	@PostMapping("/{personaId}/regenerate")
	public ResponseEntity<PersonaResult> regenerate(@PathVariable Long personaId) {
		Persona persona = personaService.regenerate(personaId);
		return ResponseEntity.ok(PersonaResult.from(persona));
	}

	@PutMapping("/{personaId}")
	public ResponseEntity<PersonaResult> update(
		@PathVariable Long personaId,
		@RequestBody UpdatePersonaCommand command
	) {
		if (command == null || !StringUtils.hasText(command.systemPrompt())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "systemPrompt must not be blank");
		}

		Persona persona = personaService.update(personaId, command.systemPrompt());
		return ResponseEntity.ok(PersonaResult.from(persona));
	}

	@DeleteMapping("/{personaId}")
	public ResponseEntity<Void> delete(@PathVariable Long personaId) {
		personaService.delete(personaId);

		return ResponseEntity.noContent().build();
	}
}
