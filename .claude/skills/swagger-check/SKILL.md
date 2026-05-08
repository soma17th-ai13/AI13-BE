---
name: swagger-check
description: Use when verifying Swagger/OpenAPI annotation completeness in Spring Boot controllers and DTOs before creating a PR, or when asked to check API specification coverage. Applies to springdoc-openapi projects using @Operation, @ApiResponses, @Parameter, @Tag, @Schema.
---

# Swagger 명세 점검

## Overview

PR 생성 전, 변경된 Spring Boot 컨트롤러와 DTO 파일의 Swagger/OpenAPI 어노테이션 누락·품질 문제를 탐지한다.
**빈 문자열(`""`)은 누락으로 처리한다.**

## Process

### 1. 변경 파일 수집

```bash
git diff --name-only origin/main...HEAD \
  | grep -E "Controller\.java$|Request\.java$|Response\.java$|Result\.java$|Command\.java$|DTO\.java$"
```

파일이 없으면 "변경된 API 파일 없음"으로 종료.

### 2. 컨트롤러 파일 점검

각 Controller 파일을 읽고 아래를 확인.

| 위치 | 어노테이션 | 합격 조건 |
|---|---|---|
| 클래스 | `@Tag` | `name` + `description` 모두 비어있지 않음 |
| 각 public 엔드포인트 메서드 | `@Operation` | `summary` + `description` 모두 비어있지 않음 |
| 각 public 엔드포인트 메서드 | `@ApiResponses` | 2xx 성공 코드 + 관련 4xx 이상 오류 코드 포함 |
| `@PathVariable` / `@RequestParam` 있는 파라미터 | `@Parameter` | `description` 비어있지 않음 |

**추가 검사 — 실제 throw 코드 일치:**
컨트롤러가 호출하는 Service 파일을 찾아 읽고, **명시적으로 throw되는 Custom 예외의 HTTP 코드**(404, 400, 502 등)가 `@ApiResponses`에 문서화되어 있는지 대조한다.
- 기준: `CustomException`, `BusinessException` 등 도메인 예외 클래스가 throw되는 경우만 포함
- 기준 외: `RestClientException`, `NullPointerException` 등 미처리 런타임 예외가 500으로 빠지는 경우는 제외 (권고 사항으로 메모만)

### 3. DTO 파일 점검

Request/Response/Result/Command 파일을 읽고:

- **Request DTO도 Response DTO와 동일하게 점검** — 요청 바디에 포함되는 모든 필드에 `@Schema` 필요
- `@Schema(description = "...")` — 필드(또는 record 파라미터) 각각에 `description` 비어있지 않으면 합격
- `example` 필드는 선택 사항 — 없어도 합격
- class-level `@Schema`는 선택 사항 — 필드 레벨만 있으면 합격

### 4. 결과 리포트

파일별, 메서드별 결과를 체크리스트로 출력 후 전체 요약:

```
## Swagger 명세 점검 결과

### ChatController.java
- [@Tag] ✅ 있음
- [createSession] @Operation ✅ summary + description 있음
- [createSession] @ApiResponses ✅ 201, 400, 404 포함
- [sendMessage] @ApiResponses ⚠️ 실제 throw 502 (SolarApiException) 미문서화
- [sendMessage] @Parameter ✅ sessionId 있음

### CreateChatSessionCommand.java (Request DTO)
- personaId ⚠️ @Schema 없음
- title ⚠️ @Schema 없음

### ChatMessageResult.java (Response DTO)
- content ✅ @Schema 있음
- ...

---
✅ 통과: 10 / 13  ❌ 미완성: 3개
```

## Common Mistakes

- **Request DTO를 빠뜨리는 것** — Request/Command DTO도 Response DTO와 동일하게 `@Schema` 점검
- **빈 값 통과** — `@Operation(summary = "")` 처럼 어노테이션은 있어도 값이 빈 경우는 누락
- **Service 코드 미확인** — 컨트롤러만 읽고 실제 throw 코드를 대조하지 않으면 502 같은 케이스를 놓침
- **`@PathVariable`에 `@Parameter` 생략** — 파라미터 하나라도 빠지면 미완성

## 범위 외 항목

점검하지 않아도 되는 항목 (별도 작업):
- `@SecurityRequirement` (인증 미구현 시)
- `@Content(mediaType = ...)` 명시 여부
- Bean Validation (`@NotBlank`, `@NotNull`) 어노테이션
