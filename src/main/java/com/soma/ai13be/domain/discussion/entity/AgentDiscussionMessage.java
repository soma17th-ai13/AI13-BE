package com.soma.ai13be.domain.discussion.entity;

import com.soma.ai13be.domain.common.BaseTimeEntity;
import com.soma.ai13be.domain.persona.entity.Persona;

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
 * 토론 라운드별 페르소나 원본 응답이다.
 * 최종 합성 결과가 이상할 때 어떤 페르소나의 어떤 라운드에서 문제가 생겼는지 추적하기 위해 보관한다.
 */
@Getter
@Entity
@Table(name = "agent_discussion_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentDiscussionMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "discussion_id", nullable = false)
	private AgentDiscussion discussion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "persona_id")
	private Persona persona;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DiscussionRound round;

	@Lob
	@Column(nullable = false)
	private String content;

	@Builder
	private AgentDiscussionMessage(
		AgentDiscussion discussion,
		Persona persona,
		DiscussionRound round,
		String content
	) {
		this.discussion = discussion;
		this.persona = persona;
		this.round = round;
		this.content = content;
	}
}
