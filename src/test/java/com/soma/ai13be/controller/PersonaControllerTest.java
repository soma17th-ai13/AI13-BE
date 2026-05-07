package com.soma.ai13be.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.soma.ai13be.domain.persona.controller.PersonaController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.soma.ai13be.common.exception.GlobalExceptionHandler;
import com.soma.ai13be.domain.persona.exception.BuiltInPersonaDeletionException;
import com.soma.ai13be.domain.persona.entity.Persona;
import com.soma.ai13be.domain.persona.exception.DuplicatePersonaException;
import com.soma.ai13be.domain.persona.exception.PersonaNotFoundException;
import com.soma.ai13be.domain.persona.exception.PersonaPromptGenerationException;
import com.soma.ai13be.domain.persona.service.PersonaService;

class PersonaControllerTest {

	private final PersonaService personaService = org.mockito.Mockito.mock(PersonaService.class);
	private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PersonaController(personaService))
		.setControllerAdvice(new GlobalExceptionHandler())
		.build();

	@Test
	void createsPersona() throws Exception {
		when(personaService.create("health")).thenReturn(persona("health"));

		mockMvc.perform(post("/api/personas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "domainName": "health"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.domainName").value("health"))
			.andExpect(jsonPath("$.name").value("health Persona"))
			.andExpect(jsonPath("$.systemPrompt").value("health prompt"))
			.andExpect(jsonPath("$.builtIn").value(false))
			.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	void rejectsBlankDomainName() throws Exception {
		mockMvc.perform(post("/api/personas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "domainName": " "
					}
					"""))
			.andExpect(status().isBadRequest());

		verify(personaService, never()).create(anyString());
	}

	@Test
	void returnsConflictWhenPersonaAlreadyExists() throws Exception {
		when(personaService.create("health")).thenThrow(new DuplicatePersonaException("health"));

		mockMvc.perform(post("/api/personas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "domainName": "health"
					}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Persona already exists for domain: health"));
	}

	@Test
	void returnsBadGatewayWhenPromptGenerationFails() throws Exception {
		when(personaService.create("health")).thenThrow(new PersonaPromptGenerationException("health"));

		mockMvc.perform(post("/api/personas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "domainName": "health"
					}
					"""))
			.andExpect(status().isBadGateway())
			.andExpect(jsonPath("$.message").value("Failed to generate persona prompt for domain: health"));
	}

	@Test
	void findsAllPersonas() throws Exception {
		when(personaService.findAll()).thenReturn(List.of(persona("health"), persona("study")));

		mockMvc.perform(get("/api/personas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].domainName").value("health"))
			.andExpect(jsonPath("$[1].domainName").value("study"));
	}

	@Test
	void deletesPersona() throws Exception {
		mockMvc.perform(delete("/api/personas/{personaId}", 1L))
			.andExpect(status().isNoContent());

		verify(personaService).delete(1L);
	}

	@Test
	void returnsNotFoundWhenDeletingUnknownPersona() throws Exception {
		doThrow(new PersonaNotFoundException(1L)).when(personaService).delete(1L);

		mockMvc.perform(delete("/api/personas/{personaId}", 1L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Persona not found: 1"));
	}

	@Test
	void returnsConflictWhenDeletingBuiltInPersona() throws Exception {
		doThrow(new BuiltInPersonaDeletionException(1L)).when(personaService).delete(1L);

		mockMvc.perform(delete("/api/personas/{personaId}", 1L))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Built-in persona cannot be deleted: 1"));
	}

	private Persona persona(String domainName) {
		return Persona.builder()
			.domainName(domainName)
			.name(domainName + " Persona")
			.systemPrompt(domainName + " prompt")
			.builtIn(false)
			.enabled(true)
			.build();
	}
}
