package com.soma.kg.dto;

import com.soma.kg.entity.Domain;
import java.time.LocalDateTime;

public class DomainDto {

    public record CreateRequest(String name, String description) {}

    public record Response(Long id, String name, String description, LocalDateTime createdAt) {
        public static Response from(Domain d) {
            return new Response(d.getId(), d.getName(), d.getDescription(), d.getCreatedAt());
        }
    }
}
