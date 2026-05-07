package com.soma.ai13be.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.soma.ai13be.common.client.dto.SolarChatMessage;
import com.soma.ai13be.knowledge.entity.KnowledgeNode;
import com.soma.ai13be.knowledge.repository.KnowledgeNodeRepository;

class KnowledgeContextBuilderTest {

    private final KnowledgeNodeRepository nodeRepository = mock(KnowledgeNodeRepository.class);
    private final KnowledgeContextBuilder builder = new KnowledgeContextBuilder(nodeRepository);

    @Test
    void returnsEmptyWhenNoNodesExist() {
        when(nodeRepository.findByDomainNameOrderByCreatedAtDesc("건강")).thenReturn(List.of());

        Optional<SolarChatMessage> result = builder.buildContextMessage("건강");

        assertThat(result).isEmpty();
    }

    @Test
    void returnsSystemMessageWithFormattedNodes() {
        KnowledgeNode node1 = node("수면 패턴", "하루 5시간 수면");
        KnowledgeNode node2 = node("피로감", "오후에 집중력 저하");
        when(nodeRepository.findByDomainNameOrderByCreatedAtDesc("건강"))
            .thenReturn(List.of(node1, node2));

        Optional<SolarChatMessage> result = builder.buildContextMessage("건강");

        assertThat(result).isPresent();
        SolarChatMessage message = result.get();
        assertThat(message.role()).isEqualTo("system");
        assertThat(message.content()).contains("[사용자 지식 그래프 - 건강 도메인]");
        assertThat(message.content()).contains("수면 패턴");
        assertThat(message.content()).contains("하루 5시간 수면");
        assertThat(message.content()).contains("피로감");
        assertThat(message.content()).contains("오후에 집중력 저하");
    }

    @Test
    void limitsToMostRecent15Nodes() {
        List<KnowledgeNode> twentyNodes = IntStream.rangeClosed(1, 20)
            .mapToObj(i -> node("노드" + i, "내용" + i))
            .toList();
        when(nodeRepository.findByDomainNameOrderByCreatedAtDesc("건강"))
            .thenReturn(twentyNodes);

        Optional<SolarChatMessage> result = builder.buildContextMessage("건강");

        assertThat(result).isPresent();
        String content = result.get().content();
        assertThat(content).contains("노드1");
        assertThat(content).doesNotContain("노드16");
        assertThat(content).doesNotContain("노드20");
    }

    private KnowledgeNode node(String title, String content) {
        return KnowledgeNode.builder()
            .title(title)
            .content(content)
            .domainName("건강")
            .nodeType("USER_INPUT")
            .analyzed(false)
            .build();
    }
}
