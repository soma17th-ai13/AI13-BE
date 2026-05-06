package com.soma.kg.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_nodes")
public class KnowledgeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawInput;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 500)
    private String tags;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public KnowledgeNode() {}

    public KnowledgeNode(Domain domain, String rawInput, String summary, String tags) {
        this.domain = domain;
        this.rawInput = rawInput;
        this.summary = summary;
        this.tags = tags;
    }

    public Long getId() { return id; }
    public Domain getDomain() { return domain; }
    public String getRawInput() { return rawInput; }
    public String getSummary() { return summary; }
    public String getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSummary(String summary) { this.summary = summary; }
    public void setTags(String tags) { this.tags = tags; }
}
