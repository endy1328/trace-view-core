# Trace View Core Development Backlog

## 1. 문서 목적

이 문서는 `Agent.md`를 기준으로 Trace View Core 개발을 실제 착수 가능한 작업 단위로 분해한 백로그다.
목표는 멀티에이전트가 병렬로 작업하더라도 충돌 없이 빠르게 MVP를 구현할 수 있게 하는 것이다.

## 2. 운영 원칙

- 작업은 `Spring 범용 코어 -> 제품 레이어 -> Adapter` 순으로 자른다.
- 하나의 작업은 하나의 명확한 산출물만 가진다.
- 같은 파일 집합을 여러 에이전트가 동시에 수정하지 않는다.
- 모든 작업은 입력 문서와 완료 조건을 가진다.
- 검증 가능한 작업부터 먼저 구현한다.

## 3. 개발 단계

1. Spring 분석 범위 확정
2. 기술 스택 확정
3. 공통 모델 및 저장 구조 구현
4. Spring Core Analyzer 구현
5. 조회 API 구현
6. 최소 UI 구현
7. 검증 및 승인 흐름 구현
8. Adapter Framework 구현
9. 런타임 추적 보강

## 4. Epic 목록

### Epic 1. 프로젝트 부트스트랩

목적:

- 실제 구현에 필요한 리포지토리 구조와 기술 선택을 확정한다.

### Epic 2. 공통 모델 및 저장 구조

목적:

- 분석 결과를 저장할 공통 모델, Evidence, Snapshot 구조를 구현한다.

### Epic 3. 정적 분석 파이프라인

목적:

- 화면, 엔드포인트, 서비스 체인을 추출하는 MVP 분석기를 구현한다.

### Epic 4. 조회 API

목적:

- 검색, 상세, 그래프에 필요한 조회 API를 구현한다.

### Epic 5. MVP UI

목적:

- 검색, 화면 상세, 엔드포인트 상세, 기본 그래프 화면을 구현한다.

### Epic 6. 검증 및 운영 흐름

목적:

- Draft/Approved/Rejected 상태와 리뷰 흐름을 구현한다.

### Epic 7. 런타임 추적 보강

목적:

- 개발/스테이징 환경에서 정적 분석을 보강하는 추적 구조를 추가한다.

## 5. 상세 백로그

| ID | Epic | Task | Priority | 선행조건 | 산출물 | 완료 조건 |
| --- | --- | --- | --- | --- | --- | --- |
| DEV-001 | 프로젝트 부트스트랩 | 대상 프론트/백엔드 리포지토리 확정 | P0 | 기획 문서 완료 | 대상 시스템 목록 | 분석 대상이 문서로 고정됨 |
| DEV-002 | 프로젝트 부트스트랩 | 대표 화면 20~30개 선정 | P0 | DEV-001 | 화면 목록 | MVP 대상 화면이 확정됨 |
| DEV-003 | 프로젝트 부트스트랩 | 기술 스택 비교 및 선택 | P0 | DEV-001 | 기술 선택 결과 | 프론트, 백엔드, 저장소, 검색 방식 결정 |
| DEV-004 | 프로젝트 부트스트랩 | 개발 작업공간 구조 생성 | P0 | DEV-003 | 초기 프로젝트 구조 | 실행 가능한 기본 프로젝트가 생성됨 |
| DEV-005 | 공통 모델 및 저장 구조 | 공통 도메인 모델 정의 | P0 | DEV-003 | 모델 코드 | Screen, ApiEndpoint, ServiceMethod, Evidence, Snapshot 구조가 코드화됨 |
| DEV-006 | 공통 모델 및 저장 구조 | 저장소 스키마/저장 구조 구현 | P0 | DEV-005 | 저장 계층 | 노드, 관계, Evidence 저장 가능 |
| DEV-007 | 공통 모델 및 저장 구조 | 스냅샷 저장 구조 구현 | P1 | DEV-006 | 스냅샷 모듈 | 분석 결과 버전 저장 가능 |
| DEV-008 | 정적 분석 파이프라인 | 프론트 라우트 추출기 구현 | P0 | DEV-005 | Frontend Analyzer 일부 | Screen, FrontendRoute 추출 가능 |
| DEV-009 | 정적 분석 파이프라인 | 프론트 API 호출 추출기 구현 | P0 | DEV-008 | Frontend Analyzer 일부 | Screen -> ApiEndpoint 후보 생성 가능 |
| DEV-010 | 정적 분석 파이프라인 | 백엔드 엔드포인트 추출기 구현 | P0 | DEV-005 | Backend Analyzer 일부 | ApiEndpoint, BackendEntryPoint 추출 가능 |
| DEV-011 | 정적 분석 파이프라인 | 서비스 체인 추출기 구현 | P0 | DEV-010 | Backend Analyzer 일부 | BackendEntryPoint -> ServiceMethod 연결 가능 |
| DEV-012 | 정적 분석 파이프라인 | Mapping Engine MVP 구현 | P0 | DEV-009, DEV-011 | Mapping Engine | 화면-API-서비스 체인 통합 가능 |
| DEV-013 | 정적 분석 파이프라인 | Evidence 기록 구현 | P0 | DEV-012 | Evidence 저장 | 관계별 근거가 저장됨 |
| DEV-014 | 조회 API | 통합 검색 API 구현 | P0 | DEV-012 | Search API | 화면, API, 메서드 검색 가능 |
| DEV-015 | 조회 API | 화면 상세 API 구현 | P0 | DEV-012 | Screen Detail API | 화면 기준 호출 API 조회 가능 |
| DEV-016 | 조회 API | 엔드포인트 상세 API 구현 | P0 | DEV-012 | Endpoint Detail API | 엔드포인트 기준 서비스 체인 조회 가능 |
| DEV-017 | 조회 API | 기본 그래프 조회 API 구현 | P1 | DEV-012 | Graph API | 연결 그래프 데이터 조회 가능 |
| DEV-018 | MVP UI | 통합 검색 화면 구현 | P0 | DEV-014 | Search UI | 검색 결과에서 상세 이동 가능 |
| DEV-019 | MVP UI | 화면 상세 화면 구현 | P0 | DEV-015 | Screen Detail UI | 호출 API, 순서, 설명 확인 가능 |
| DEV-020 | MVP UI | 엔드포인트 상세 화면 구현 | P0 | DEV-016 | Endpoint Detail UI | 진입점과 서비스 체인 확인 가능 |
| DEV-021 | MVP UI | 기본 그래프 화면 구현 | P1 | DEV-017 | Graph UI | 핵심 연결 관계 시각화 가능 |
| DEV-022 | 검증 및 운영 흐름 | 설명 상태 모델 구현 | P0 | DEV-005 | 상태 모델 코드 | Draft, Approved, Rejected 저장 가능 |
| DEV-023 | 검증 및 운영 흐름 | 리뷰 결과 저장 구조 구현 | P0 | DEV-006 | Review 모델 | Must Fix, Recommended 저장 가능 |
| DEV-024 | 검증 및 운영 흐름 | 주석/승인 API 구현 | P1 | DEV-022, DEV-023 | Annotation API | 주석 생성, 승인, 반려 가능 |
| DEV-025 | 검증 및 운영 흐름 | 검증/승인 UI 구현 | P1 | DEV-024 | Review UI | 상태 변경과 리뷰 결과 조회 가능 |
| DEV-026 | 런타임 추적 보강 | 브라우저 호출 수집 구조 구현 | P2 | DEV-018 | Trace Collector 일부 | 개발/스테이징 환경에서 호출 수집 가능 |
| DEV-027 | 런타임 추적 보강 | 백엔드 trace 연결 구조 구현 | P2 | DEV-026 | Trace Correlation | 요청-서비스 흐름 연결 가능 |
| DEV-028 | 런타임 추적 보강 | 정적 분석 보강 규칙 구현 | P2 | DEV-027 | 보강 로직 | 추론 관계 신뢰도 보강 가능 |

## 6. 권장 스프린트 구성

### Sprint 1

- DEV-001
- DEV-002
- DEV-003
- DEV-004
- DEV-005
- DEV-006

목표:

- 구현 대상과 기반 모델을 고정한다.

### Sprint 2

- DEV-008
- DEV-009
- DEV-010
- DEV-011
- DEV-012
- DEV-013

목표:

- 정적 분석 MVP를 끝낸다.

### Sprint 3

- DEV-014
- DEV-015
- DEV-016
- DEV-017
- DEV-018
- DEV-019
- DEV-020

목표:

- 검색과 상세 흐름이 실제로 동작하게 만든다.

### Sprint 4

- DEV-021
- DEV-022
- DEV-023
- DEV-024
- DEV-025

목표:

- 그래프와 검증/운영 흐름을 붙인다.

### Sprint 5

- DEV-026
- DEV-027
- DEV-028

목표:

- 런타임 추적 보강을 붙인다.

## 7. 멀티에이전트 배치 권장안

### 트랙 A. 구조 분석

- Agent A1: 프론트 구조 분석
- Agent A2: API wrapper 분석
- Agent A3: 백엔드 구조 분석

대상 작업:

- DEV-001
- DEV-002
- DEV-003

### 트랙 B. 기반 구현

- Agent B1: 공통 모델 / 저장 구조
- Agent B2: 프론트 분석기
- Agent B3: 백엔드 분석기

대상 작업:

- DEV-005
- DEV-006
- DEV-008
- DEV-009
- DEV-010
- DEV-011

### 트랙 C. 통합 및 API

- Agent C1: Mapping Engine
- Agent C2: Search / Detail API
- Agent C3: Graph API

대상 작업:

- DEV-012
- DEV-013
- DEV-014
- DEV-015
- DEV-016
- DEV-017

### 트랙 D. UI

- Agent D1: Search / Screen Detail UI
- Agent D2: Endpoint Detail / Graph UI
- Agent D3: Review / Annotation UI

대상 작업:

- DEV-018
- DEV-019
- DEV-020
- DEV-021
- DEV-025

### 트랙 E. 검증

- Agent E1: 코드 리뷰 및 테스트 검증
- Agent E2: 문서-구현 정합성 검증

대상 작업:

- DEV-022
- DEV-023
- DEV-024
- 전 작업 리뷰

## 8. 병렬 수행 규칙

- 같은 디렉터리와 같은 핵심 파일을 동시에 수정하지 않는다.
- 모델 변경은 API/UI 작업보다 먼저 고정한다.
- 분석기와 UI는 병렬 가능하지만 Mapping Engine 변경 중에는 API 계약을 잠근다.
- 검증 에이전트는 구현 에이전트와 분리한다.

## 9. 병목 대응 규칙

- 30분 이상 설계 결정을 기다리면 메인 에이전트가 직접 결정하거나 추가 분석 에이전트를 투입한다.
- 구현 에이전트가 같은 파일에서 충돌할 가능성이 보이면 작업을 즉시 재분해한다.
- 테스트 또는 검증이 지연되면 별도 검증 에이전트를 추가 생성한다.
- 장시간 읽기/분석 작업은 탐색 전용 에이전트로 분리한다.

## 10. 상태 관리 컬럼

| ID | Epic | Owner Agent | Status | Validation Status | Priority | Progress | Input | Output | Next Action | Risk / Blocker |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

## 11. 완료 판단 기준

- 대표 화면군에서 화면 -> API -> 서비스 체인이 조회 가능
- 검색과 상세 화면이 실제 사용 가능
- Evidence가 저장되고 노출 가능
- Draft / Approved / Rejected 상태 흐름이 동작
- 문서와 구현 간 치명적 불일치가 없음

## 12. 다음 문서

이 백로그 다음 단계로 바로 이어서 만들 문서는 아래다.

- `docs/implementation-plan.md`
- `docs/task-board.md`
- `docs/reviews/implementation-review-*.md`
