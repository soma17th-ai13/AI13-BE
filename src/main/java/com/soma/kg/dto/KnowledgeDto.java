package com.soma.kg.dto;

import com.soma.kg.entity.KnowledgeNode;
import java.time.LocalDateTime;

public class KnowledgeDto {

    public record AddRequest(String rawInput) {}

    public record ChatRequest(String question) {}

    public record ChatResponse(String answer) {}

    public record Response(Long id, String rawInput, String summary, String tags, LocalDateTime createdAt) {
        public static Response from(KnowledgeNode n) {
            return new Response(n.getId(), n.getRawInput(), n.getSummary(), n.getTags(), n.getCreatedAt());
        }
    }
}
