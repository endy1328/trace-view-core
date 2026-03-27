# Trace View Core Architecture

## 1. 문서 목적

이 문서는 Trace View Core의 초기 아키텍처 방향을 정리한다. 목표는 PoC와 MVP 구현 전에 시스템 경계, 데이터 흐름, 핵심 컴포넌트 책임을 명확히 하는 것이다.

## 2. 설계 원칙

- 정적 분석과 런타임 추적을 분리한다.
- 자동 추론 결과와 확정 메타데이터를 구분한다.
- 특정 기술 스택에 종속되지 않는 공통 모델을 먼저 정의한다.
- MVP에서는 완전성보다 탐색 경험을 우선한다.

## 3. 시스템 개요

Trace View Core는 크게 4개 계층으로 본다.

- 수집 계층
- 해석 계층
- 저장 계층
- 제공 계층

## 4. 상위 구조

### 4.1 수집 계층

역할:

- 소스 코드와 실행 데이터를 모은다.
- 화면, 라우트, API, 백엔드 진입점, 호출 관계 후보를 추출한다.

구성:

- Frontend Analyzer
- Backend Analyzer
- Runtime Trace Collector
- Annotation Ingestor

### 4.2 해석 계층

역할:

- 수집된 원천 데이터를 공통 그래프 모델로 변환한다.
- 후보 관계를 확정 관계 또는 추론 관계로 분류한다.
- 검색과 시각화에 맞는 형태로 가공한다.

구성:

- Mapping Engine
- Call Chain Resolver
- Summary Generator
- Impact Analyzer
- Metadata Merger

### 4.3 저장 계층

역할:

- 노드와 관계를 저장한다.
- 빠른 검색과 상세 조회를 지원한다.

구성:

- Graph Store
- Search Index
- Metadata Store
- Analysis Snapshot Store

### 4.4 제공 계층

역할:

- 검색, 상세 보기, 그래프 탐색, 주석 관리를 제공한다.

구성:

- Search UI
- Detail UI
- Graph UI
- Admin UI

## 5. 핵심 데이터 흐름

### 5.1 정적 분석 흐름

1. 프론트 분석기가 라우트, 화면 컴포넌트, API 호출 코드를 스캔한다.
2. 백엔드 분석기가 라우터, 컨트롤러, 핸들러, 서비스 호출 체인을 스캔한다.
3. Mapping Engine이 화면-API, API-진입점, 진입점-서비스 관계 후보를 생성한다.
4. 결과를 공통 그래프 모델로 변환해 저장한다.

### 5.2 런타임 추적 흐름

1. 브라우저에서 화면 진입과 API 호출 이벤트를 수집한다.
2. 백엔드에서 요청 trace와 서비스 span을 수집한다.
3. Runtime Trace Collector가 프론트와 백엔드 흐름을 연결한다.
4. 정적 분석 결과와 런타임 결과를 비교해 신뢰도를 보강한다.

### 5.3 수동 보강 흐름

1. 사용자가 화면, API, 서비스 노드에 설명을 입력한다.
2. Metadata Merger가 자동 결과와 수동 메타데이터를 결합한다.
3. 상세 보기에서 자동 추론과 수동 설명을 함께 노출한다.

## 6. 공통 모델

Trace View Core는 특정 언어 구조를 그대로 저장하지 않고 공통 모델로 변환한다.

### 6.1 노드 유형

- Screen
- FrontendRoute
- ApiEndpoint
- BackendEntryPoint
- ServiceMethod
- RepositoryCall
- ExternalCall
- BusinessModule
- TraceSession
- Annotation

### 6.2 관계 유형

- contains
- calls
- handled_by
- invokes
- accesses
- belongs_to
- traced_to
- annotated_by

### 6.3 노드 공통 속성

- id
- type
- name
- source_path
- source_symbol
- confidence
- approval_status
- tags
- updated_at

### 6.4 관계 공통 속성

- from_id
- to_id
- relation_type
- certainty_type
- confidence
- evidence_id
- updated_at

### 6.5 근거 저장 모델

자동 분석 관계는 최소 아래 근거 중 하나를 남겨야 한다.

- evidence_id
- source_file
- source_line
- source_symbol
- trace_id
- rule_id
- analyzer_version

사용자는 관계가 왜 생성되었는지 역추적할 수 있어야 한다.

## 7. 컴포넌트 상세

### 7.1 Frontend Analyzer

책임:

- 라우트 정의 파악
- 화면 컴포넌트 추출
- API client wrapper 사용 추적
- fetch, axios, SDK 기반 호출 추출

출력 예시:

- Screen
- FrontendRoute
- Screen calls ApiEndpoint

### 7.2 Backend Analyzer

책임:

- 엔드포인트 선언 파악
- 컨트롤러 또는 핸들러 파악
- 서비스 호출 체인 파악
- 저장소 및 외부 호출 후보 추출

출력 예시:

- ApiEndpoint handled_by BackendEntryPoint
- BackendEntryPoint invokes ServiceMethod
- ServiceMethod accesses RepositoryCall
- ServiceMethod calls ExternalCall

### 7.3 Runtime Trace Collector

책임:

- 실제 브라우저 화면 기준 호출 흐름 수집
- 요청 trace id와 백엔드 span 연결
- 정적 분석으로 못 잡은 동적 호출 보완

출력 예시:

- Screen traced_to ApiEndpoint
- ApiEndpoint traced_to ServiceMethod

### 7.4 Mapping Engine

책임:

- 정적 분석 결과를 공통 그래프 모델로 병합
- 중복 노드 식별
- 후보 관계의 신뢰도 계산
- 근거 데이터 연결

주의점:

- 자동 추론 관계는 확정 관계와 시각적으로 구분되어야 한다.

### 7.5 Summary Generator

책임:

- 서비스와 핸들러 단위 설명 생성
- 핵심 검증, 저장, 외부 호출, 예외 흐름 요약

권장 방식:

- 초기에는 룰 기반 요약
- 이후 LLM 보조 요약 검토

요약 상태:

- Draft
- Approved
- Rejected

자동 생성 요약은 기본적으로 Draft로 저장하고 사람 승인 전까지 확정 설명으로 취급하지 않는다.

## 8. 저장소 선택 방향

구체 DB는 나중에 결정해도 되지만 역할은 분리해야 한다.

### 8.1 Graph Store

용도:

- 연결 관계 중심 조회
- 그래프 탐색
- 영향도 계산

### 8.2 Search Index

용도:

- 화면명, URL, 엔드포인트, 클래스명, 메서드명 검색
- 빠른 자동완성

### 8.3 Metadata Store

용도:

- 수동 설명
- 운영 메모
- 태그
- 소유 팀 정보

### 8.4 Snapshot Store

용도:

- 분석 실행 이력
- 버전별 결과 비교
- 회귀 검증

## 9. 분석 단위

MVP의 기본 분석 단위는 아래와 같다.

- 프론트: 단일 애플리케이션 리포지토리
- 백엔드: 단일 서비스 또는 BFF
- 스냅샷 기준: 특정 시점의 코드 버전과 분석 실행 결과 묶음

후속 단계에서 `repo`, `module`, `service`, `environment` 축을 확장할 수 있다.

## 10. 신뢰도 모델

Trace View Core는 결과를 아래 두 가지로 구분해 보여줘야 한다.

### 10.1 확정 관계

- 프레임워크 메타데이터나 명시적 코드 구조로 직접 확인된 관계
- 예: 컨트롤러 어노테이션으로 확인된 엔드포인트

### 10.2 추론 관계

- 네이밍 규칙, 호출 패턴, 공통 래퍼, 런타임 관찰에 기반한 관계
- 예: 특정 화면에서 간접 호출되는 API

사용자에게는 근거와 신뢰도를 함께 보여줘야 한다.

분류 예시:

- 프레임워크 메타데이터로 직접 확인된 라우트 연결은 확정 관계
- 공통 API wrapper 내부 문자열 조합으로 추적된 호출은 추론 관계
- feature flag 분기에 따라 달라지는 호출은 런타임 근거가 없으면 추론 관계

## 11. UI 구조 제안

### 11.1 Search View

입력:

- 화면명
- 라우트
- 엔드포인트
- 클래스명
- 메서드명

출력:

- 관련 노드 목록
- 노드 유형
- 신뢰도

### 11.2 Detail View

대상:

- Screen
- ApiEndpoint
- ServiceMethod

출력:

- 기본 메타데이터
- 연결된 상하위 노드
- 설명과 운영 메모
- 소스 위치
- 근거 정보
- 승인 상태

### 11.3 Graph View

목적:

- 호출 흐름을 시각적으로 탐색
- 영향 범위를 빠르게 파악

표현 기준:

- 확정 관계와 추론 관계를 다르게 표시
- 핵심 경로 우선 노출

## 12. 운영 시나리오

### 12.1 분석 갱신

- 코드 변경 후 수동 또는 배치 방식으로 정적 분석 재실행
- 결과 스냅샷 저장
- 기존 결과와 비교

### 12.2 온보딩 활용

- 신규 인력이 화면이나 API를 기준으로 검색
- 설명과 그래프를 통해 맥락 파악
- 필요한 경우 메모 추가

### 12.3 영향도 검토

- 변경 대상 서비스 또는 API를 기준으로 연결된 상하위 노드 확인
- 관련 화면과 외부 연동 확인

### 12.4 문서 및 메타데이터 검증

- 문서 생성 후 별도 검증 에이전트가 리뷰
- 자동 요약은 승인자 확인 전까지 Draft 유지
- 검증 결과는 리뷰 문서로 저장

## 13. MVP 기술 전략

초기에는 아래 전략이 적절하다.

- 대표 화면군만 대상으로 분석
- 단일 프론트 앱과 단일 백엔드 서비스부터 시작
- 정적 분석을 우선 구현
- 런타임 추적은 누락 보완용으로 후속 도입
- 요약 품질보다 탐색 정확도를 먼저 확보

## 14. 확장 포인트

MVP 이후 확장 후보는 아래와 같다.

- 멀티 리포지토리 통합
- 배치와 이벤트 흐름 연결
- 권한 정책 흐름 표시
- 변경 diff 기반 영향도 추천
- 릴리즈 노트와 연결

## 15. 오픈 이슈

현재 문서 기준 Open Questions는 아래로 정리한다.

- 프론트 분석 범위를 MVP에서 제외한 상태로 유지할지, 후속 단계에서 어떤 기준으로 재도입할지
- API 호출 래퍼 추적 깊이를 1단계로 제한할지, 공통 wrapper 내부까지 추적할지
- 백엔드 내부 호출은 엔드포인트 기준 몇 단계까지 기본 노출할지
- BusinessModule 분류를 규칙 기반으로 둘지 수동 보강 중심으로 둘지

## 16. 문서 검증 에이전트 설계

Trace View Core의 문서 생성 프로세스에는 별도의 검증 에이전트를 둔다.

### 16.1 역할

- 생성된 PRD, 설계서, 화면 문서의 품질 검증
- 문서 간 충돌 여부 확인
- 누락된 설계 조건과 운영 조건 확인
- 과도한 복잡도나 비현실적 가정 지적
- 대안 제시

### 15.2 입력

- 초안 문서
- 관련 상위 문서와 링크
- 이전 검증 결과

### 15.3 출력

- Must Fix
- Recommended Improvements
- Open Questions
- 최종 판정

### 15.4 판정 기준

- 문서가 상위 목표와 정합적인가
- 요구사항이 구현 가능한 수준으로 구체적인가
- 아키텍처가 MVP 범위에 비해 과도하지 않은가
- 운영 방식과 책임 주체가 빠져 있지 않은가
- 자동화 가정이 과장되어 있지 않은가

### 15.5 운영 원칙

- 생성 에이전트와 검증 에이전트는 분리한다.
- 검증 결과는 원문서와 별도로 추적 가능해야 한다.
- 문서 확정 전에는 반드시 최소 1회 검증을 거친다.

## 17. Decision Log

- 2026-03-26: 공통 모델에 근거 저장 필드를 추가
- 2026-03-26: 자동 생성 비즈니스 요약은 Draft 승인 모델을 따르도록 결정
- 2026-03-26: MVP의 기본 분석 단위를 단일 프론트 앱과 단일 백엔드 서비스로 설정
- 2026-03-26: MVP의 런타임 추적은 개발 및 스테이징 환경에서만 허용
- 2026-03-26: 메타데이터 승인자는 도메인별 지정 리드로 정의
