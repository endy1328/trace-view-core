# Trace View Core

Trace View Core는 Spring 애플리케이션의 `endpoint -> handler -> service -> repository / external call` 흐름을 정적 분석하고, 이를 검색/그래프/리뷰 API로 제공하는 백엔드 코어 프로젝트다.

기획 개요 문서는 [docs/product-overview.md](./docs/product-overview.md)로 분리했다.

## 현재 상태

- Spring 범용 코어 분석기 구현 완료
- graph / node detail / service chain 조회 API 구현 완료
- annotation 생성, approve, reject용 review API 구현 완료
- runtime trace session / event ingest / correlation API 구현 완료
- 정적 제품 레이어 UI 구현 완료
- 검색, 노드/엔드포인트 상세, 그래프, 리뷰 화면 제공
- 내장 샘플 프로젝트 기준 통합 테스트 완료
- adapter, 실제 외부 대상 코드베이스 연결은 아직 미구현

## 요구 사항

- Java 17
- Maven 3.9+

## 실행

프로젝트 루트에서:

```bash
mvn -Dmaven.repo.local=.m2/repository spring-boot:run
```

기본 실행 주소:

- `http://localhost:8080`
- `http://localhost:8080/`

## 테스트

전체 테스트:

```bash
mvn -Dmaven.repo.local=.m2/repository test
```

현재 기준 기대 결과:

- `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`

## 빠른 확인 순서

### 1. 내장 샘플 분석 실행

```bash
curl -X POST http://localhost:8080/api/analysis/run \
  -H 'Content-Type: application/json' \
  -d '{"rootPath":"/home/u24/projects/trace_view_core/samples/spring-reference-app"}'
```

### 2. 최신 스냅샷 조회

분석 실행 전에는 `latest`와 `graph`가 비어 있을 수 있으며, 이 경우 `204 No Content`가 반환된다. 분석 결과를 보려면 먼저 1번을 실행한다.

```bash
curl http://localhost:8080/api/query/latest
```

### 3. 그래프 / 엔드포인트 조회

```bash
curl 'http://localhost:8080/api/query/graph'
curl 'http://localhost:8080/api/query/endpoints'
```

### 4. 노드 상세 조회

먼저 그래프나 엔드포인트 응답에서 `id`를 확인한 뒤:

```bash
curl 'http://localhost:8080/api/query/nodes/<node-id>'
curl 'http://localhost:8080/api/query/endpoints/<endpoint-id>/service-chain'
```

### 5. UI 빠른 확인

1. `http://localhost:8080/` 접속
2. Search에서 노드 검색 또는 추천 카드 이동
3. Node / Endpoint detail에서 Graph / Reviews 이동
4. Reviews에서 annotation 생성, approve, reject 확인

### 6. Runtime Trace 빠른 확인

분석 스냅샷이 있으면 runtime trace event를 정적 그래프 노드와 상관분석할 수 있다.

세션 생성:

```bash
curl -X POST http://localhost:8080/api/traces/sessions \
  -H 'Content-Type: application/json' \
  -d '{"traceId":"demo-trace-1","environment":"local"}'
```

event ingest:

```bash
curl -X POST http://localhost:8080/api/traces/sessions/<session-id>/events \
  -H 'Content-Type: application/json' \
  -d '{
    "events": [
      {
        "eventType": "API_CALL",
        "occurredAt": "2026-03-27T06:01:00Z",
        "httpMethod": "GET",
        "path": "/orders/{id}",
        "traceId": "demo-trace-1",
        "spanId": "span-1",
        "metadata": {}
      },
      {
        "eventType": "SERVICE_SPAN",
        "occurredAt": "2026-03-27T06:01:01Z",
        "sourceSymbol": "OrderService#findOrder",
        "traceId": "demo-trace-1",
        "spanId": "span-2",
        "metadata": {}
      }
    ]
  }'
```

correlation 조회:

```bash
curl 'http://localhost:8080/api/traces/sessions/<session-id>/correlation'
```

## 리뷰 API 테스트

`targetId`는 현재 스냅샷에 존재하는 노드 ID여야 한다.

Draft annotation 생성:

```bash
curl -X POST http://localhost:8080/api/reviews/annotations \
  -H 'Content-Type: application/json' \
  -d '{"targetId":"service_order","content":"주문 조회 핵심 로직","author":"tester"}'
```

annotation 조회:

```bash
curl 'http://localhost:8080/api/reviews/annotations?targetId=service_order'
curl 'http://localhost:8080/api/reviews/annotations'
```

approve / reject:

```bash
curl -X POST http://localhost:8080/api/reviews/annotations/<annotation-id>/approve \
  -H 'Content-Type: application/json' \
  -d '{"approver":"lead"}'

curl -X POST http://localhost:8080/api/reviews/annotations/<annotation-id>/reject \
  -H 'Content-Type: application/json' \
  -d '{"approver":"lead","reason":"근거 부족"}'
```

## 주요 문서

- [User Guide](./docs/user-guide.md)
- [Remaining Work Plan](./docs/remaining-work-plan.md)
- [Project Overview](./docs/product-overview.md)
- [PRD](./docs/PRD.md)
- [Architecture](./docs/architecture.md)
- [Data Model](./docs/data-model.md)
- [Analysis Pipeline](./docs/analysis-pipeline.md)
- [Operations](./docs/operations.md)
- [Implementation Plan](./docs/implementation-plan.md)
- [Task Board](./docs/task-board.md)
- [Status Report](./docs/status-report.md)
