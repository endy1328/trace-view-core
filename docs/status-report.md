# Trace View Core Status Report

## 보고 기준

- 보고 시각: 2026-03-27
- 보고 주체: Status Agent 기준 포맷, 메인 에이전트 반영

## 상태 추적 컬럼

| ID | Phase | Workstream | Owner Agent | Status | Validation Status | Progress | Last Update | Next Action | Risk / Blocker |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Planning | Product Overview / PRD | Main / Planning Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 2 | Design | Architecture | Main / Architecture Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 3 | Design | Screen Design | Main / UX/Ops Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 4 | Design | Data Model | Main / Architecture Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 5 | Design | Analysis Pipeline | Main / Architecture Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 6 | Design | Operations / Security | Main / UX/Ops Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 7 | Planning | PoC Plan / Tech Stack | Main / Planning Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 8 | Governance | Decision Log | Main | Done | Validated | 100 | 2026-03-26 | 사용자 확인 대기 | 없음 |
| 9 | Governance | Validation Review | Validation Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 10 | Pre-Dev | Implementation Plan | Main | Done | Validated | 100 | 2026-03-26 | 제품 레이어 전환 기준으로 유지 | 없음 |
| 11 | Pre-Dev | Task Board | Main / Status Agent | Done | Validated | 100 | 2026-03-26 | 제품 레이어 DEV-014 이후 항목 추적 | 문서 정합성 완료, 개발 보드는 계속 활성 |
| 12 | Pre-Dev | Target Codebase Intake | Main | Done | Validated | 100 | 2026-03-26 | 실제 외부 대상 코드베이스 연결 대기 | 실제 대상 코드 필요 |
| 13 | Pre-Dev | Repo Structure Analysis | Explorer | Done | Validated | 100 | 2026-03-27 | module별 adapter 규칙 심화 | XML/action/batch 세부 규칙은 후속 |
| 14 | Strategy | Spring Product Strategy | Main / Spring Strategy Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 15 | Strategy | Spring Scope / Plugin Design | Main / Spring Strategy Track Agent | Done | Validated | 100 | 2026-03-26 | 유지 | 없음 |
| 16 | Dev | Spring Core Scaffold | Main | Done | Validated | 100 | 2026-03-26 | 샘플 프로젝트 확장 검증 및 제품 레이어 전환 | 없음 |
| 17 | Dev | Review / Approval Model | Main | Done | Validated | 100 | 2026-03-26 | Review UI 연계 완료 | 없음 |
| 18 | Dev | Annotation / Review API | Main | Done | Validated | 100 | 2026-03-26 | Review UI 연계 완료 | 없음 |
| 19 | Dev | Search UI | Main / UI Worker | Done | Validated | 100 | 2026-03-27 | 브라우저 플로우 테스트 보강 | 해시 라우팅 자동화 검증 미흡 |
| 20 | Dev | Screen / Endpoint Detail UI | Main / UI Worker | Done | Validated | 100 | 2026-03-27 | 서비스 상세 화면 분리 여부 검토 | 서비스 전용 상세는 후속 범위 |
| 21 | Dev | Graph UI | Main / UI Worker | Done | Validated | 100 | 2026-03-27 | 필터링/레이아웃 고도화 | 없음 |
| 22 | Dev | Review UI | Main / UI Worker | Done | Validated | 100 | 2026-03-27 | 클라이언트 라우팅/상호작용 자동화 검토 | 브라우저 레벨 검증 얕음 |
| 23 | Dev | Runtime Trace Collection API | Main / Trace Worker | Done | Validated | 100 | 2026-03-27 | UI/SDK 연계 여부 검토 | 실제 브라우저 자동 수집은 후속 |
| 24 | Dev | Runtime Trace Correlation API | Main / Trace Worker | Done | Validated | 100 | 2026-03-27 | correlation 시각화 확장 검토 | 정적 graph와의 운영 UX는 후속 |
| 25 | Dev | Adapter Framework / AStore Legacy Adapter | Main | Done | Validated | 100 | 2026-03-27 | module별 규칙 확장 | canonicalization 완료, XML/action 세부 규칙은 후속 |
| 26 | Dev | Module Classification / Adapter Recommendation API | Main | Done | Validated | 100 | 2026-03-27 | module별 adapter 세분화 진행 | module 내부 심화 규칙은 후속 |

## 수시 보고 포맷

- 보고 시각
- 변경된 작업
- 이전 상태 -> 현재 상태
- 핵심 변경 내용
- 영향 받는 문서
- 다음 액션

## 현재 요약

- 기획 및 설계 전체 단계 목록화 문서 생성 완료
- planning-design-track-board 기준으로 트랙 재분류 완료
- Planning, UX/Ops, Architecture 트랙은 검증 완료 후 Done으로 승격
- 상태 체계와 단계 체계 정렬 및 Validation Round 3 반영 완료
- 보안 거버넌스, UI 정보구조, 기술 선택 기준 문서 추가 완료
- 구현 계획과 작업 보드 상태 체계 정렬 완료
- 코드베이스 인수 문서와 구조 분석 템플릿 추가 완료
- Spring 범용 제품 전략 및 Spring 전용 MVP 문서 추가 완료
- MVP 검증용 로컬 Spring 샘플 프로젝트 선정 및 생성 완료
- 실제 외부 대상 코드베이스 AStore 연결 및 구조 분석 완료
- Spring Boot 코어 스캐폴딩과 기본 분석기/API 구현 완료
- Spring 표준 패턴 기준 endpoint -> service -> repository/external 흐름 검증 완료
- Graph API, node detail, endpoint service chain 조회 API 보강 완료
- Review / Approval Model과 Annotation API 구현 완료
- Spring Boot 정적 리소스 기반 제품 레이어 UI 구현 완료
- Search, Node/Endpoint Detail, Graph, Review 화면과 기본 해시 라우팅 제공
- 정적 UI 서빙, 빈 스냅샷 latest/graph 응답, API 예외 처리 검증 테스트 추가 완료
- 시드된 query/review API 통합 테스트 추가 후 전체 테스트 16건 통과
- runtime trace session / event ingest / correlation API 구현 및 통합 테스트 추가 완료
- adapter framework와 `astore-legacy` adapter 구현, canonicalization 반영 완료
- analysis API에서 `adapterId` 선택 및 unknown adapter 검증 추가 완료
- module classifier와 adapter recommendation API 추가 후 AStore를 5개 단위로 분류 완료
- 분류 결과는 batch, Admin, Carrier, Seller, lib 단위로 확인됨
- AStore 구조 분석 결과 기준으로 웹 3개 + 배치 1개 + 공유 라이브러리 1개 단위 분리가 적절함을 확인
- 전체 테스트 기준선은 23건 통과로 갱신
- 다음 단계는 module별 XML/action/batch 세부 규칙 심화와 runtime trace UI/SDK 연계 검토
