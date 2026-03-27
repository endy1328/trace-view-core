# Trace View Core Master Plan

## 문서 목적

이 문서는 Trace View Core의 전체 기획 및 설계 과정을 순서대로 관리하는 마스터 문서다.
메인 에이전트가 조율하고, 각 세부 트랙은 별도 에이전트가 수행하며, 결과물은 검증 에이전트가 리뷰한다.

## 운영 원칙

- 메인 에이전트는 전체 일정, 범위, 문서 통합을 책임진다.
- 세부 작업은 역할별 서브 에이전트가 분담한다.
- 문서와 설계 결과물은 검증 에이전트가 별도 리뷰한다.
- 진행 현황은 상황파악 에이전트 기준으로 수시 업데이트한다.
- 기획 및 설계 문서는 검증 완료 후 구현 작업의 기준 문서로 사용한다.

## 에이전트 구성

| 역할 | 담당 | 책임 범위 | 상태 |
| --- | --- | --- | --- |
| Main Agent | 메인 에이전트 | 전체 조율, 문서 통합, 우선순위 결정 | Done |
| Planning Track Agent | 기획 전담 | PRD, 범위, 단계별 산출물 정의 | Done |
| Architecture Track Agent | 설계 전담 | 시스템 구조, 데이터 흐름, 기술 설계 | Done |
| UX/Ops Track Agent | UX/운영 전담 | 화면 설계, 운영 정책, 온보딩 흐름 설계 | Done |
| Spring Strategy Track Agent | Spring 전략 전담 | Spring 제품 전략, 범위, MVP 구조 설계 | Done |
| Validation Agent | 검증 전담 | 문서 품질, 모순, 누락, 개선점 검토 | Done |
| Status Agent | 상황파악 전담 | 진행 현황 요약, 상태 관리 형식 제안 | Done |

## 전체 단계 목록

| Stage ID | 단계 | 설명 | 주요 산출물 | 담당 | 검증 담당 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| PD-01 | 비전 및 문제 정의 | 제품 목적, 사용자 문제, 목표 정리 | product-overview.md, PRD 초안 | Main, Planning Track | Validation | Done |
| PD-02 | 용어 및 범위 정의 | 화면, 비즈니스 로직, 추적 범위 정의 | PRD, spring-analysis-scope.md | Planning Track, Spring Strategy Track | Validation | Done |
| PD-03 | 사용자 및 시나리오 정의 | 핵심 사용자, 온보딩 시나리오 정리 | PRD, screen-design.md | Planning Track, UX/Ops Track | Validation | Done |
| PD-04 | 기능 요구사항 설계 | 검색, 그래프, 요약, 영향도 기능 정의 | PRD | Planning Track | Validation | Done |
| PD-05 | 비기능 요구사항 설계 | 성능, 신뢰도, 보안, 운영 요건 정의 | PRD, operations.md | Planning Track, UX/Ops Track | Validation | Done |
| PD-06 | 정보 구조 및 화면 설계 | 주요 화면, 흐름, 상태 정의 | screen-design.md, ui-information-architecture.md | UX/Ops Track | Validation | Done |
| PD-07 | 아키텍처 설계 | 계층, 컴포넌트, 데이터 흐름 정의 | architecture.md | Architecture Track | Validation | Done |
| PD-08 | 데이터 모델 설계 | 노드, 관계, 근거 저장, 승인 상태 정의 | data-model.md | Architecture Track | Validation | Done |
| PD-09 | 분석 파이프라인 설계 | 정적 분석, 런타임 추적, 요약 생성 설계 | analysis-pipeline.md | Architecture Track | Validation | Done |
| PD-10 | 운영 및 거버넌스 설계 | 권한, 승인, 문서 운영, 검증 게이트 정의 | operations.md, security-governance.md | UX/Ops Track, Main | Validation | Done |
| PD-11 | PoC 및 단계별 실행계획 | MVP 범위, 일정, 성공 기준 정리 | poc-plan.md, tech-stack-selection.md, implementation-plan.md, task-board.md | Main, Planning Track, Architecture Track | Validation | Done |
| PD-12 | Spring 제품 전략 설계 | Spring 범용 제품 전략과 MVP 구조 정의 | spring-product-strategy.md, spring-plugin-architecture.md, spring-mvp-backlog.md, spring-sample-selection.md | Spring Strategy Track | Validation | Done |
| PD-13 | 통합 검증 및 상태 체계 확정 | 전 문서 교차 검토, 상태 정의, 보고 체계 정리 | review docs, status-report.md, planning-design-track-board.md | Validation, Main, Status | Done |

## 진행 상태 규칙

| 상태 | 의미 |
| --- | --- |
| Pending | 아직 시작하지 않음 |
| In Progress | 담당 에이전트가 작업 중 |
| Review Pending | 초안 작성 완료, 검증 대기 |
| Needs Revision | 검증 결과 수정 필요 |
| Validated | 검증 완료 |
| On Hold | 선행 결정 또는 외부 조건 대기 |
| Blocked | 선행 결정 또는 정보 부족으로 중단 |
| Done | 현재 단계 산출물 반영 완료 |

## 현재 진행 현황

| 항목 | 상태 | 메모 |
| --- | --- | --- |
| Product Overview | Done | 기획 개요 문서 분리 및 검증 반영 완료 |
| PRD | Done | 범위/요구사항/상태 연계 검증 반영 완료 |
| Architecture | Done | data-model 및 analysis-pipeline 정합화 검증 반영 완료 |
| Validation Review | Done | Validation Round 3까지 반영 완료 |
| Master Plan | Done | planning-design-track-board 및 status-report와 상태 정렬 완료 |

## 문서 목록

- [product-overview.md](./product-overview.md)
- [PRD.md](./PRD.md)
- [architecture.md](./architecture.md)
- [screen-design.md](./screen-design.md)
- [data-model.md](./data-model.md)
- [analysis-pipeline.md](./analysis-pipeline.md)
- [open-questions.md](./open-questions.md)
- [operations.md](./operations.md)
- [ui-information-architecture.md](./ui-information-architecture.md)
- [security-governance.md](./security-governance.md)
- [poc-plan.md](./poc-plan.md)
- [tech-stack-selection.md](./tech-stack-selection.md)
- [implementation-plan.md](./implementation-plan.md)
- [task-board.md](./task-board.md)
- [target-codebase-intake.md](./target-codebase-intake.md)
- [repo-structure-analysis.md](./repo-structure-analysis.md)
- [spring-product-strategy.md](./spring-product-strategy.md)
- [spring-analysis-scope.md](./spring-analysis-scope.md)
- [spring-plugin-architecture.md](./spring-plugin-architecture.md)
- [spring-mvp-backlog.md](./spring-mvp-backlog.md)
- [decision-log.md](./decision-log.md)
- [status-report.md](./status-report.md)
- [planning-design-track-board.md](./planning-design-track-board.md)
- [master-plan.md](./master-plan.md)
- [2026-03-26-doc-review.md](./reviews/2026-03-26-doc-review.md)
- [2026-03-26-validation-review-round-2.md](./reviews/2026-03-26-validation-review-round-2.md)
- [2026-03-26-validation-review-round-3.md](./reviews/2026-03-26-validation-review-round-3.md)

## 다음 액션

- 제품 레이어 작업인 UI, review flow, adapter 트랙으로 전환
- 실제 외부 대상 코드베이스 연결 시 구조 분석과 intake 문서를 같은 라운드에서 갱신
- 신규 이슈가 발생할 때만 추가 검증 라운드를 연다

## 의사결정 로그

- 2026-03-26: 기획 및 설계 전 과정을 멀티 에이전트 방식으로 진행하기로 결정
- 2026-03-26: 문서와 설계 결과물은 항상 별도 검증 에이전트 리뷰를 거치도록 결정
- 2026-03-26: 기획 및 설계 문서는 검증 완료 후 구현 기준 문서로 사용하기로 결정
- 2026-03-26: 2차 검증 완료 후 기획 및 설계 단계 문서를 1차 확정했으나, 후속 정합성 보강 라운드를 진행하기로 결정
- 2026-03-26: 개발 직전 준비 문서로 implementation-plan과 task-board를 추가
- 2026-03-26: 제품 방향을 Spring 범용 분석 제품으로 전환
