# Trace View Core

Trace View Core는 Spring 애플리케이션의 `endpoint -> handler -> service -> repository / external call` 흐름을 정적 분석하고, 이를 검색/그래프/리뷰 API로 제공하는 백엔드 코어 프로젝트다.

기획 개요 문서는 [docs/product-overview.md](./docs/product-overview.md)로 분리했다.

## 현재 상태

- Spring 범용 코어 분석기 구현 완료
- graph / node detail / service chain 조회 API 구현 완료
- annotation 생성, approve, reject용 review API 구현 완료
- runtime trace session / event ingest / correlation API 구현 완료
- adapter framework, `astore-legacy` canonicalization, 세부 adapter 분류 구현 완료
- 모듈 자동 분류 API와 세부 adapter 추천 기능 구현 완료
- 정적 제품 레이어 UI 구현 완료
- 검색, 노드/엔드포인트 상세, 그래프, 리뷰 화면 제공
- 내장 샘플 프로젝트 기준 통합 테스트 완료
- 실제 외부 대상 코드베이스 AStore 구조 분석 완료, 모듈별 규칙 심화는 진행 중

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

- `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`

## 빠른 확인 순서

### 1. 내장 샘플 분석 실행

```bash
curl -X POST http://localhost:8080/api/analysis/run \
  -H 'Content-Type: application/json' \
  -d '{"rootPath":"/home/u24/projects/trace_view_core/samples/spring-reference-app"}'
```

adapter를 지정하려면 `adapterId`를 함께 보낸다.

```bash
curl -X POST http://localhost:8080/api/analysis/run \
  -H 'Content-Type: application/json' \
  -d '{"rootPath":"/home/u24/projects/AStore","adapterId":"astore-legacy"}'
```

Windows에서 호출할 때는 셸에 따라 quoting 규칙이 다르다.

`cmd.exe` 예시:

```bat
curl -X POST http://localhost:8080/api/analysis/run ^
  -H "Content-Type: application/json" ^
  -d "{\"rootPath\":\"/home/u24/projects/trace_view_core/samples/spring-reference-app\"}"
```

PowerShell 예시:

```powershell
curl.exe -X POST http://localhost:8080/api/analysis/run `
  -H "Content-Type: application/json" `
  -d '{"rootPath":"/home/u24/projects/trace_view_core/samples/spring-reference-app"}'
```

서버가 WSL 또는 Linux에서 실행 중이면 Windows 경로 대신 Linux 경로를 보내야 한다.

- 가능: `/home/u24/projects/...`
- 가능: `/mnt/c/Users/<user>/IdeaProjects/...`
- 불가: `C:\Users\...`
- 불가: `\\wsl.localhost\Ubuntu-24.04\...`

`cmd.exe`에서 작은따옴표는 quoting에 사용되지 않으므로 `-H 'Content-Type: application/json'` 형태는 `415 Unsupported Media Type`를 만들 수 있다.

현재 지원 adapter:

- `spring-standard`: 기본 Spring 표준 규칙
- `astore-legacy`: AStore 레거시 Spring MVC / ServiceImpl / Biz / DAO 규칙 보강 및 interface/impl canonicalization
- `astore-web-mvc`: AStore 웹 MVC 모듈용 adapter
- `astore-batch-legacy`: AStore batch/iBatis 모듈용 adapter
- `astore-lib-shared`: AStore 공통 라이브러리 모듈용 adapter

모듈 자동 분류:

```bash
curl -X POST http://localhost:8080/api/analysis/classify-modules \
  -H 'Content-Type: application/json' \
  -d '{"rootPath":"/home/u24/projects/AStore"}'
```

현재 AStore 기준 분류 결과:

- `AStore-batch-backend` -> `astore-batch-legacy`
- `AStore-Admin` -> `astore-web-mvc`
- `AStore-Carrier` -> `astore-web-mvc`
- `AStore-Seller` -> `astore-web-mvc`
- `AStore-lib` -> `astore-lib-shared`

현재 구조 분석 결론:

- `AStore-ear-backend` 아래 `Admin`, `Carrier`, `Seller`, `lib`가 웹/공용 라이브러리 축
- `AStore-batch-backend`는 XML 중심 레거시 배치 축
- adapter 세분화 단위는 `웹 3개 + 배치 1개 + 공유 라이브러리 1개`가 적절

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
