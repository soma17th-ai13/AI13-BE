package com.soma.ai13be.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.soma.ai13be.common.client.SolarApiClient;
import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.common.client.dto.SolarChatRequest;
import com.soma.ai13be.common.client.dto.SolarChatResponse;

class SolarChatControllerTest {

	private final SolarApiClient solarApiClient = org.mockito.Mockito.mock(SolarApiClient.class);
	private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SolarChatController(solarApiClient)).build();

	@Test
	void callsSolarApiClientAndReturnsAssistantContent() throws Exception {
		when(solarApiClient.chatCompletion(any(SolarChatRequest.class)))
			.thenReturn(new SolarChatResponse(
				"chatcmpl-test",
				"chat.completion",
				1710000000L,
				"solar-pro3",
				List.of(new SolarChatResponse.Choice(
					0,
					SolarChatMessage.assistant("안녕하세요."),
					"stop"
				)),
				new SolarChatResponse.Usage(3, 5, 8)
			));

		mockMvc.perform(post("/api/solar/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "message": "안녕",
					  "systemMessage": "짧게 답해",
					  "temperature": 0.2,
					  "maxTokens": 100
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("안녕하세요."))
			.andExpect(jsonPath("$.usage.total_tokens").value(8));

		ArgumentCaptor<SolarChatRequest> requestCaptor = ArgumentCaptor.forClass(SolarChatRequest.class);
		verify(solarApiClient).chatCompletion(requestCaptor.capture());
		SolarChatRequest request = requestCaptor.getValue();

		org.assertj.core.api.Assertions.assertThat(request.messages())
			.containsExactly(
				SolarChatMessage.system("짧게 답해"),
				SolarChatMessage.user("안녕")
			);
		org.assertj.core.api.Assertions.assertThat(request.temperature()).isEqualTo(0.2);
		org.assertj.core.api.Assertions.assertThat(request.maxTokens()).isEqualTo(100);
	}

	@Test
	void rejectsBlankMessage() throws Exception {
		mockMvc.perform(post("/api/solar/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "message": " "
					}
					"""))
			.andExpect(status().isBadRequest());

		verify(solarApiClient, never()).chatCompletion(any(SolarChatRequest.class));
	}
}
