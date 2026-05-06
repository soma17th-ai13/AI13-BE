package com.soma.ai13be.domain.user.entity;

import com.soma.ai13be.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인 지식 그래프 데이터를 사용자 단위로 분리하기 위한 소유자 엔티티다.
 * 인증 방식이 바뀌더라도 그래프, 채팅, 토론 기록은 항상 사용자에 귀속되어야 한다.
 */
@Getter
@Entity
@Table(name = "user_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(nullable = false, length = 100)
	private String name;

	@Builder
	private UserAccount(String email, String name) {
		this.email = email;
		this.name = name;
	}
}
