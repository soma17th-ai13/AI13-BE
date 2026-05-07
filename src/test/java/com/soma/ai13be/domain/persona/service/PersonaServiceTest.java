package com.soma.ai13be.domain.persona.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.soma.ai13be.common.client.SolarApiClient;
import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.common.client.dto.SolarChatRequest;
import com.soma.ai13be.common.client.dto.SolarChatResponse;
import com.soma.ai13be.domain.persona.entity.Persona;
import com.soma.ai13be.domain.persona.exception.BuiltInPersonaDeletionException;
import com.soma.ai13be.domain.persona.exception.DuplicatePersonaException;
import com.soma.ai13be.domain.persona.exception.PersonaNotFoundException;
import com.soma.ai13be.domain.persona.exception.PersonaPromptGenerationException;
import com.soma.ai13be.domain.persona.repository.PersonaRepository;

class PersonaServiceTest {

	private final PersonaRepository personaRepository = org.mockito.Mockito.mock(PersonaRepository.class);
	private final SolarApiClient solarApiClient = org.mockito.Mockito.mock(SolarApiClient.class);
	private final PersonaService service = new PersonaService(personaRepository, solarApiClient);

	@Test
	void createsPersonaWithGeneratedSystemPrompt() {
		when(personaRepository.existsByDomainName("health")).thenReturn(false);
		when(solarApiClient.chatCompletion(any(SolarChatRequest.class)))
			.thenReturn(response("Generated health persona prompt"));
		when(personaRepository.save(any(Persona.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Persona persona = service.create(" health ");

		assertThat(persona.getDomainName()).isEqualTo("health");
		assertThat(persona.getName()).isEqualTo("health Persona");
		assertThat(persona.getSystemPrompt()).isEqualTo("Generated health persona prompt");
		assertThat(persona.isBuiltIn()).isFalse();
		assertThat(persona.isEnabled()).isTrue();

		ArgumentCaptor<SolarChatRequest> requestCaptor = ArgumentCaptor.forClass(SolarChatRequest.class);
		verify(solarApiClient).chatCompletion(requestCaptor.capture());
		SolarChatRequest request = requestCaptor.getValue();
		assertThat(request.temperature()).isEqualTo(0.2);
		assertThat(request.maxTokens()).isEqualTo(1200);
		assertThat(request.messages())
			.extracting(SolarChatMessage::role)
			.containsExactly("system", "user");
		assertThat(request.messages().get(1).content()).contains("Domain: health");
	}

	@Test
	void rejectsBlankDomainName() {
		assertThatThrownBy(() -> service.create(" "))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("domainName");
	}

	@Test
	void rejectsDuplicateDomainName() {
		when(personaRepository.existsByDomainName("health")).thenReturn(true);

		assertThatThrownBy(() -> service.create("health"))
			.isInstanceOf(DuplicatePersonaException.class)
			.hasMessageContaining("health");
	}

	@Test
	void rejectsEmptyGeneratedPrompt() {
		when(personaRepository.existsByDomainName("health")).thenReturn(false);
		when(solarApiClient.chatCompletion(any(SolarChatRequest.class)))
			.thenReturn(emptyResponse());

		assertThatThrownBy(() -> service.create("health"))
			.isInstanceOf(PersonaPromptGenerationException.class)
			.hasMessageContaining("health");
	}

	@Test
	void deletesPersona() {
		Persona persona = persona("health", false);
		when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

		service.delete(1L);

		verify(personaRepository).delete(persona);
	}

	@Test
	void rejectsDeletingUnknownPersona() {
		when(personaRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(1L))
			.isInstanceOf(PersonaNotFoundException.class)
			.hasMessageContaining("1");
	}

	@Test
	void rejectsDeletingBuiltInPersona() {
		Persona persona = persona("health", true);
		when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

		assertThatThrownBy(() -> service.delete(1L))
			.isInstanceOf(BuiltInPersonaDeletionException.class)
			.hasMessageContaining("1");
		verify(personaRepository, never()).delete(any(Persona.class));
	}

	private Persona persona(String domainName, boolean builtIn) {
		return Persona.builder()
			.domainName(domainName)
			.name(domainName + " Persona")
			.systemPrompt(domainName + " prompt")
			.builtIn(builtIn)
			.enabled(true)
			.build();
	}

	private SolarChatResponse response(String content) {
		return new SolarChatResponse(
			"chatcmpl-test",
			"chat.completion",
			1710000000L,
			"solar-pro3",
			List.of(new SolarChatResponse.Choice(
				0,
				SolarChatMessage.assistant(content),
				"stop"
			)),
			new SolarChatResponse.Usage(3, 5, 8)
		);
	}

	private SolarChatResponse emptyResponse() {
		return new SolarChatResponse(
			"chatcmpl-test",
			"chat.completion",
			1710000000L,
			"solar-pro3",
			List.of(new SolarChatResponse.Choice(
				0,
				null,
				"stop"
			)),
			new SolarChatResponse.Usage(3, 0, 3)
		);
	}
}
