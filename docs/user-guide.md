# Trace View Core User Guide

## 1. 문서 목적

이 문서는 Trace View Core를 처음 사용하는 사람이
`실행 -> 분석 -> 탐색 -> 리뷰 -> trace correlation` 흐름을 바로 따라할 수 있게 정리한 사용 가이드다.

대상 독자:

- 제품을 처음 켜보는 개발자
- 분석 결과를 탐색하려는 신규 인력
- annotation 승인/반려를 수행하는 리뷰 담당자

## 2. 제품이 하는 일

Trace View Core는 Spring 애플리케이션을 정적 분석해서 아래 흐름을 보여준다.

- ApiEndpoint
- BackendEntryPoint
- ServiceMethod
- RepositoryCall
- ExternalCall

사용자는 UI 또는 API로 다음 작업을 할 수 있다.

- 최신 분석 스냅샷 확인
- 노드 검색
- 노드 상세 / 엔드포인트 상세 확인
- 호출 그래프 탐색
- annotation 생성
- annotation approve / reject
- runtime trace session 생성
- runtime trace event 적재
- runtime trace correlation 확인

## 3. 시작 전 준비

필수 환경:

- Java 17
- Maven 3.9+

프로젝트 루트:

```bash
cd /home/u24/projects/trace_view_core
```

## 4. 실행 방법

애플리케이션 실행:

```bash
mvn -Dmaven.repo.local=.m2/repository spring-boot:run
```

접속 주소:

- UI: `http://localhost:8080/`
- API base: `http://localhost:8080/api`

정상 기동 시 로그 예시:

- `Tomcat started on port 8080`
- `Started TraceViewCoreApplication`

## 5. 처음 켰을 때 보이는 상태

앱을 처음 켜면 아직 분석 결과가 없을 수 있다.

이 경우 동작은 아래와 같다.

- `/api/query/latest` -> `204 No Content`
- `/api/query/graph` -> `204 No Content`
- UI 좌측 Snapshot 영역 -> `No active snapshot`
- Search / Graph 화면 -> 분석을 먼저 실행하라는 empty-state 표시

이 상태는 오류가 아니라 정상 초기 상태다.

## 6. 첫 분석 실행

가장 쉬운 시작 방법은 내장 샘플 프로젝트를 분석하는 것이다.

```bash
curl -X POST http://localhost:8080/api/analysis/run \
  -H 'Content-Type: application/json' \
  -d '{"rootPath":"/home/u24/projects/trace_view_core/samples/spring-reference-app"}'
```

성공 시 예시 응답:

- `snapshotId`
- `rootPath`
- `createdAt`
- `nodeCount`
- `relationCount`
- `evidenceCount`

분석이 끝나면 UI를 새로고침하거나 다시 열면 Snapshot 정보가 보인다.

## 7. 가장 쉬운 사용 순서

처음 사용하는 경우 아래 순서가 가장 이해하기 쉽다.

1. Search 화면에서 검색 또는 추천 카드 확인
2. 노드 상세 또는 엔드포인트 상세 열기
3. Graph 화면에서 관계 시각화 확인
4. Reviews 화면에서 annotation 생성 / 승인 / 반려

## 8. UI 사용 방법

### 8.1 Search

경로:

- `#/search`

할 수 있는 일:

- 서비스명, 엔드포인트명, 클래스명 검색
- 추천 노드 바로 열기
- 엔드포인트 상세 / 그래프로 이동

추천 사용 예:

- `OrderService`
- `GET /orders/{id}`
- `BillingClient`

검색 결과 카드에서 확인 가능한 정보:

- 노드 유형
- review 상태
- confidence
- source path / symbol

### 8.2 Node Detail

경로:

- `#/nodes/<node-id>`

보는 정보:

- 노드 메타데이터
- incoming / outgoing 관계
- evidence 목록
- annotation 요약

언제 쓰는가:

- 특정 서비스 메서드가 어디서 호출되는지 보고 싶을 때
- 특정 저장소 접근이나 외부 연동의 근거를 보고 싶을 때

### 8.3 Endpoint Detail

경로:

- `#/endpoints/<endpoint-id>`

추가로 보는 정보:

- service chain
- endpoint 기준 하위 흐름

언제 쓰는가:

- API 하나를 기준으로 controller -> service -> repository / external 흐름을 한 번에 보고 싶을 때

### 8.4 Graph

경로:

- `#/graph`

기능:

- 노드 전체 관계 시각화
- `nodeId` 기준 집중 보기
- `type` 기준 필터링
- 노드 / relation 선택 시 우측 상세 패널 확인

예시:

- `#/graph?nodeId=service_orderservice_findorder`
- `#/graph?type=SERVICE_METHOD`

빈 스냅샷 상태에서는 그래프 대신 empty-state가 보인다.

### 8.5 Reviews

경로:

- `#/reviews`
- `#/reviews?targetId=<node-id>`

기능:

- pending annotation 목록 보기
- 특정 target 기준 annotation 목록 보기
- 새 annotation 생성
- approve
- reject

추천 사용 방식:

1. 상세 화면에서 Review 이동
2. `targetId`가 자동으로 들어간 상태에서 annotation 생성
3. approve 또는 reject 수행

## 9. API 사용 방법

### 9.1 최신 스냅샷 확인

```bash
curl -i http://localhost:8080/api/query/latest
```

해석:

- `204`면 아직 분석 결과 없음
- `200`이면 최신 스냅샷 JSON 반환

### 9.2 그래프 조회

```bash
curl -i http://localhost:8080/api/query/graph
curl -i 'http://localhost:8080/api/query/graph?type=SERVICE_METHOD'
curl -i 'http://localhost:8080/api/query/graph?nodeId=service_orderservice_findorder'
```

### 9.3 엔드포인트 목록 조회

```bash
curl http://localhost:8080/api/query/endpoints
```

### 9.4 노드 상세 조회

```bash
curl 'http://localhost:8080/api/query/nodes/<node-id>'
```

### 9.5 엔드포인트 서비스 체인 조회

```bash
curl 'http://localhost:8080/api/query/endpoints/<endpoint-id>/service-chain'
```

### 9.6 annotation 생성

```bash
curl -X POST http://localhost:8080/api/reviews/annotations \
  -H 'Content-Type: application/json' \
  -d '{"targetId":"service_orderservice_findorder","content":"핵심 주문 조회 로직","author":"tester"}'
```

### 9.7 annotation approve

```bash
curl -X POST http://localhost:8080/api/reviews/annotations/<annotation-id>/approve \
  -H 'Content-Type: application/json' \
  -d '{"approver":"lead"}'
```

### 9.8 annotation reject

```bash
curl -X POST http://localhost:8080/api/reviews/annotations/<annotation-id>/reject \
  -H 'Content-Type: application/json' \
  -d '{"approver":"lead","reason":"근거 부족"}'
```

### 9.9 trace session 생성

```bash
curl -X POST http://localhost:8080/api/traces/sessions \
  -H 'Content-Type: application/json' \
  -d '{"traceId":"demo-trace-1","environment":"local"}'
```

응답에서 확인하는 핵심 필드:

- `id`
- `traceId`
- `environment`
- `eventCount`

### 9.10 trace event 적재

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

지원 eventType:

- `SCREEN_VIEW`
- `API_CALL`
- `BACKEND_ENTRY`
- `SERVICE_SPAN`

### 9.11 trace session 조회

```bash
curl http://localhost:8080/api/traces/sessions
curl http://localhost:8080/api/traces/sessions/<session-id>
```

### 9.12 trace correlation 조회

```bash
curl http://localhost:8080/api/traces/sessions/<session-id>/correlation
```

해석:

- `matchedNodes`: runtime event가 정적 graph의 어떤 node와 연결됐는지
- `matchedLinks`: session -> node, node -> node 흐름으로 복원된 trace link
- `unmatchedEvents`: 정적 graph와 아직 연결되지 않은 event

### 9.8 annotation reject

```bash
curl -X POST http://localhost:8080/api/reviews/annotations/<annotation-id>/reject \
  -H 'Content-Type: application/json' \
  -d '{"approver":"lead","reason":"근거 부족"}'
```

## 10. 자주 겪는 상황

### 10.1 UI는 열리는데 데이터가 없다

원인:

- 아직 분석을 실행하지 않음

확인:

```bash
curl -i http://localhost:8080/api/query/latest
```

조치:

- `204`면 `POST /api/analysis/run` 실행

### 10.2 예전 오류 로그가 계속 보인다

원인:

- 수정 전 프로세스가 계속 떠 있음

조치:

```bash
pkill -f 'trace-view-core|spring-boot:run' || true
mvn -Dmaven.repo.local=.m2/repository spring-boot:run
```

### 10.3 잘못된 targetId 또는 annotation id를 보냈다

현재 동작:

- 없는 target / annotation은 `404`
- 스냅샷이 필요한 API를 분석 전에 호출하면 일부는 `409` 또는 `204`

### 10.4 분석 결과가 사라졌다

현재 구현 특성:

- 저장소는 인메모리 기반
- 프로세스를 재시작하면 스냅샷과 annotation 상태가 초기화될 수 있다

## 11. 권장 데모 시나리오

### 시나리오 A. 제품 처음 보기

1. 앱 실행
2. 샘플 분석 실행
3. Search에서 `OrderService` 검색
4. 상세 화면 이동
5. Graph 이동
6. Reviews 이동 후 annotation 생성

### 시나리오 B. API 하나 추적하기

1. Search에서 `GET /orders/{id}` 검색
2. Endpoint Detail 이동
3. Service Chain 확인
4. Graph에서 주변 관계 확인

### 시나리오 C. 리뷰 작업하기

1. 특정 노드 상세 이동
2. Review 이동
3. annotation 생성
4. approve 또는 reject

## 12. 현재 한계

- 실제 외부 대상 코드베이스 연결은 아직 미구현
- adapter 범위는 아직 미구현
- runtime trace는 아직 미구현
- 클라이언트 브라우저 상호작용 테스트는 아직 얕다
- 저장소는 인메모리 기반이라 영속 저장이 없다

## 13. 관련 문서

- `README.md`
- `docs/product-overview.md`
- `docs/architecture.md`
- `docs/analysis-pipeline.md`
- `docs/operations.md`
- `docs/status-report.md`
