package com.soma.ai13be.common.dto;

/**
 * API 오류 응답의 공통 포맷입니다.
 * 컨트롤러별로 에러 DTO를 만들지 않고 전역 예외 처리에서 재사용합니다.
 */
public record ErrorResponse(
	String message
) {
}
