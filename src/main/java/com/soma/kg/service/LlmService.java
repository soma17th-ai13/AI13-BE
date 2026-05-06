package com.soma.kg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LlmService {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${upstage.api.key}")
    private String apiKey;

    @Value("${upstage.api.url}")
    private String apiUrl;

    @Value("${upstage.api.model}")
    private String model;

    public LlmService() {
        this.restClient = RestClient.create();
    }

    public String chat(String systemPrompt, String userMessage) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userMessage);

        String responseBody = restClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = mapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("LLM 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 입력 텍스트에서 요약과 태그를 JSON으로 추출.
     * 반환 형식: {"summary": "...", "tags": ["태그1", "태그2"]}
     */
    public String extractSummaryAndTags(String domainName, String rawInput) {
        String system = """
                당신은 개인 지식 관리 시스템의 분석 에이전트입니다.
                도메인: %s
                사용자가 입력한 텍스트에서 핵심 요약과 태그를 추출하세요.
                반드시 아래 JSON 형식으로만 응답하세요 (다른 텍스트 없이):
                {"summary": "핵심 요약 1-2문장", "tags": ["태그1", "태그2", "태그3"]}
                """.formatted(domainName);

        return chat(system, rawInput);
    }
}
