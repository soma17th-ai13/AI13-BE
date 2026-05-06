package com.soma.kg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soma.kg.dto.KnowledgeDto;
import com.soma.kg.entity.Domain;
import com.soma.kg.entity.KnowledgeNode;
import com.soma.kg.repository.KnowledgeNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {

    private final KnowledgeNodeRepository nodeRepository;
    private final LlmService llmService;
    private final ObjectMapper mapper = new ObjectMapper();

    public KnowledgeService(KnowledgeNodeRepository nodeRepository, LlmService llmService) {
        this.nodeRepository = nodeRepository;
        this.llmService = llmService;
    }

    public KnowledgeDto.Response addKnowledge(Domain domain, String rawInput) {
        String llmResponse = llmService.extractSummaryAndTags(domain.getName(), rawInput);

        String summary = rawInput;
        String tags = "";
        try {
            // JSON 블록 추출 (```json ... ``` 처리)
            String json = llmResponse.trim();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end >= 0) {
                json = json.substring(start, end + 1);
            }
            JsonNode node = mapper.readTree(json);
            summary = node.path("summary").asText(rawInput);
            tags = node.path("tags").isArray()
                    ? buildTagString(node.path("tags"))
                    : node.path("tags").asText("");
        } catch (Exception ignored) {
            // LLM 응답 파싱 실패 시 원문을 summary로 사용
        }

        KnowledgeNode saved = nodeRepository.save(new KnowledgeNode(domain, rawInput, summary, tags));
        return KnowledgeDto.Response.from(saved);
    }

    public List<KnowledgeDto.Response> findByDomain(Long domainId) {
        return nodeRepository.findByDomainIdOrderByCreatedAtDesc(domainId).stream()
                .map(KnowledgeDto.Response::from)
                .toList();
    }

    public String buildContextForDomain(Long domainId) {
        List<KnowledgeNode> nodes = nodeRepository.findByDomainIdOrderByCreatedAtDesc(domainId);
        if (nodes.isEmpty()) return "(저장된 지식 없음)";
        return nodes.stream()
                .map(n -> "- " + n.getSummary() + (n.getTags() != null && !n.getTags().isEmpty() ? " [" + n.getTags() + "]" : ""))
                .collect(Collectors.joining("\n"));
    }

    public KnowledgeDto.ChatResponse chat(Domain domain, String question) {
        String context = buildContextForDomain(domain.getId());
        String system = """
                당신은 '%s' 도메인 전문 에이전트입니다.
                아래는 이 도메인에 저장된 지식입니다:
                %s

                위 지식을 바탕으로 사용자 질문에 전문적이고 친절하게 답하세요.
                """.formatted(domain.getName(), context);

        String answer = llmService.chat(system, question);
        return new KnowledgeDto.ChatResponse(answer);
    }

    private String buildTagString(JsonNode tagsNode) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode t : tagsNode) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(t.asText());
        }
        return sb.toString();
    }
}
