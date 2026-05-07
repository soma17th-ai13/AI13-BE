package com.soma.ai13be.domain.knowledge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.soma.ai13be.common.dto.ErrorResponse;
import com.soma.ai13be.domain.knowledge.dto.request.CreateKnowledgeEdgeCommand;
import com.soma.ai13be.domain.knowledge.dto.request.CreateKnowledgeNodeCommand;
import com.soma.ai13be.domain.knowledge.dto.response.KnowledgeEdgeResult;
import com.soma.ai13be.domain.knowledge.dto.response.KnowledgeGraphResult;
import com.soma.ai13be.domain.knowledge.dto.response.KnowledgeNodeResult;
import com.soma.ai13be.domain.knowledge.service.KnowledgeGraphService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Knowledge Graph", description = "RDB 기반 지식 그래프 노드/엣지 관리 API")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeGraphController {

	private final KnowledgeGraphService knowledgeGraphService;

	@Operation(
		summary = "지식 노드 생성",
		description = "사용자 입력, 인사이트, 실행 계획 등을 그래프 노드로 저장합니다. 생성된 노드는 기본적으로 analyzed=false 상태입니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "노드 생성 성공",
			content = @Content(schema = @Schema(implementation = KnowledgeNodeResult.class))),
		@ApiResponse(responseCode = "400", description = "필수 요청값 누락 또는 공백",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/nodes")
	public ResponseEntity<KnowledgeNodeResult> createNode(@RequestBody CreateKnowledgeNodeCommand command) {
		validateCreateNodeCommand(command);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(knowledgeGraphService.createNodeResult(command));
	}

	@Operation(
		summary = "지식 노드 목록 조회",
		description = "저장된 지식 노드를 최신순으로 조회합니다. domainName을 전달하면 해당 도메인의 노드만 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "노드 목록 조회 성공",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = KnowledgeNodeResult.class))))
	})
	@GetMapping("/nodes")
	public ResponseEntity<List<KnowledgeNodeResult>> findNodes(
		@Parameter(description = "필터링할 도메인 이름", example = "건강")
		@RequestParam(required = false) String domainName
	) {
		return ResponseEntity.ok(knowledgeGraphService.findNodeResults(domainName));
	}

	@Operation(
		summary = "지식 노드 상세 조회",
		description = "노드 ID로 단일 지식 노드를 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "노드 조회 성공",
			content = @Content(schema = @Schema(implementation = KnowledgeNodeResult.class))),
		@ApiResponse(responseCode = "404", description = "노드를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/nodes/{nodeId}")
	public ResponseEntity<KnowledgeNodeResult> findNode(
		@Parameter(description = "조회할 노드 ID", example = "1")
		@PathVariable Long nodeId
	) {
		return ResponseEntity.ok(knowledgeGraphService.findNodeResult(nodeId));
	}

	@Operation(
		summary = "지식 엣지 생성",
		description = "두 지식 노드 사이의 방향성 관계를 생성합니다. sourceNodeId에서 targetNodeId로 향하는 엣지를 저장합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "엣지 생성 성공",
			content = @Content(schema = @Schema(implementation = KnowledgeEdgeResult.class))),
		@ApiResponse(responseCode = "400", description = "필수 요청값 누락 또는 공백",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "출발 또는 도착 노드를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/edges")
	public ResponseEntity<KnowledgeEdgeResult> createEdge(@RequestBody CreateKnowledgeEdgeCommand command) {
		validateCreateEdgeCommand(command);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(knowledgeGraphService.createEdgeResult(command));
	}

	@Operation(
		summary = "특정 노드 주변 1-hop 그래프 조회",
		description = "중심 노드와 직접 연결된 엣지, 그리고 해당 엣지의 양 끝 노드를 반환합니다. 그래프 캔버스 표시와 토론 컨텍스트 구성의 기초 조회로 사용할 수 있습니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "1-hop 그래프 조회 성공",
			content = @Content(schema = @Schema(implementation = KnowledgeGraphResult.class))),
		@ApiResponse(responseCode = "404", description = "중심 노드를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/nodes/{nodeId}/graph")
	public ResponseEntity<KnowledgeGraphResult> findOneHopGraph(
		@Parameter(description = "중심 노드 ID", example = "1")
		@PathVariable Long nodeId
	) {
		return ResponseEntity.ok(knowledgeGraphService.findOneHopGraph(nodeId));
	}

	private void validateCreateNodeCommand(CreateKnowledgeNodeCommand command) {
		if (command == null || !StringUtils.hasText(command.title())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
		}
		if (!StringUtils.hasText(command.content())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content must not be blank");
		}
		if (!StringUtils.hasText(command.domainName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "domainName must not be blank");
		}
		if (!StringUtils.hasText(command.nodeType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nodeType must not be blank");
		}
	}

	private void validateCreateEdgeCommand(CreateKnowledgeEdgeCommand command) {
		if (command == null || command.sourceNodeId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceNodeId must not be null");
		}
		if (command.targetNodeId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetNodeId must not be null");
		}
		if (!StringUtils.hasText(command.relationType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "relationType must not be blank");
		}
	}
}
