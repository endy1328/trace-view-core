# Trace View Core Task Board

## 1. 문서 목적

이 문서는 Trace View Core의 구현 직전 작업 보드다.
실제 개발이 시작되면 이 문서를 기준으로 에이전트별 작업 배치와 상태 갱신을 관리한다.

## 2. 상태 규칙

- Pending
- In Progress
- Review Pending
- Needs Revision
- Validated
- Done
- On Hold
- Blocked

## 3. 작업 보드

| ID | Phase | Workstream | Owner Agent | Status | Validation Status | Priority | Progress | Input | Output | Next Action | Risk / Blocker |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DEV-001 | Phase 1 | Target Repo Selection | Main | Done | Validated | P0 | 100 | docs/development-backlog.md | 대상 시스템 범위 | 실제 리포지토리 연결 | 실제 코드 미연결 |
| DEV-002 | Phase 1 | Screen Scope Selection | Planning | Done | Validated | P0 | 100 | docs/PRD.md | 대표 화면군 정의 | 실제 화면 목록 추출 | 실제 코드 미연결 |
| DEV-003 | Phase 1 | Tech Stack Selection | Main | Done | Validated | P0 | 100 | docs/tech-stack-selection.md | 구현 스택 기준 | 실제 스택 확정 시 세부 조정 | 실제 코드 미연결 |
| DEV-004 | Phase 1 | External Repo Structure Analysis | Explorer | On Hold | On Hold | P0 | 10 | docs/target-codebase-intake.md | 구조 분석 결과 | 실제 외부 대상 코드베이스 연결 | 실제 대상 코드 필요 |
| DEV-005 | Phase 2 | Core Data Model | Main | Done | Validated | P0 | 100 | docs/data-model.md | 모델 코드 | 제품 레이어 연계 | 없음 |
| DEV-006 | Phase 2 | Evidence / Snapshot Store | Main | Done | Validated | P0 | 100 | docs/data-model.md | 저장 구조 | review/state 모델 확장 | 없음 |
| DEV-007 | Phase 3 | Spring Endpoint Analyzer | Main | Done | Validated | P0 | 100 | docs/analysis-pipeline.md | endpoint 분석기 | 샘플 프로젝트 확장 검증 | 없음 |
| DEV-008 | Phase 3 | Spring Service / Repository Analyzer | Main | Done | Validated | P0 | 100 | docs/analysis-pipeline.md | service/repository 분석기 | adapter 없이 커버되지 않는 패턴 분리 | 없음 |
| DEV-009 | Phase 3 | Mapping Engine | Main | Done | Validated | P0 | 100 | docs/analysis-pipeline.md | 병합 로직 | frontend analyzer 연계 시 확장 | 없음 |
| DEV-010 | Phase 4 | Search API | Main | Done | Validated | P0 | 100 | docs/architecture.md | 검색 API | UI 계약 고정 | 없음 |
| DEV-011 | Phase 4 | Snapshot / Latest API | Main | Done | Validated | P0 | 100 | docs/architecture.md | latest 조회 API | review 상태 연계 | 없음 |
| DEV-012 | Phase 4 | Node Detail API | Main | Done | Validated | P0 | 100 | docs/architecture.md | 노드 상세 API | UI 상세 계약 반영 | 없음 |
| DEV-013 | Phase 4 | Graph API | Main | Done | Validated | P1 | 100 | docs/architecture.md | 그래프 API | graph UI 연계 | 없음 |
| DEV-014 | Phase 5 | Search UI | UI Worker | Done | Validated | P0 | 100 | docs/screen-design.md | 검색 화면 | 유지보수 및 UX 보강 | 없음 |
| DEV-015 | Phase 5 | Screen Detail UI | UI Worker | Done | Validated | P0 | 100 | docs/screen-design.md | 화면 상세 | 서비스 상세 확장 여부 검토 | 서비스 전용 상세는 후속 범위 |
| DEV-016 | Phase 5 | Endpoint Detail UI | UI Worker | Done | Validated | P0 | 100 | docs/screen-design.md | 엔드포인트 상세 | 서비스 체인 UX 보강 | 없음 |
| DEV-017 | Phase 5 | Graph UI | UI Worker | Done | Validated | P1 | 100 | docs/ui-information-architecture.md | 그래프 UI | 필터/시각화 고도화 | 없음 |
| DEV-018 | Phase 6 | Review / Approval Model | Main | Done | Validated | P1 | 100 | docs/operations.md | 승인 모델 | Review UI 연계 | 없음 |
| DEV-019 | Phase 6 | Annotation / Review API | Main | Done | Validated | P1 | 100 | docs/operations.md | 검증 API | Review UI 연계 | 없음 |
| DEV-020 | Phase 6 | Review UI | UI Worker | Done | Validated | P1 | 100 | docs/operations.md | 검증 UI | 클라이언트 라우팅/상호작용 자동화 검토 | 브라우저 상호작용 테스트는 여전히 얕음 |
| DEV-021 | Phase 7 | Browser Trace Collection | Trace Worker | Done | Validated | P2 | 100 | docs/analysis-pipeline.md | trace session / event ingest API | runtime UI 연계 검토 | 브라우저 자동 수집 SDK는 후속 |
| DEV-022 | Phase 7 | Trace Correlation | Trace Worker | Done | Validated | P2 | 100 | docs/architecture.md | trace correlation API | 시각화/운영 UX 연계 검토 | 실제 브라우저 이벤트 소스는 후속 |

## 4. 병렬 수행 권장 조합

### 조합 A

- DEV-004
- DEV-014
- DEV-015

실제 외부 대상 코드 인수와 기본 검색/상세 UI를 병행할 수 있다.

### 조합 B

- DEV-017
- DEV-020
- DEV-004

Graph UI, Review UI, 외부 대상 구조 분석은 서로 다른 입력을 사용하므로 병행 가능하다.

### 조합 C

- DEV-018
- DEV-019
- DEV-020

단, DEV-020은 DEV-018, DEV-019 결과를 입력으로 사용한다.

## 5. 상황파악 에이전트 보고 형식

- 현재 단계
- 완료 작업
- 진행 중 작업
- 검증 대기 작업
- 병목 작업
- 추가 에이전트 투입 여부
- 다음 액션

## 6. 다음 액션

- 실제 외부 대상 코드베이스 연결 후 DEV-004 재개
- adapter 범위와 실제 브라우저 수집 SDK 우선순위 재정렬
- 제품 레이어 UI의 해시 라우팅/상호작용 검증 방식 확정
- runtime trace API의 UI/운영 화면 연계 여부 결정
