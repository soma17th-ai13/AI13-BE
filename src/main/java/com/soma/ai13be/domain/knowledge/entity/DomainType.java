package com.soma.ai13be.domain.knowledge.entity;

/**
 * 지식 노드와 페르소나 라우팅에 사용하는 도메인 분류다.
 * 분류 신뢰도가 낮거나 특정 도메인으로 좁히기 어려운 입력은 GENERAL로 처리한다.
 */
public enum DomainType {
	HEALTH,
	STUDY,
	FINANCE,
	HOBBY,
	WORK,
	GENERAL
}
