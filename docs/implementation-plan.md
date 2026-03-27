# Trace View Core Implementation Plan

## 1. 문서 목적

이 문서는 Trace View Core를 실제 구현 단계로 전환하기 직전의 실행 계획을 정의한다.
목표는 개발 시작 후 의사결정 지연과 에이전트 충돌을 최소화하는 것이다.

## 2. 구현 원칙

- MVP는 `Spring 표준 패턴 기반 범용 분석 코어`를 기준으로 한다.
- 구현 순서는 `공통 모델 -> Spring Core Analyzer -> 조회 API -> 최소 UI -> 검증 흐름 -> Adapter 구조 -> 런타임 보강`을 따른다.
- 기능 단위보다 `수직 슬라이스` 단위로 우선 완성한다.
- 자동 분석 결과는 Evidence 없이 확정하지 않는다.
- 검증 흐름은 기능 구현과 별도 트랙이 아니라 필수 동시 구현 항목이다.

이 문서의 상태 표시는 `계획/작업 상태` 기준이며 문서 승인 상태와 구분한다.

## 3. 제품 레이어 전환 준비 완료 기준

아래 항목이 준비되면 UI, review flow, adapter, runtime 보강을 포함한 제품 레이어 구현에 착수할 수 있다.

- 제품 요구사항 문서 확정
- 아키텍처 문서 확정
- 데이터 모델 문서 확정
- 분석 파이프라인 문서 확정
- 운영 및 보안 정책 문서 확정
- 개발 착수용 백로그 작성 완료
- 에이전트 운영 규칙 문서 작성 완료

현재 상태:

- 문서 준비: 검증 완료
- Spring 코어 구현: 완료
- 제품 레이어 구현: 부분 진행 중

## 4. 구현 단계

### Phase 1. 대상 코드베이스 구조 분석

목표:

- Spring 표준 패턴과 실제 대상 코드베이스 구조를 파악한다.

구분:

- 내장 참조 샘플: Core Analyzer MVP 검증용으로 사용
- 실제 외부 대상 코드베이스: 제품 적용 전 구조 분석과 화면/리포지토리 연계용으로 별도 관리

해석 기준:

- 내장 참조 샘플 검증은 Spring 코어 구현과 제품 레이어 전환의 기본 게이트다.
- 실제 외부 대상 코드베이스 분석은 제품 적용 정확도를 높이기 위한 병행 트랙이며, 외부 코드 미연결 상태에서도 UI, review flow, adapter의 공통 제품 레이어 구현은 진행할 수 있다.

산출물:

- 라우트 정의 위치
- API wrapper 위치
- 백엔드 엔드포인트 위치
- 서비스 계층 구조
- 저장소/외부 연동 패턴

완료 조건:

- 내장 참조 샘플 기준 코어 검증이 완료된다.
- 실제 외부 대상 코드가 연결되면 DEV-001 ~ DEV-004 입력이 실제 외부 대상 코드 기준으로 확정된다.

### Phase 2. 기반 계층 구현

목표:

- 공통 모델과 저장 구조를 먼저 구현한다.

산출물:

- 도메인 모델
- Evidence 구조
- Snapshot 구조
- 관계 저장 구조

완료 조건:

- 분석 결과를 코드로 저장할 최소 구조가 동작한다.

### Phase 3. Spring Core Analyzer MVP 구현

목표:

- Spring endpoint -> service -> repository 연결을 구현한다.

산출물:

- Spring Endpoint Analyzer
- Spring Service Chain Analyzer
- Spring Repository Analyzer
- Mapping Engine

완료 조건:

- Spring 표준 프로젝트에서 기본 연결 그래프가 생성된다.

### Phase 4. 조회 API 구현

목표:

- 검색과 상세 보기용 API를 제공한다.

산출물:

- Search API
- Screen Detail API
- Endpoint Detail API
- Graph API

완료 조건:

- UI 없이도 핵심 탐색 데이터가 조회 가능하다.

### Phase 5. MVP UI 구현

목표:

- 신규 인력 온보딩에 필요한 최소 화면을 구현한다.

산출물:

- 검색 화면
- 화면 상세
- 엔드포인트 상세
- 그래프 화면

완료 조건:

- 사용자가 화면 기준으로 API와 서비스 체인을 탐색할 수 있다.

### Phase 6. 검증 및 승인 흐름 구현

목표:

- Draft/Approved/Rejected, Must Fix/Recommended 흐름을 구현한다.

산출물:

- 주석/승인 API
- 검증 상태 저장 구조
- 검증/리뷰 UI

완료 조건:

- 문서 및 결과물 검증 흐름이 동작한다.

### Phase 6.5 Adapter Framework 구현

목표:

- 프로젝트별 특수 규칙을 붙일 확장 구조를 만든다.

산출물:

- Adapter interface
- sample adapter
- merge 규칙

완료 조건:

- Core와 분리된 확장 포인트가 동작한다.

### Phase 7. 런타임 추적 보강

목표:

- 개발/스테이징 환경에서 정적 분석을 보강한다.

산출물:

- 브라우저 호출 수집
- 백엔드 trace 연결
- 보강 규칙

완료 조건:

- 일부 동적 호출을 런타임 데이터로 보강할 수 있다.

## 5. 우선순위

### P0

- 공통 모델
- Evidence 저장
- Spring Core Analyzer
- Mapping Engine
- Search API
- Screen / Endpoint Detail API
- Search / Detail UI

### P1

- Graph API
- Graph UI
- Annotation / Review / Approval 흐름

### P2

- Runtime Trace Collector
- Snapshot Compare
- Impact 보강

## 6. 에이전트 배치 원칙

- 메인 에이전트는 공통 모델, API 계약, 우선순위를 직접 통제한다.
- 구조 분석과 리서치는 탐색 에이전트로 분리한다.
- 저장 구조, 분석기, API, UI는 파일 범위가 겹치지 않으면 병렬 수행한다.
- 검증 에이전트는 구현 에이전트와 분리한다.
- 지연이 발생하면 보조 에이전트를 즉시 추가 투입한다.

## 7. 의존성 규칙

- 데이터 모델 확정 전 조회 API를 확정하지 않는다.
- Mapping Engine 출력이 고정되기 전 그래프 UI를 고도화하지 않는다.
- Approval 상태 모델 없이 자동 설명 기능을 노출하지 않는다.
- 보안 정책 없이 런타임 추적을 켜지 않는다.

## 8. 검증 게이트

각 Phase 종료 시 아래를 확인한다.

- 문서와 구현이 정합적인가
- 테스트 가능 상태인가
- Evidence 저장이 누락되지 않았는가
- 상태 모델이 반영되었는가
- 민감정보 정책을 위반하지 않는가

## 9. 병목 대응 규칙

- 구조 분석이 지연되면 프론트/백엔드 전용 탐색 에이전트를 추가한다.
- 공통 모델 논의가 길어지면 메인 에이전트가 결정을 고정한다.
- 검증이 지연되면 추가 검증 에이전트를 생성한다.
- UI 구현이 API 대기로 묶이면 mock contract 기반으로 병렬 진행한다.

## 10. 다음 문서

- `docs/task-board.md`
- `docs/reviews/implementation-review-*.md`
- `docs/status-report.md` 갱신

## 11. 현재 단계 해석

- Phase 2 ~ Phase 4의 Spring 코어 구현 항목은 선행 완료됐다.
- 현재 남은 핵심 작업은 Phase 1의 `실제 외부 대상 코드베이스 구조 분석` 병행 트랙과 Phase 5 이후 `남은 제품 레이어 구현`이다.
- 따라서 이 문서는 `전체 제품 전환 계획` 기준으로 Validated/Done 상태의 기준 문서이며, 이미 완료된 코어 구현 이력과 충돌하지 않도록 해석한다.
