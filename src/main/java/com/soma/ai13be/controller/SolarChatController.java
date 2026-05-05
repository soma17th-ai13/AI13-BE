package com.soma.ai13be.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.soma.ai13be.common.client.SolarApiClient;
import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.common.client.dto.SolarChatRequest;
import com.soma.ai13be.common.client.dto.SolarChatResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solar")
@RequiredArgsConstructor
public class SolarChatController {

	private final SolarApiClient solarApiClient;

	@PostMapping("/chat")
	public ResponseEntity<SolarChatResult> chat(@RequestBody SolarChatCommand command) {
		if (command == null || !StringUtils.hasText(command.message())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
		}

		SolarChatResponse response = solarApiClient.chatCompletion(toSolarChatRequest(command));

		return ResponseEntity.ok(new SolarChatResult(response.firstContent(), response.usage()));
	}

	private SolarChatRequest toSolarChatRequest(SolarChatCommand command) {
		List<SolarChatMessage> messages = new ArrayList<>();
		if (StringUtils.hasText(command.systemMessage())) {
			messages.add(SolarChatMessage.system(command.systemMessage()));
		}
		messages.add(SolarChatMessage.user(command.message()));

		return new SolarChatRequest(messages, command.temperature(), command.maxTokens());
	}

	public record SolarChatCommand(
		String message,
		String systemMessage,
		Double temperature,
		Integer maxTokens
	) {
	}

	public record SolarChatResult(
		String content,
		SolarChatResponse.Usage usage
	) {
	}
}
