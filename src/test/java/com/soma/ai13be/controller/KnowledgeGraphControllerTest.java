package com.soma.ai13be.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.soma.ai13be.common.exception.GlobalExceptionHandler;
import com.soma.ai13be.knowledge.controller.KnowledgeGraphController;
import com.soma.ai13be.knowledge.dto.request.CreateKnowledgeEdgeCommand;
import com.soma.ai13be.knowledge.dto.request.CreateKnowledgeNodeCommand;
import com.soma.ai13be.knowledge.dto.response.KnowledgeEdgeResult;
import com.soma.ai13be.knowledge.dto.response.KnowledgeGraphResult;
import com.soma.ai13be.knowledge.dto.response.KnowledgeNodeResult;
import com.soma.ai13be.knowledge.exception.KnowledgeNodeNotFoundException;
import com.soma.ai13be.knowledge.service.KnowledgeGraphService;

class KnowledgeGraphControllerTest {

	private final KnowledgeGraphService knowledgeGraphService = org.mockito.Mockito.mock(KnowledgeGraphService.class);
	private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new KnowledgeGraphController(knowledgeGraphService))
		.setControllerAdvice(new GlobalExceptionHandler())
		.build();

	@Test
	void createsNode() throws Exception {
		when(knowledgeGraphService.createNodeResult(any(CreateKnowledgeNodeCommand.class)))
			.thenReturn(nodeResult(1L, "수면 부족", "건강"));

		mockMvc.perform(post("/api/knowledge/nodes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "수면 부족",
					  "content": "최근 수면 시간이 줄었다.",
					  "domainName": "건강",
					  "nodeType": "USER_INPUT"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.title").value("수면 부족"))
			.andExpect(jsonPath("$.domainName").value("건강"))
			.andExpect(jsonPath("$.nodeType").value("USER_INPUT"))
			.andExpect(jsonPath("$.analyzed").value(false));
	}

	@Test
	void rejectsBlankNodeTitle() throws Exception {
		mockMvc.perform(post("/api/knowledge/nodes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": " ",
					  "content": "최근 수면 시간이 줄었다.",
					  "domainName": "건강",
					  "nodeType": "USER_INPUT"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("title must not be blank"));
	}

	@Test
	void findsNodes() throws Exception {
		when(knowledgeGraphService.findNodeResults("건강"))
			.thenReturn(List.of(nodeResult(1L, "수면 부족", "건강")));

		mockMvc.perform(get("/api/knowledge/nodes")
				.param("domainName", "건강"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("수면 부족"));

		verify(knowledgeGraphService).findNodeResults("건강");
	}

	@Test
	void createsEdge() throws Exception {
		when(knowledgeGraphService.createEdgeResult(any(CreateKnowledgeEdgeCommand.class)))
			.thenReturn(edgeResult(10L, 1L, 2L, "AFFECTS"));

		mockMvc.perform(post("/api/knowledge/edges")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sourceNodeId": 1,
					  "targetNodeId": 2,
					  "relationType": "AFFECTS",
					  "confidence": 0.8200,
					  "evidenceText": "수면 부족과 집중도 저하가 함께 언급됨"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(10))
			.andExpect(jsonPath("$.sourceNodeId").value(1))
			.andExpect(jsonPath("$.targetNodeId").value(2))
			.andExpect(jsonPath("$.relationType").value("AFFECTS"))
			.andExpect(jsonPath("$.confidence").value(0.8200));
	}

	@Test
	void returnsNotFoundWhenCreatingEdgeForUnknownNode() throws Exception {
		when(knowledgeGraphService.createEdgeResult(any(CreateKnowledgeEdgeCommand.class)))
			.thenThrow(new KnowledgeNodeNotFoundException(2L));

		mockMvc.perform(post("/api/knowledge/edges")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sourceNodeId": 1,
					  "targetNodeId": 2,
					  "relationType": "AFFECTS"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Knowledge node not found: 2"));
	}

	@Test
	void findsOneHopGraph() throws Exception {
		when(knowledgeGraphService.findOneHopGraph(1L))
			.thenReturn(new KnowledgeGraphResult(
				nodeResult(1L, "수면 부족", "건강"),
				List.of(
					nodeResult(1L, "수면 부족", "건강"),
					nodeResult(2L, "집중도 저하", "학습")
				),
				List.of(edgeResult(10L, 1L, 2L, "AFFECTS"))
			));

		mockMvc.perform(get("/api/knowledge/nodes/{nodeId}/graph", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.centerNode.id").value(1))
			.andExpect(jsonPath("$.nodes[1].title").value("집중도 저하"))
			.andExpect(jsonPath("$.edges[0].relationType").value("AFFECTS"));
	}

	private KnowledgeNodeResult nodeResult(Long id, String title, String domainName) {
		return new KnowledgeNodeResult(
			id,
			title,
			title + " content",
			domainName,
			"USER_INPUT",
			false,
			null,
			null
		);
	}

	private KnowledgeEdgeResult edgeResult(Long id, Long sourceNodeId, Long targetNodeId, String relationType) {
		return new KnowledgeEdgeResult(
			id,
			sourceNodeId,
			targetNodeId,
			relationType,
			new BigDecimal("0.8200"),
			relationType + " evidence",
			null,
			null
		);
	}
}
