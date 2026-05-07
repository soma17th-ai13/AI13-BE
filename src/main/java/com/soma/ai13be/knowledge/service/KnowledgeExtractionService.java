package com.soma.ai13be.knowledge.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soma.ai13be.common.client.SolarApiClient;
import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.common.client.dto.SolarChatRequest;
import com.soma.ai13be.knowledge.dto.request.CreateKnowledgeEdgeCommand;
import com.soma.ai13be.knowledge.dto.request.CreateKnowledgeNodeCommand;
import com.soma.ai13be.knowledge.dto.request.ExtractKnowledgeCommand;
import com.soma.ai13be.knowledge.dto.response.KnowledgeEdgeResult;
import com.soma.ai13be.knowledge.dto.response.KnowledgeExtractionResult;
import com.soma.ai13be.knowledge.dto.response.KnowledgeNodeResult;
import com.soma.ai13be.knowledge.entity.KnowledgeEdge;
import com.soma.ai13be.knowledge.entity.KnowledgeNode;
import com.soma.ai13be.knowledge.exception.KnowledgeExtractionException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KnowledgeExtractionService {

	private static final String SYSTEM_PROMPT = """
		너는 개인 지식 그래프 추출기다.
		사용자의 자유 텍스트에서 개인 지식 노드와 노드 사이의 방향성 관계만 추출한다.
		반드시 아래 JSON 형식만 반환하고, 설명 문장이나 마크다운 코드 블록은 포함하지 않는다.
		{
		  "nodes": [
		    {
		      "title": "짧은 노드 제목",
		      "content": "원문에 근거한 구체적 설명",
		      "domainName": "건강|학습|금융|취미|업무|기타 중 하나",
		      "nodeType": "USER_INPUT"
		    }
		  ],
		  "edges": [
		    {
		      "sourceNodeIndex": 0,
		      "targetNodeIndex": 1,
		      "relationType": "AFFECTS|CAUSES|RELATED_TO|IMPROVES|WORSENS|TRIGGERS 중 하나",
		      "confidence": 0.0,
		      "evidenceText": "관계를 판단한 원문 근거"
		    }
		  ]
		}
		노드는 최대 5개, 엣지는 최대 6개로 제한한다.
		근거가 약한 관계는 만들지 않는다.
		""";

	private final SolarApiClient solarApiClient;
	private final KnowledgeGraphService knowledgeGraphService;
	private final ObjectMapper objectMapper;

	@Transactional
	public KnowledgeExtractionResult extractAndStore(ExtractKnowledgeCommand command) {
		validateCommand(command);

		String content = solarApiClient.chatCompletion(new SolarChatRequest(
			List.of(
				SolarChatMessage.system(SYSTEM_PROMPT),
				SolarChatMessage.user(command.text().strip())
			),
			0.0,
			1400
		)).firstContent();

		ExtractedKnowledge extractedKnowledge = parseExtraction(content);
		List<KnowledgeNode> savedNodes = saveNodes(extractedKnowledge.nodes());
		List<KnowledgeEdge> savedEdges = saveEdges(savedNodes, extractedKnowledge.edges());

		return new KnowledgeExtractionResult(
			savedNodes.stream()
				.map(KnowledgeNodeResult::from)
				.toList(),
			savedEdges.stream()
				.map(KnowledgeEdgeResult::from)
				.toList()
		);
	}

	private List<KnowledgeNode> saveNodes(List<ExtractedNode> nodes) {
		if (nodes == null || nodes.isEmpty()) {
			throw new KnowledgeExtractionException("No knowledge nodes were extracted.");
		}

		List<KnowledgeNode> savedNodes = new ArrayList<>();
		for (ExtractedNode node : nodes) {
			if (!StringUtils.hasText(node.title())
				|| !StringUtils.hasText(node.content())
				|| !StringUtils.hasText(node.domainName())) {
				continue;
			}
			savedNodes.add(knowledgeGraphService.createNode(new CreateKnowledgeNodeCommand(
				node.title(),
				node.content(),
				node.domainName(),
				StringUtils.hasText(node.nodeType()) ? node.nodeType() : "USER_INPUT"
			)));
		}

		if (savedNodes.isEmpty()) {
			throw new KnowledgeExtractionException("No valid knowledge nodes were extracted.");
		}
		return savedNodes;
	}

	private List<KnowledgeEdge> saveEdges(List<KnowledgeNode> savedNodes, List<ExtractedEdge> edges) {
		if (edges == null || edges.isEmpty()) {
			return List.of();
		}

		List<KnowledgeEdge> savedEdges = new ArrayList<>();
		for (ExtractedEdge edge : edges) {
			if (!isValidEdge(edge, savedNodes.size())) {
				continue;
			}
			KnowledgeNode sourceNode = savedNodes.get(edge.sourceNodeIndex());
			KnowledgeNode targetNode = savedNodes.get(edge.targetNodeIndex());
			savedEdges.add(knowledgeGraphService.createEdge(new CreateKnowledgeEdgeCommand(
				sourceNode.getId(),
				targetNode.getId(),
				edge.relationType(),
				edge.confidence(),
				edge.evidenceText()
			)));
		}
		return savedEdges;
	}

	private boolean isValidEdge(ExtractedEdge edge, int nodeCount) {
		return edge != null
			&& edge.sourceNodeIndex() != null
			&& edge.targetNodeIndex() != null
			&& edge.sourceNodeIndex() >= 0
			&& edge.targetNodeIndex() >= 0
			&& edge.sourceNodeIndex() < nodeCount
			&& edge.targetNodeIndex() < nodeCount
			&& !edge.sourceNodeIndex().equals(edge.targetNodeIndex())
			&& StringUtils.hasText(edge.relationType());
	}

	private ExtractedKnowledge parseExtraction(String content) {
		if (!StringUtils.hasText(content)) {
			throw new KnowledgeExtractionException("Solar API returned an empty extraction result.");
		}
		try {
			return objectMapper.readValue(stripJsonFence(content), ExtractedKnowledge.class);
		} catch (Exception exception) {
			throw new KnowledgeExtractionException("Failed to parse knowledge extraction result.", exception);
		}
	}

	private String stripJsonFence(String content) {
		String stripped = content.strip();
		if (stripped.startsWith("```")) {
			stripped = stripped.replaceFirst("^```json\\s*", "");
			stripped = stripped.replaceFirst("^```\\s*", "");
			stripped = stripped.replaceFirst("\\s*```$", "");
		}
		return stripped.strip();
	}

	private void validateCommand(ExtractKnowledgeCommand command) {
		if (command == null || !StringUtils.hasText(command.text())) {
			throw new IllegalArgumentException("text must not be blank");
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ExtractedKnowledge(
		List<ExtractedNode> nodes,
		List<ExtractedEdge> edges
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ExtractedNode(
		String title,
		String content,
		String domainName,
		String nodeType
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ExtractedEdge(
		Integer sourceNodeIndex,
		Integer targetNodeIndex,
		String relationType,
		BigDecimal confidence,
		String evidenceText
	) {
	}
}
