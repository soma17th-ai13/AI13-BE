package com.soma.ai13be.knowledge.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인 지식 추출 및 저장 결과")
public record KnowledgeExtractionResult(
	@ArraySchema(schema = @Schema(implementation = KnowledgeNodeResult.class))
	List<KnowledgeNodeResult> nodes,

	@ArraySchema(schema = @Schema(implementation = KnowledgeEdgeResult.class))
	List<KnowledgeEdgeResult> edges
) {
	public KnowledgeExtractionResult {
		nodes = List.copyOf(nodes == null ? List.of() : nodes);
		edges = List.copyOf(edges == null ? List.of() : edges);
	}
}
