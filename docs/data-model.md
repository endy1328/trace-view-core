# Trace View Core Data Model

## 1. 문서 목적

이 문서는 Trace View Core의 공통 데이터 모델을 정의한다.
목표는 프론트와 백엔드 분석 결과를 공통 노드 및 관계 모델로 통합하는 것이다.

## 2. 모델 설계 원칙

- 특정 언어와 프레임워크에 종속되지 않아야 한다.
- 자동 추론 관계와 확정 관계를 구분해야 한다.
- 모든 중요한 관계는 근거 정보를 가져야 한다.
- 자동 생성 설명은 승인 상태를 가져야 한다.

## 3. 핵심 노드

### 3.1 Screen

- 의미: 사용자가 직접 진입 가능한 라우트 기반 페이지
- 주요 속성: id, name, route_path, source_path, owner_team, tags

### 3.2 FrontendRoute

- 의미: 프론트 라우팅 정의 단위
- 주요 속성: id, path, route_name, source_path, source_symbol

### 3.3 ApiEndpoint

- 의미: HTTP method와 path 기준의 엔드포인트
- 주요 속성: id, method, path, domain, openapi_operation_id, tags

### 3.4 BackendEntryPoint

- 의미: 컨트롤러, 핸들러, 라우터 메서드 등 서버 진입점
- 주요 속성: id, class_name, method_name, source_path, source_line

### 3.5 ServiceMethod

- 의미: 핵심 비즈니스 로직을 담당하는 서비스 또는 유스케이스 메서드
- 주요 속성: id, class_name, method_name, module_name, source_path, source_line

### 3.6 RepositoryCall

- 의미: DB 조회 또는 저장 처리
- 주요 속성: id, repository_name, method_name, source_path, source_line

### 3.7 ExternalCall

- 의미: 외부 API, 외부 서비스, 타 시스템 연동 호출
- 주요 속성: id, target_name, protocol, source_path, source_line

### 3.8 BusinessModule

- 의미: 주문, 결제, 회원 등 도메인 또는 모듈 구분 단위
- 주요 속성: id, name, description, owner_team

### 3.9 TraceSession

- 의미: 런타임 수집 기준이 되는 세션 또는 요청 흐름 묶음
- 주요 속성: id, trace_id, environment, collected_at

### 3.10 Annotation

- 의미: 수동 주석, 운영 메모, 승인된 설명
- 주요 속성: id, target_id, content, author, approver, status, created_at

## 4. 핵심 관계

- Screen contains FrontendRoute
- Screen calls ApiEndpoint
- ApiEndpoint handled_by BackendEntryPoint
- BackendEntryPoint invokes ServiceMethod
- ServiceMethod accesses RepositoryCall
- ServiceMethod calls ExternalCall
- ServiceMethod belongs_to BusinessModule
- AnyNode annotated_by Annotation
- TraceSession traced_to AnyNode

## 5. 관계 분류

### 5.1 확정 관계

- 프레임워크 메타데이터, 명시적 선언, 정적 분석으로 직접 확인 가능

예시:

- 어노테이션 기반 컨트롤러 매핑
- 명시적 라우트 선언

### 5.2 추론 관계

- 래퍼, 동적 문자열, 런타임 패턴, 규칙 기반으로 추론

예시:

- 공통 API wrapper를 경유한 호출
- feature flag에 따라 갈리는 분기 호출

## 6. 공통 속성

### 6.1 노드 공통 속성

- id
- type
- name
- source_path
- source_symbol
- tags
- confidence
- approval_status
- updated_at

### 6.2 관계 공통 속성

- from_id
- to_id
- relation_type
- certainty_type
- confidence
- evidence_id
- updated_at

## 7. 근거 모델

모든 자동 분석 관계는 최소 하나의 근거를 가져야 한다.

### 7.1 Evidence

- id
- evidence_type
- source_file
- source_line
- source_symbol
- trace_id
- rule_id
- analyzer_name
- analyzer_version
- captured_at

### 7.2 evidence_type 예시

- static_route_parse
- http_client_parse
- controller_mapping
- service_call_chain
- runtime_trace
- manual_link

## 8. 승인 상태 모델

### 8.1 설명 및 메타데이터 상태

- Draft
- Review Pending
- Approved
- Rejected
- Archived

### 8.2 관계 상태

- Confirmed
- Inferred
- Deprecated

## 9. 스냅샷 모델

### 9.1 Snapshot

- id
- snapshot_name
- code_version
- environment
- created_at
- analyzer_bundle_version

역할:

- 특정 시점의 분석 결과를 버전으로 보관
- 이전 분석과 비교 가능하게 함

## 10. 조회 패턴

- 화면 기준 API 목록 조회
- API 기준 서버 진입점 조회
- 서비스 메서드 기준 하위 저장소 및 외부 호출 조회
- 스냅샷 간 변경 관계 비교
- 승인 상태별 설명 필터링

## 11. 모델 제약

- 하나의 ApiEndpoint는 여러 Screen에 연결될 수 있다.
- 하나의 Screen은 여러 ApiEndpoint를 호출할 수 있다.
- 하나의 ServiceMethod는 여러 ApiEndpoint에서 재사용될 수 있다.
- 근거가 없는 자동 관계는 저장하지 않는다.
- Draft 설명은 기본 설명으로 노출하지 않는다.

## 12. Open Questions

- Annotation을 문서형 노드로 확장할지 별도 메타데이터로 유지할지
- TraceSession을 요청 단위로 볼지 사용자 세션 단위로 볼지
- BusinessModule 분류 체계를 도메인 기준으로 강제할지 자유 태깅으로 둘지
