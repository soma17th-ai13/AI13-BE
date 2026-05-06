package com.soma.ai13be.domain.persona.entity;

import com.soma.ai13be.domain.common.BaseTimeEntity;
import com.soma.ai13be.domain.knowledge.entity.DomainType;
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
 * 사용자별 도메인 전문 에이전트 설정이다.
 * 시스템 프롬프트를 DB에 저장해 기본 페르소나뿐 아니라 사용자 커스텀 페르소나도 코드 변경 없이 지원한다.
 */
@Getter
@Entity
@Table(name = "personas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Persona extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private UserAccount owner;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DomainType domainType;

	// Solar API 호출 시 사용할 시스템 프롬프트
	@Lob
	@Column(nullable = false)
	private String systemPrompt;

	// 현재 사용 가능한 페르소나인지 여부
	@Column(nullable = false)
	private boolean enabled;

	@Builder
	private Persona(UserAccount owner, String name, DomainType domainType, String systemPrompt, boolean enabled) {
		this.owner = owner;
		this.name = name;
		this.domainType = domainType;
		this.systemPrompt = systemPrompt;
		this.enabled = enabled;
	}
}
