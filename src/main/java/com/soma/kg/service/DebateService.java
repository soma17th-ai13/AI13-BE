package com.soma.kg.service;

import com.soma.kg.entity.Domain;
import com.soma.kg.repository.DomainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebateService {

    private final DomainRepository domainRepository;
    private final KnowledgeService knowledgeService;
    private final LlmService llmService;

    public DebateService(DomainRepository domainRepository, KnowledgeService knowledgeService, LlmService llmService) {
        this.domainRepository = domainRepository;
        this.knowledgeService = knowledgeService;
        this.llmService = llmService;
    }

    public String runDebate() {
        List<Domain> domains = domainRepository.findAll();
        if (domains.isEmpty()) {
            return "토론할 도메인 데이터가 없습니다. 먼저 도메인을 생성하고 지식을 입력하세요.";
        }

        // 모든 도메인 지식을 하나의 컨텍스트로 합산
        String domainContext = domains.stream()
                .map(d -> {
                    String knowledge = knowledgeService.buildContextForDomain(d.getId());
                    return "[%s 에이전트]\n%s".formatted(d.getName(), knowledge);
                })
                .collect(Collectors.joining("\n\n"));

        String domainNames = domains.stream().map(Domain::getName).collect(Collectors.joining(", "));

        String system = """
                당신은 멀티 에이전트 토론 오케스트레이터입니다.
                각 도메인 에이전트(%s)가 보유한 지식을 바탕으로 3라운드 구조화 토론을 진행하세요.

                [Round 1 - 독립 분석]
                각 도메인 에이전트가 자신의 데이터에서 발견한 주요 패턴과 인사이트를 제시합니다.

                [Round 2 - 교차 도메인 반론]
                각 에이전트가 다른 도메인의 분석에 대해 자신의 관점에서 반론하거나 연관성을 제시합니다.

                [Round 3 - 합성 및 실행 계획]
                모든 도메인을 아우르는 교차 패턴을 종합하고, 사용자가 바로 실행할 수 있는 구체적 행동 계획 3가지를 제시합니다.

                각 라운드를 명확히 구분하여 출력하세요.
                """.formatted(domainNames);

        String userMessage = """
                다음은 각 도메인의 지식 데이터입니다:

                %s

                위 데이터를 바탕으로 3라운드 토론을 진행해 주세요.
                """.formatted(domainContext);

        return llmService.chat(system, userMessage);
    }
}
