package com.soma.ai13be.domain.discussion.entity;

import com.soma.ai13be.domain.common.BaseTimeEntity;
import com.soma.ai13be.domain.knowledge.entity.KnowledgeNode;
import com.soma.ai13be.domain.user.entity.UserAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 교차 도메인 패턴 감지 또는 사용자 요청으로 시작된 멀티 에이전트 토론이다.
 * 최종 요약과 실행 계획은 라운드별 메시지를 매번 재조합하지 않고 바로 조회할 수 있도록 별도 컬럼에 저장한다.
 */
@Getter
@Entity
@Table(name = "agent_discussions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentDiscussion extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private UserAccount owner;

	// 토론을 시작하게 만든 지식 노드
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trigger_node_id")
	private KnowledgeNode triggerNode;

	// 토론 상태
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DiscussionStatus status;

	// 토론 제목
	@Column(nullable = false, length = 255)
	private String title;

	// 최종 요약
	@Lob
	@Column
	private String summary;

	// 실행 계획
	@Lob
	@Column
	private String actionPlan;

	@Builder
	private AgentDiscussion(
		UserAccount owner,
		KnowledgeNode triggerNode,
		DiscussionStatus status,
		String title,
		String summary,
		String actionPlan
	) {
		this.owner = owner;
		this.triggerNode = triggerNode;
		this.status = status == null ? DiscussionStatus.REQUESTED : status;
		this.title = title;
		this.summary = summary;
		this.actionPlan = actionPlan;
	}
}
