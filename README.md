# AI13-BE API 문서

> 프론트엔드 개발자를 위한 REST API 가이드

## 목차

- [기본 정보](#기본-정보)
- [공통 규칙](#공통-규칙)
- [도메인별 API](#도메인별-api)
  - [페르소나 (Persona)](#1-페르소나-persona)
  - [지식 추출 (Knowledge Extraction)](#2-지식-추출-knowledge-extraction)
  - [지식 그래프 (Knowledge Graph)](#3-지식-그래프-knowledge-graph)
  - [채팅 (Chat)](#4-채팅-chat)
  - [토론 (Discussion)](#5-토론-discussion)
- [주요 플로우](#주요-플로우)
- [에러 처리](#에러-처리)

---

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080` |
| Content-Type | `application/json` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

모든 요청/응답 본문은 JSON입니다.

---

## 공통 규칙

### 시간 형식

모든 시각 필드(`createdAt`, `updatedAt`)는 **ISO 8601 UTC** 형식입니다.

```
"2026-05-08T09:00:00Z"
```

### 에러 응답

실패 응답은 항상 아래 구조입니다.

```json
{
  "code": "PERSONA_NOT_FOUND",
  "message": "Persona not found"
}
```

| HTTP 상태 | 의미 |
|-----------|------|
| `400` | 요청값 누락 또는 공백 |
| `404` | 리소스를 찾을 수 없음 |
| `409` | 중복 또는 제약 위반 |
| `502` | Solar AI API 호출 실패 |

---

## 도메인별 API

---

### 1. 페르소나 (Persona)

AI 에이전트 역할을 하는 페르소나를 관리합니다. 페르소나는 특정 도메인(건강, 학업 등)을 담당하며, 채팅과 토론의 기반이 됩니다.

---

#### 페르소나 생성

`POST /api/personas`

도메인 이름을 입력하면 Solar AI가 페르소나 이름과 시스템 프롬프트를 자동 생성합니다.

**요청**

```json
{
  "domainName": "건강"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `domainName` | string | ✅ | 페르소나가 담당할 도메인 이름 |

**응답 `201 Created`**

```json
{
  "id": 1,
  "domainName": "건강",
  "name": "건강 코치 아리아",
  "systemPrompt": "당신은 수면·피로 전문 건강 코치입니다.",
  "builtIn": false,
  "enabled": true,
  "createdAt": "2026-05-08T09:00:00Z",
  "updatedAt": "2026-05-08T09:00:00Z"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 페르소나 ID |
| `domainName` | string | 담당 도메인 |
| `name` | string | AI가 생성한 페르소나 이름 |
| `systemPrompt` | string | 페르소나의 행동 지침 프롬프트 |
| `builtIn` | boolean | 기본 내장 페르소나 여부 (true이면 삭제 불가) |
| `enabled` | boolean | 활성화 여부 |
| `createdAt` | string | 생성 시각 |
| `updatedAt` | string | 수정 시각 |

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `domainName`이 null 또는 공백 |
| `409` | `DUPLICATE_PERSONA` | 동일 도메인 페르소나 이미 존재 |
| `502` | `PERSONA_PROMPT_GENERATION_FAILED` | Solar API 호출 실패 |

---

#### 페르소나 목록 조회

`GET /api/personas`

저장된 모든 페르소나를 반환합니다.

**응답 `200 OK`**

```json
[
  {
    "id": 1,
    "domainName": "건강",
    "name": "건강 코치 아리아",
    "systemPrompt": "당신은 수면·피로 전문 건강 코치입니다.",
    "builtIn": false,
    "enabled": true,
    "createdAt": "2026-05-08T09:00:00Z",
    "updatedAt": "2026-05-08T09:00:00Z"
  }
]
```

---

#### 페르소나 시스템 프롬프트 재생성

`POST /api/personas/{personaId}/regenerate`

Solar AI를 다시 호출해 시스템 프롬프트를 새로 만듭니다.

**경로 변수**

| 변수 | 타입 | 설명 |
|------|------|------|
| `personaId` | number | 재생성할 페르소나 ID |

**응답 `200 OK`** — 업데이트된 `PersonaResult`

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `PERSONA_NOT_FOUND` | 페르소나를 찾을 수 없음 |
| `502` | `PERSONA_PROMPT_GENERATION_FAILED` | Solar API 호출 실패 |

---

#### 페르소나 시스템 프롬프트 수정

`PUT /api/personas/{personaId}`

시스템 프롬프트를 직접 덮어씁니다.

**요청**

```json
{
  "systemPrompt": "당신은 수면·피로 전문 건강 코치입니다."
}
```

**응답 `200 OK`** — 업데이트된 `PersonaResult`

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `systemPrompt`가 null 또는 공백 |
| `404` | `PERSONA_NOT_FOUND` | 페르소나를 찾을 수 없음 |

---

#### 페르소나 삭제

`DELETE /api/personas/{personaId}`

페르소나를 삭제합니다. `builtIn: true`인 기본 내장 페르소나는 삭제할 수 없습니다.

**응답 `204 No Content`**

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `PERSONA_NOT_FOUND` | 페르소나를 찾을 수 없음 |
| `409` | `BUILT_IN_PERSONA_DELETION` | 기본 내장 페르소나 삭제 시도 |

---

### 2. 지식 추출 (Knowledge Extraction)

사용자의 자유 텍스트를 AI로 분석해 지식 그래프 노드와 엣지를 자동 생성합니다.

---

#### 개인 지식 추출 및 저장

`POST /api/knowledge/extractions`

일기, 메모 등 자유 텍스트를 입력하면 의미 있는 지식 노드와 관계(엣지)를 추출해 저장합니다.

**요청**

```json
{
  "text": "요즘 잠을 5시간밖에 못 자고 낮에 공부 집중이 잘 안 돼."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `text` | string | ✅ | 노드와 엣지로 추출할 자유 텍스트 |

**응답 `201 Created`**

```json
{
  "nodes": [
    {
      "id": 1,
      "title": "수면 부족",
      "content": "최근 3일간 수면 시간이 5시간 이하로 줄었다.",
      "domainName": "건강",
      "nodeType": "USER_INPUT",
      "analyzed": false,
      "createdAt": "2026-05-08T09:00:00Z",
      "updatedAt": "2026-05-08T09:00:00Z"
    }
  ],
  "edges": [
    {
      "id": 10,
      "sourceNodeId": 1,
      "targetNodeId": 2,
      "relationType": "AFFECTS",
      "confidence": 0.82,
      "evidenceText": "수면 부족과 집중도 저하가 같은 입력에서 함께 언급됨",
      "createdAt": "2026-05-08T09:00:00Z",
      "updatedAt": "2026-05-08T09:00:00Z"
    }
  ],
  "suggestedDomains": ["업무"]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `nodes` | array | 추출된 지식 노드 목록 |
| `edges` | array | 추출된 관계(엣지) 목록 |
| `suggestedDomains` | string[] | 기존 페르소나 도메인에 없는 신규 후보 도메인. 이 값을 이용해 새 페르소나 생성을 유도할 수 있습니다. |

> **`suggestedDomains` 활용 팁**: 응답에 suggestedDomains가 있으면 "새 도메인이 발견됐어요. 페르소나를 추가할까요?" 같은 UX를 제공할 수 있습니다.

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `text`가 null 또는 공백 |
| `502` | `KNOWLEDGE_EXTRACTION_FAILED` | Solar AI 호출 또는 파싱 실패 |

---

**노드 필드 상세**

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 노드 ID |
| `title` | string | 노드 제목 |
| `content` | string | 노드 본문 |
| `domainName` | string | 노드의 주 도메인 |
| `nodeType` | string | 노드 유형 (`USER_INPUT` 등) |
| `analyzed` | boolean | 에이전트 처리 완료 여부 |

**엣지 필드 상세**

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 엣지 ID |
| `sourceNodeId` | number | 출발 노드 ID |
| `targetNodeId` | number | 도착 노드 ID |
| `relationType` | string | 관계 유형 (예: `AFFECTS`) |
| `confidence` | number | 관계 신뢰도 (0~1) |
| `evidenceText` | string | 관계 판단 근거 |

---

### 3. 지식 그래프 (Knowledge Graph)

저장된 지식 노드를 조회하고 그래프 시각화에 필요한 데이터를 제공합니다.

---

#### 지식 노드 목록 조회

`GET /api/knowledge/nodes`

저장된 지식 노드를 최신순으로 조회합니다. `domainName` 쿼리 파라미터로 필터링할 수 있습니다.

**쿼리 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `domainName` | string | ❌ | 필터링할 도메인 이름. 없으면 전체 조회. |

**예시**

```
GET /api/knowledge/nodes
GET /api/knowledge/nodes?domainName=건강
```

**응답 `200 OK`** — `KnowledgeNodeResult` 배열

---

#### 지식 노드 단건 조회

`GET /api/knowledge/nodes/{nodeId}`

노드 ID로 단일 지식 노드를 조회합니다.

**응답 `200 OK`** — `KnowledgeNodeResult`

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `KNOWLEDGE_NODE_NOT_FOUND` | 노드를 찾을 수 없음 |

---

#### 1-hop 그래프 조회

`GET /api/knowledge/nodes/{nodeId}/graph`

중심 노드와 직접 연결된 이웃 노드 및 엣지를 한 번에 반환합니다. 그래프 캔버스 렌더링이나 토론 컨텍스트 선택에 활용하세요.

**응답 `200 OK`**

```json
{
  "centerNode": {
    "id": 1,
    "title": "수면 부족",
    ...
  },
  "nodes": [
    { "id": 1, "title": "수면 부족", ... },
    { "id": 2, "title": "집중력 저하", ... }
  ],
  "edges": [
    {
      "id": 10,
      "sourceNodeId": 1,
      "targetNodeId": 2,
      "relationType": "AFFECTS",
      "confidence": 0.82,
      "evidenceText": "...",
      ...
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `centerNode` | object | 기준 중심 노드 |
| `nodes` | array | 중심 노드 + 1-hop 이웃 노드 전체 (중심 노드 포함) |
| `edges` | array | 중심 노드와 직접 연결된 엣지 |

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `KNOWLEDGE_NODE_NOT_FOUND` | 중심 노드를 찾을 수 없음 |

---

### 4. 채팅 (Chat)

특정 페르소나와 1:1 대화 세션을 만들고 메시지를 주고받습니다.

---

#### 채팅 세션 생성

`POST /api/chats`

페르소나와 연결된 채팅 세션을 생성합니다.

**요청**

```json
{
  "personaId": 1,
  "title": "나의 건강 일지"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `personaId` | number | ✅ | 연결할 페르소나 ID |
| `title` | string | ✅ | 채팅방 제목 |

**응답 `201 Created`**

```json
{
  "id": 1,
  "personaId": 1,
  "personaDomain": "건강",
  "title": "나의 건강 일지",
  "createdAt": "2026-05-08T09:00:00Z"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 세션 ID (이후 메시지 전송에 사용) |
| `personaId` | number | 연결된 페르소나 ID |
| `personaDomain` | string | 페르소나 도메인 이름 |
| `title` | string | 채팅방 제목 |
| `createdAt` | string | 생성 시각 |

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `title`이 null 또는 공백 |
| `404` | `PERSONA_NOT_FOUND` | 페르소나를 찾을 수 없음 |

---

#### 메시지 전송

`POST /api/chats/{sessionId}/messages`

사용자 메시지를 전송하면 페르소나 AI가 응답합니다. 응답으로 AI의 답변 메시지가 반환됩니다.

**경로 변수**

| 변수 | 타입 | 설명 |
|------|------|------|
| `sessionId` | number | 채팅 세션 ID |

**요청**

```json
{
  "content": "나 요즘 피곤해"
}
```

**응답 `201 Created`** — AI의 답변 메시지

```json
{
  "id": 10,
  "sequence": 1,
  "role": "ASSISTANT",
  "content": "최근에 수면 시간이 줄었나요? 조금 더 이야기해 주세요.",
  "createdAt": "2026-05-08T09:00:01Z"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 메시지 ID |
| `sequence` | number | 세션 내 메시지 순서 (0부터 시작) |
| `role` | string | `USER` / `ASSISTANT` / `SYSTEM` |
| `content` | string | 메시지 내용 |
| `createdAt` | string | 생성 시각 |

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `content`가 null 또는 공백 |
| `404` | `CHAT_SESSION_NOT_FOUND` | 채팅 세션을 찾을 수 없음 |
| `502` | `SOLAR_RESPONSE_EMPTY` | Solar API 응답 오류 |

---

#### 채팅 히스토리 조회

`GET /api/chats/{sessionId}/messages`

해당 세션의 전체 메시지를 `sequence` 순서대로 반환합니다.

**응답 `200 OK`** — `ChatMessageResult` 배열

```json
[
  { "id": 9, "sequence": 0, "role": "USER", "content": "나 요즘 피곤해", ... },
  { "id": 10, "sequence": 1, "role": "ASSISTANT", "content": "최근에 수면 시간이 ...", ... }
]
```

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `CHAT_SESSION_NOT_FOUND` | 채팅 세션을 찾을 수 없음 |

---

### 5. 토론 (Discussion)

여러 페르소나가 특정 주제를 놓고 3라운드 토론을 진행합니다.

> ⚠️ **주의**: 토론 생성은 Solar AI를 여러 번 호출하는 **동기 처리**입니다. 응답까지 수 초~수십 초가 소요될 수 있습니다. 로딩 인디케이터를 꼭 표시해 주세요.

---

#### 토론 생성 및 실행

`POST /api/discussions`

주제, 지식 노드, 참여 페르소나를 지정해 토론을 시작합니다. 3라운드(분석 → 반박 → 종합)가 완료된 결과를 반환합니다.

**요청**

```json
{
  "topic": "요즘 피곤한 이유를 건강과 학업 관점에서 분석해줘",
  "knowledgeNodeId": 1,
  "personaIds": [1, 2]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `topic` | string | ✅ | 토론 주제 |
| `knowledgeNodeId` | number \| null | ❌ | 근거로 사용할 지식 노드 ID. 없으면 주제만으로 토론합니다. |
| `personaIds` | number[] \| null | ❌ | 참여할 페르소나 ID 목록. 비어 있으면 활성화된 모든 페르소나가 참여합니다. 최소 2명 필요. |

**응답 `201 Created`**

```json
{
  "id": 1,
  "triggerNodeId": 1,
  "status": "COMPLETED",
  "title": "요즘 피곤한 이유 분석",
  "summary": "수면 부족과 학업 부담이 함께 작용했습니다.",
  "actionPlan": "1. 수면 시간을 기록합니다.\n2. 집중 시간대를 파악합니다.",
  "createdAt": "2026-05-08T09:00:00Z",
  "messages": [
    {
      "id": 10,
      "personaId": 1,
      "personaName": "건강 코치 아리아",
      "round": "ANALYSIS",
      "content": "건강 관점에서 보면 수면 부족이 주원인입니다.",
      "createdAt": "2026-05-08T09:00:01Z"
    },
    {
      "id": 11,
      "personaId": 2,
      "personaName": "학업 멘토 진우",
      "round": "ANALYSIS",
      "content": "학업 과부하도 중요한 원인입니다.",
      "createdAt": "2026-05-08T09:00:02Z"
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 토론 ID |
| `triggerNodeId` | number \| null | 근거로 사용한 지식 노드 ID |
| `status` | string | 토론 상태 (`COMPLETED` / `FAILED`) |
| `title` | string | AI가 생성한 토론 제목 |
| `summary` | string | 최종 요약 |
| `actionPlan` | string | AI가 생성한 실행 계획 |
| `createdAt` | string | 생성 시각 |
| `messages` | array | 라운드별 메시지 목록 |

**메시지 필드 상세**

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 메시지 ID |
| `personaId` | number \| null | 페르소나 ID. 서버 합성 메시지는 null |
| `personaName` | string \| null | 페르소나 이름. 서버 합성 메시지는 null |
| `round` | string | `ANALYSIS` / `REBUTTAL` / `SYNTHESIS` |
| `content` | string | 해당 라운드 발언 내용 |
| `createdAt` | string | 생성 시각 |

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `400` | `INVALID_REQUEST` | `topic`이 공백이거나 참여 페르소나가 2명 미만 |
| `404` | `KNOWLEDGE_NODE_NOT_FOUND` | 지식 노드를 찾을 수 없음 |
| `404` | `PERSONA_NOT_FOUND` | 페르소나를 찾을 수 없음 |
| `502` | `SOLAR_RESPONSE_EMPTY` | Solar API 응답 오류 |

---

#### 토론 상세 조회

`GET /api/discussions/{discussionId}`

토론의 최종 요약, 실행 계획, 라운드별 메시지를 조회합니다.

**응답 `200 OK`** — `DiscussionResult` (위와 동일 구조)

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `DISCUSSION_NOT_FOUND` | 토론을 찾을 수 없음 |

---

#### 토론 메시지만 조회

`GET /api/discussions/{discussionId}/messages`

라운드별 메시지만 생성 순서대로 반환합니다.

**응답 `200 OK`** — `DiscussionMessageResult` 배열

**에러**

| 상태 | code | 원인 |
|------|------|------|
| `404` | `DISCUSSION_NOT_FOUND` | 토론을 찾을 수 없음 |

---

## 주요 플로우

### 플로우 1: 지식 기록 → 그래프 시각화

```
1. POST /api/knowledge/extractions   ← 사용자 텍스트 입력
2. GET  /api/knowledge/nodes         ← 전체 노드 목록 조회 (도메인 필터 가능)
3. GET  /api/knowledge/nodes/{id}/graph ← 특정 노드 주변 그래프 조회
```

- `suggestedDomains`가 있으면 새 페르소나 생성 제안 UX를 추가하세요.

---

### 플로우 2: 페르소나 채팅

```
1. POST /api/personas                ← 페르소나 생성 (도메인 입력)
2. POST /api/chats                   ← 채팅 세션 생성 (personaId 필요)
3. POST /api/chats/{sessionId}/messages  ← 메시지 전송 (AI 응답 반환)
4. GET  /api/chats/{sessionId}/messages  ← 히스토리 조회 (전체 대화 렌더링)
```

---

### 플로우 3: 멀티 페르소나 토론

```
1. GET  /api/personas                ← 참여할 페르소나 목록 선택
2. GET  /api/knowledge/nodes         ← 근거로 사용할 지식 노드 선택 (선택 사항)
3. POST /api/discussions             ← 토론 생성 (수 초 소요, 로딩 UI 필수)
4. GET  /api/discussions/{id}        ← 토론 결과 재조회 (저장 후 접근)
```

---

## 에러 처리

모든 에러는 동일한 형식으로 반환됩니다.

```json
{
  "code": "에러코드",
  "message": "에러 메시지"
}
```

**전체 에러 코드 목록**

| code | HTTP | 설명 |
|------|------|------|
| `INVALID_REQUEST` | 400 | 요청값 누락 또는 공백 |
| `PERSONA_NOT_FOUND` | 404 | 페르소나를 찾을 수 없음 |
| `KNOWLEDGE_NODE_NOT_FOUND` | 404 | 지식 노드를 찾을 수 없음 |
| `CHAT_SESSION_NOT_FOUND` | 404 | 채팅 세션을 찾을 수 없음 |
| `DISCUSSION_NOT_FOUND` | 404 | 토론을 찾을 수 없음 |
| `DUPLICATE_PERSONA` | 409 | 동일 도메인 페르소나 중복 |
| `BUILT_IN_PERSONA_DELETION` | 409 | 기본 내장 페르소나 삭제 시도 |
| `PERSONA_PROMPT_GENERATION_FAILED` | 502 | Solar API 프롬프트 생성 실패 |
| `KNOWLEDGE_EXTRACTION_FAILED` | 502 | Solar AI 지식 추출 실패 |
| `SOLAR_RESPONSE_EMPTY` | 502 | Solar API 빈 응답 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 |
