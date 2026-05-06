# Knowledge Graph Agent API 문서

베이스 URL: `http://localhost:8080`

---

## 도메인 (Domain)

### 도메인 생성
```
POST /api/domains
```
**Request Body**
```json
{
  "name": "건강",
  "description": "건강 및 수면 관련 데이터"
}
```
**Response** `201 Created`
```json
{
  "id": 1,
  "name": "건강",
  "description": "건강 및 수면 관련 데이터",
  "createdAt": "2026-05-04T14:33:33.412975"
}
```

---

### 도메인 목록 조회
```
GET /api/domains
```
**Response** `200 OK`
```json
[
  {
    "id": 1,
    "name": "건강",
    "description": "건강 및 수면 관련 데이터",
    "createdAt": "2026-05-04T14:33:33.412975"
  },
  {
    "id": 2,
    "name": "학업",
    "description": "학습 및 집중력 관련 데이터",
    "createdAt": "2026-05-04T14:33:40.058574"
  }
]
```

---

## 지식 노드 (Knowledge)

### 지식 입력 (AI 분석 후 저장)
```
POST /api/domains/{id}/knowledge
```
사용자가 입력한 텍스트를 LLM이 분석하여 **요약**과 **태그**를 자동 추출 후 저장합니다.

**Request Body**
```json
{
  "rawInput": "요즘 수면이 6시간 이하로 줄었고 아침에 일어나기 매우 힘들다"
}
```
**Response** `201 Created`
```json
{
  "id": 1,
  "rawInput": "요즘 수면이 6시간 이하로 줄었고 아침에 일어나기 매우 힘들다",
  "summary": "최근 수면 시간이 6시간 이하로 줄어들며 아침 기상에 어려움을 겪고 있음",
  "tags": "수면 부족, 피로, 아침 기상, 건강 관리",
  "createdAt": "2026-05-04T14:33:56.324438"
}
```

---

### 도메인 지식 전체 조회
```
GET /api/domains/{id}/knowledge
```
**Response** `200 OK`
```json
[
  {
    "id": 1,
    "rawInput": "요즘 수면이 6시간 이하로 줄었고 아침에 일어나기 매우 힘들다",
    "summary": "최근 수면 시간이 6시간 이하로 줄어들며 아침 기상에 어려움을 겪고 있음",
    "tags": "수면 부족, 피로, 아침 기상, 건강 관리",
    "createdAt": "2026-05-04T14:33:56.324438"
  }
]
```

---

### 도메인 페르소나와 대화
```
POST /api/domains/{id}/chat
```
저장된 도메인 지식 전체를 컨텍스트로 사용하여 해당 도메인 전문 에이전트가 답변합니다.

**Request Body**
```json
{
  "question": "내 수면 패턴에서 주의할 점은?"
}
```
**Response** `200 OK`
```json
{
  "answer": "현재 수면 시간이 6시간 이하로 성인 권장 수면 시간(7~9시간)에 미달합니다. ..."
}
```

---

## 토론 (Debate)

### 멀티 에이전트 토론 실행
```
POST /api/debate
```
모든 도메인의 지식 데이터를 단일 프롬프트에 통합하여 3라운드 구조화 토론을 진행합니다.

**Request Body** 없음

**Response** `200 OK`
```json
{
  "result": "[Round 1 - 독립 분석]\n건강 에이전트: ...\n학업 에이전트: ...\n\n[Round 2 - 교차 도메인 반론]\n...\n\n[Round 3 - 합성 및 실행 계획]\n..."
}
```

**토론 구조**
| 라운드 | 내용 |
|---|---|
| Round 1 | 각 도메인 에이전트 독립 분석 |
| Round 2 | 교차 도메인 반론 및 연관성 제시 |
| Round 3 | 종합 인사이트 + 실행 계획 3가지 |

---

## 빠른 테스트 (curl)

```bash
# 도메인 생성
curl -X POST http://localhost:8080/api/domains \
  -H "Content-Type: application/json" \
  -d '{"name":"건강","description":"건강 데이터"}'

# 지식 입력
curl -X POST http://localhost:8080/api/domains/1/knowledge \
  -H "Content-Type: application/json" \
  -d '{"rawInput":"요즘 수면이 부족하다"}'

# 도메인 채팅
curl -X POST http://localhost:8080/api/domains/1/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"수면 개선 방법은?"}'

# 토론 실행
curl -X POST http://localhost:8080/api/debate
```

---

## 서버 실행

```bash
cd SOMA/project
bash start.sh
```

- 포트: `8080`
- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/knowledgedb`)
