package com.soma.ai13be.knowledge.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.knowledge.entity.KnowledgeNode;
import com.soma.ai13be.knowledge.repository.KnowledgeNodeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeContextBuilder {

	private final KnowledgeNodeRepository nodeRepository;

	public Optional<SolarChatMessage> buildContextMessage(String domainName) {
		List<KnowledgeNode> nodes = nodeRepository
			.findTop15ByDomainNameOrderByCreatedAtDesc(domainName);

		if (nodes.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(SolarChatMessage.system(formatNodes(domainName, nodes)));
	}

	private String formatNodes(String domainName, List<KnowledgeNode> nodes) {
		StringBuilder sb = new StringBuilder();
		sb.append("[사용자 지식 그래프 - ").append(domainName).append(" 도메인]");
		for (int i = 0; i < nodes.size(); i++) {
			KnowledgeNode node = nodes.get(i);
			sb.append("\n").append(i + 1).append(". 제목: ").append(node.getTitle());
			sb.append("\n   내용: ").append(node.getContent());
		}
		return sb.toString();
	}
}
