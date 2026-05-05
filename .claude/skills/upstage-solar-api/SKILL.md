---
name: upstage-solar-api
description: Use when integrating, using, or asking about Upstage Solar API, Solar LLM, Solar embedding, Document AI, or any upstage.ai service. Requires context7 documentation lookup before any implementation.
---

# Upstage Solar API Integration

## Overview

Upstage의 Solar API는 버전 업데이트와 엔드포인트 변경이 잦습니다. **context7로 공식 문서를 먼저 확인하지 않으면 deprecated API를 사용하거나 잘못된 파라미터를 전달할 위험이 있습니다.**

## 필수 규칙: 공식 문서 확인 우선

**Solar API 관련 작업을 시작하기 전에 반드시 공식 문서를 확인하세요. context7을 먼저 시도하고, 실패하면 웹 검색으로 대체하세요.**

```
NO SOLAR API IMPLEMENTATION WITHOUT CHECKING DOCS FIRST
```

## 적용 순서

1. **공식 문서 조회** — context7 먼저, 실패 시 웹 검색 (아래 방법 참고)
2. 공식 문서 기반으로 구현
3. 구현 완료 후 파라미터/엔드포인트 재확인

## 문서 조회 방법

### 1순위: context7

```
# 1단계: 라이브러리 ID 조회
mcp__plugin_context7_context7__resolve-library-id
  libraryName: "upstage"  또는  "upstage solar"

# 2단계: 문서 조회
mcp__plugin_context7_context7__query-docs
  context7CompatibleLibraryID: <1단계 결과>
  query: "solar chat completions"  # 필요한 기능 검색
```

### 2순위: 웹 검색 (context7 실패 시 필수 대체)

context7이 결과를 반환하지 못하거나 오류가 발생하면 **반드시** 웹 검색으로 대체합니다.

```
# 웹 검색 키워드 예시
"upstage solar api docs site:developers.upstage.ai"
"upstage solar chat completions api reference"
"upstage document ai ocr api"
```

공식 문서 URL: `https://developers.upstage.ai`

**웹 검색도 생략하는 것은 허용되지 않습니다.**

## 트리거 목록

이 스킬을 적용해야 하는 상황:

- `solar-pro3` 등 Solar 모델 사용
- `api.upstage.ai` 엔드포인트 연동
- Upstage embedding API (`solar-embedding-*`)
- Upstage Document AI (OCR, layout analysis)
- `UpstageAI` SDK 또는 OpenAI 호환 클라이언트로 Upstage 연결
- Solar API 키 설정 또는 환경변수 구성

## 흔한 실수

| 실수 | 현실 |
|------|------|
| "OpenAI 호환이니까 그냥 쓰면 된다" | 모델명, 엔드포인트, 지원 파라미터가 다름. context7 확인 필수. |
| "전에 쓴 코드 그대로 쓰면 된다" | Solar API는 업데이트가 잦음. 최신 문서 확인 필수. |
| "문서 안 봐도 대충 알겠다" | deprecated 파라미터나 변경된 응답 구조로 런타임 오류 발생. |
| "간단한 테스트니까 생략해도 된다" | 테스트 코드도 공식 파라미터 기준으로 작성해야 함. |

## Red Flags — 문서 확인 없이 진행하려는 신호

- "Solar API는 OpenAI랑 똑같으니까..."
- "빠르게 해보고 오류 나면 고치면 되니까..."
- "예전에 써봤으니까 알고 있어..."
- "간단한 거라서 문서 안 봐도..."
- "context7이 안 되니까 그냥 진행할게..."

**이런 생각이 들면 STOP — context7 먼저, 안 되면 웹 검색.**
