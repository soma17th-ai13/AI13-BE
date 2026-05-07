package com.soma.ai13be.discussion.entity;

import com.soma.ai13be.common.entity.BaseTimeEntity;
import com.soma.ai13be.knowledge.entity.KnowledgeNode;

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
 * 하나의 멀티 페르소나 토론 실행 단위입니다.
 * 라운드별 상세 응답은 AgentDiscussionMessage에 저장하고, 최종 요약과 행동 계획만 이 엔티티에 보관합니다.
 */
@Getter
@Entity
@Table(name = "agent_discussions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentDiscussion extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trigger_node_id")
	private KnowledgeNode triggerNode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DiscussionStatus status;

	@Column(nullable = false, length = 255)
	private String title;

	@Lob
	@Column
	private String summary;

	@Lob
	@Column
	private String actionPlan;

	@Builder
	private AgentDiscussion(
		KnowledgeNode triggerNode,
		DiscussionStatus status,
		String title,
		String summary,
		String actionPlan
	) {
		this.triggerNode = triggerNode;
		this.status = status == null ? DiscussionStatus.REQUESTED : status;
		this.title = title;
		this.summary = summary;
		this.actionPlan = actionPlan;
	}
}
