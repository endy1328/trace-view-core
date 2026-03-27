# Trace View Core Planning and Design Track Board

## 1. 문서 목적

이 문서는 Trace View Core의 전체 기획·설계 단계를 작업 트랙 단위로 분해해 관리하는 통합 보드다.

목표는 아래를 한 문서에서 동시에 관리하는 것이다.

- 전체 기획·설계 단계 목록화
- 작업 트랙 분해
- 트랙별 담당 에이전트 지정
- 메인 에이전트 통합 조율 기준 정의
- 검증 에이전트 리뷰 체계 정의
- 상황파악 에이전트 진행 현황 보고 체계 정의
- 각 트랙의 처리 여부 표시

## 2. 운영 원칙

- 메인 에이전트는 전체 우선순위, 산출물 통합, 상태 판정을 책임진다.
- 각 트랙은 별도 에이전트가 소유한다.
- 검증 에이전트는 구현 또는 작성 에이전트와 분리한다.
- 상황파악 에이전트는 트랙별 진행률, 병목, 다음 액션을 별도로 수집해 보고한다.
- 이슈가 발생하기 전까지는 메인 에이전트가 사용자 질의 없이 작업을 계속 진행한다.
- 상태 변경은 근거 문서와 함께 갱신한다.

## 3. 에이전트 구조

| 역할 | 에이전트명 | 책임 범위 | 상태 |
| --- | --- | --- | --- |
| Main | Main Agent | 전체 조율, 우선순위, 산출물 통합, 최종 상태 확정 | Active |
| Track | Planning Track Agent | 제품 비전, 요구사항, 범위, 단계별 실행계획 | Assigned |
| Track | UX/Ops Track Agent | 화면 설계, 정보 구조, 운영/보안/거버넌스 | Assigned |
| Track | Architecture Track Agent | 시스템 구조, 데이터 모델, 분석 파이프라인, 기술 구조 | Assigned |
| Track | Spring Strategy Track Agent | Spring 제품 전략, 범위, 플러그인/MVP 구조 | Assigned |
| Validation | Validation Agent | 결과물 검토, 누락/모순/보강점 식별 | Active |
| Status | Status Agent | 진행 현황 요약, 병목, 다음 액션 보고 | Active |

## 4. 상태 규칙

본 문서의 상태는 `트랙/작업 상태`를 의미한다.
문서 단위 승인 상태인 `Approved`, `Archived`는 [operations.md](./operations.md)의 문서 상태로 별도 관리한다.

| 상태 | 의미 |
| --- | --- |
| Pending | 아직 시작하지 않음 |
| In Progress | 담당 에이전트가 작업 중 |
| Review Pending | 초안 완료, 검증 대기 |
| Needs Revision | 검증 결과 수정 필요 |
| Validated | 검증 완료 |
| Done | 메인 에이전트 통합 반영 완료 |
| On Hold | 외부 정보 또는 선행조건 대기 |
| Blocked | 진행 불가 이슈 발생 |

## 5. 기획·설계 단계 목록

| Stage ID | 단계 | 설명 | 대표 산출물 | 현재 상태 |
| --- | --- | --- | --- | --- |
| PD-01 | 비전 및 문제 정의 | 제품 목적, 핵심 문제, 성공 기준 정의 | product-overview.md, PRD.md | Done |
| PD-02 | 범위 및 용어 정의 | MVP 범위, 비목표, 용어, 분석 단위 정의 | PRD.md, spring-analysis-scope.md | Done |
| PD-03 | 사용자 및 시나리오 정의 | 사용자 유형, 탐색 시나리오, UX 흐름 정의 | PRD.md, screen-design.md | Done |
| PD-04 | 기능 요구사항 설계 | 검색, 상세, 그래프, 요약, 영향도 요구사항 정의 | PRD.md | Done |
| PD-05 | 비기능 요구사항 설계 | 성능, 신뢰도, 보안, 운영 요구 정의 | PRD.md, operations.md, security-governance.md | Done |
| PD-06 | 정보 구조 및 화면 설계 | 주요 화면, 상태, 흐름, 정보 구조 정의 | screen-design.md, ui-information-architecture.md | Done |
| PD-07 | 아키텍처 설계 | 시스템 계층, 컴포넌트, 데이터 흐름 정의 | architecture.md | Done |
| PD-08 | 데이터 모델 설계 | 공통 노드/관계/evidence/snapshot 모델 정의 | data-model.md | Done |
| PD-09 | 분석 파이프라인 설계 | 정적 분석, 런타임 추적, 승인 흐름 정의 | analysis-pipeline.md | Done |
| PD-10 | 운영/거버넌스 설계 | 승인, 보안, 문서 운영, 검증 게이트 정의 | operations.md, security-governance.md | Done |
| PD-11 | 실행 계획 및 백로그 설계 | PoC, 구현 계획, 백로그, 상태 체계 정의 | poc-plan.md, implementation-plan.md, development-backlog.md, task-board.md | Done |
| PD-12 | Spring 제품 전략 설계 | Spring 제품화 전략, 범위, MVP 구조 정의 | spring-product-strategy.md, spring-analysis-scope.md, spring-plugin-architecture.md, spring-mvp-backlog.md | Done |
| PD-13 | 검증 및 상태 체계 확정 | 리뷰, 상태 보고, 검증 문서, 마스터 문서 통합 | master-plan.md, status-report.md, docs/reviews/* | Done |

## 6. 작업 트랙 분해

| Track ID | 트랙명 | 포함 단계 | 주요 산출물 | 담당 에이전트 | 검증 담당 | 상태 담당 | 처리 여부 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TR-01 | Planning Track | PD-01, PD-02, PD-04, PD-11 | product-overview.md, PRD.md, poc-plan.md, implementation-plan.md, development-backlog.md, task-board.md | Planning Track Agent | Validation Agent | Status Agent | Done | 검증 완료, 메인 반영 완료 |
| TR-02 | UX/Ops Track | PD-03, PD-05, PD-06, PD-10 | screen-design.md, ui-information-architecture.md, operations.md, security-governance.md | UX/Ops Track Agent | Validation Agent | Status Agent | Done | 검증 완료, 메인 반영 완료 |
| TR-03 | Architecture Track | PD-07, PD-08, PD-09 | architecture.md, data-model.md, analysis-pipeline.md | Architecture Track Agent | Validation Agent | Status Agent | Done | 검증 완료, 메인 반영 완료 |
| TR-04 | Spring Strategy Track | PD-02, PD-11, PD-12 | spring-product-strategy.md, spring-analysis-scope.md, spring-plugin-architecture.md, spring-mvp-backlog.md, spring-sample-selection.md | Spring Strategy Track Agent | Validation Agent | Status Agent | Done | 검증 완료, 메인 반영 완료 |
| TR-05 | Validation and Status Track | PD-13 | master-plan.md, status-report.md, docs/reviews/2026-03-26-doc-review.md, docs/reviews/2026-03-26-validation-review-round-2.md, docs/reviews/2026-03-26-validation-review-round-3.md | Main Agent | Validation Agent | Status Agent | Done | 최종 검증 라운드 반영 완료 |

## 7. 트랙별 상세 처리 현황

### TR-01 Planning Track

- 처리 상태: Done
- 근거 문서:
  - `docs/product-overview.md`
  - `docs/PRD.md`
  - `docs/poc-plan.md`
  - `docs/implementation-plan.md`
  - `docs/development-backlog.md`
  - `docs/task-board.md`
- 완료 판정:
  - 제품 문제, 목표, 사용자, 기능/비기능 요구사항은 정리되어 있음
  - 실행 계획과 개발 백로그는 문서화되어 있으며 주요 정합성 보강이 반영됨
  - Open Questions 분리와 상태 표현 정리가 완료되었고 최종 검증까지 반영됨

### TR-02 UX/Ops Track

- 처리 상태: Done
- 근거 문서:
  - `docs/screen-design.md`
  - `docs/ui-information-architecture.md`
  - `docs/operations.md`
  - `docs/security-governance.md`
- 완료 판정:
  - 화면 구조와 운영/승인/보안 정책은 정의되어 있음
  - 검증 연결 기준과 상태 모델 분리가 반영되었고 최종 검증이 완료됨

### TR-03 Architecture Track

- 처리 상태: Done
- 근거 문서:
  - `docs/architecture.md`
  - `docs/data-model.md`
  - `docs/analysis-pipeline.md`
- 완료 판정:
  - 시스템 구조, 데이터 모델, 분석 파이프라인은 정의되어 있음
  - 공통 모델/관계/evidence 속성 정합화와 최종 검증 반영이 완료됨

### TR-04 Spring Strategy Track

- 처리 상태: Done
- 근거 문서:
  - `docs/spring-product-strategy.md`
  - `docs/spring-analysis-scope.md`
  - `docs/spring-plugin-architecture.md`
  - `docs/spring-mvp-backlog.md`
  - `docs/spring-sample-selection.md`
- 완료 판정:
  - Spring 범용 제품 기준의 분석 범위와 MVP 전략은 정의되어 있음
  - 샘플 선정, 완료 기준, 우선순위 설명의 최종 검증 반영이 완료됨

### TR-05 Validation and Status Track

- 처리 상태: Done
- 근거 문서:
  - `docs/master-plan.md`
  - `docs/status-report.md`
  - `docs/reviews/2026-03-26-doc-review.md`
  - `docs/reviews/2026-03-26-validation-review-round-2.md`
- 완료 판정:
  - 검증 및 상태 관리 체계는 문서화되어 있음
  - 최신 수정본 기준 최종 검증 라운드까지 반영 완료됨

## 8. 검증 결과 요약

### Validation Agent 최신 판정

- Validation Status: Validated
- 핵심 Must Fix:
  - 없음

### Track Agent 주요 이견

- Planning Track Agent:
  - 검증 완료
- UX/Ops Track Agent:
  - 검증 완료
- Architecture Track Agent:
  - 검증 완료
- Spring Strategy Track Agent:
  - 검증 완료

## 9. 메인 에이전트 통합 조율 규칙

- 메인 에이전트는 각 트랙 결과를 문서 단위로 병합한다.
- 메인 에이전트는 트랙 간 의존성을 조정한다.
- 메인 에이전트는 검증 결과를 수용하거나 수정 방향을 결정한다.
- 메인 에이전트는 상태 문서와 트랙 보드를 최종 기준으로 유지한다.

## 10. 검증 에이전트 운영 규칙

- 각 트랙 산출물을 독립적으로 리뷰한다.
- 결과는 `Must Fix`, `Recommended`, `Open Questions`, `Validation Status` 형식으로 보고한다.
- 같은 트랙을 작성한 에이전트가 자기 결과를 검증하지 않는다.
- 메인 에이전트는 검증 결과를 통합해 상태를 갱신한다.

## 11. 상황파악 에이전트 운영 규칙

- 트랙별 진행률과 현재 상태를 정리한다.
- 병목, 보류 사유, 다음 액션을 수시 보고한다.
- 상태 보고는 아래 형식을 따른다.

### 보고 형식

- 보고 시각
- 현재 단계
- 완료 트랙
- 진행 중 트랙
- 검증 대기 트랙
- 병목 또는 리스크
- 다음 액션

## 12. 현재 통합 상태

| 항목 | 상태 | 메모 |
| --- | --- | --- |
| 전체 기획 단계 | Done | 산출물 추적과 상태 표현 재검토 및 최종 검증 반영 완료 |
| 전체 설계 단계 | Done | 구조/데이터/파이프라인 정합화와 최종 검증 반영 완료 |
| Spring 제품 전략 단계 | Done | 전략 문서, 샘플 선정, 최종 검증 반영 완료 |
| 검증 단계 | Done | Validation Round 3 반영 완료 |
| 개발 전환 준비 | Done | 기획·설계 문서를 구현 전환 기준 문서로 사용할 수 있음 |

## 13. 다음 액션

- 제품 레이어 작업인 UI, review flow, adapter 트랙으로 전환한다.
- 실제 외부 대상 코드베이스 연결 시 `DEV-004`와 구조 분석 문서를 재개한다.
- 문서 트랙은 신규 이슈 발생 시에만 추가 검증 라운드를 연다.
