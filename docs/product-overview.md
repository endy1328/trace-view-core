# Trace View Core Product Overview

이 문서는 원래 루트 `README.md`에 있던 기획 개요를 별도 문서로 분리한 것이다.
상세 요구사항과 설계는 [PRD.md](./PRD.md), [architecture.md](./architecture.md), [analysis-pipeline.md](./analysis-pipeline.md)를 기준으로 관리한다.

## 1. 배경

신규 인력이 프로젝트에 합류하면 보통 아래 문제를 먼저 겪는다.

- 어떤 화면이 어떤 API를 호출하는지 찾기 어렵다.
- 특정 API의 실제 진입점과 핵심 비즈니스 로직 위치를 파악하는 데 시간이 오래 걸린다.
- 화면, BFF, 백엔드 서비스, 저장소, 외부 연동이 어떻게 연결되는지 한눈에 보기 어렵다.
- 문서가 있더라도 최신 코드와 어긋나기 쉽다.

Trace View Core는 코드와 실행 흐름을 기반으로 화면, API, 로직의 관계를 시각화하고 검색 가능하게 만드는 것을 목표로 한다.

## 2. 목표

- 화면 기준으로 관련 엔드포인트를 빠르게 찾을 수 있게 한다.
- 엔드포인트 기준으로 실제 비즈니스 로직 위치를 빠르게 찾을 수 있게 한다.
- 신규 인력이 화면부터 서버 로직까지 흐름을 짧은 시간 안에 이해할 수 있게 한다.
- 코드 변경 시 영향 범위를 빠르게 가늠할 수 있게 한다.

## 3. 핵심 사용자

- 신규 입사자 또는 신규 투입 인력
- 화면 담당 개발자
- API 담당 백엔드 개발자
- 리뷰어, QA, 운영 지원 인력

## 4. 핵심 질문

- 이 화면은 어떤 API를 호출하는가?
- 이 API는 어떤 컨트롤러 또는 핸들러로 들어가는가?
- 실제 핵심 비즈니스 로직은 어느 서비스, 유스케이스, 도메인 계층에 있는가?
- 이 로직은 어떤 저장소, 외부 시스템, 배치와 연결되는가?
- 이 화면이나 API를 수정하면 어디까지 영향이 갈 가능성이 있는가?

## 5. 제품 한 줄 정의

화면에서 시작해 엔드포인트와 서버 비즈니스 로직까지 연결해 보여주는 온보딩 및 영향도 분석 도구.

## 6. 해결 방식

Trace View Core는 아래 3가지 축을 결합하는 방식으로 설계한다.

### 6.1 정적 분석

- 프론트 라우트 정의
- 화면 컴포넌트
- API 호출 래퍼
- 백엔드 라우터 또는 컨트롤러
- 서비스 및 유스케이스 계층
- 저장소 및 외부 API 호출부

### 6.2 런타임 추적

- 브라우저 화면 진입 시 발생한 XHR 또는 fetch 호출
- API 호출 순서
- 화면 기준 세션 흐름
- 백엔드 서비스 간 트레이스 연결

### 6.3 수동 보강

- 수동 설명
- 운영 메모
- 승인/반려 상태 관리

## 7. 핵심 기능

- 화면 중심 탐색
- 엔드포인트 중심 탐색
- 호출 흐름 그래프
- 비즈니스 로직 요약
- 영향도 탐색
- 리뷰 및 승인 흐름

## 8. 현재 범위

현재 구현은 `Spring 표준 패턴 기반 범용 분석 코어`를 우선 대상으로 한다.

- Spring endpoint -> service -> repository / external call 정적 분석
- snapshot 저장과 graph/query API
- annotation 생성과 approve / reject API
- 내장 샘플 프로젝트 기준 검증

아직 남은 범위:

- UI
- Review UI
- adapter framework
- runtime trace 보강
- 실제 외부 대상 코드베이스 연결 검증

## 9. 관련 문서

- [PRD.md](./PRD.md)
- [architecture.md](./architecture.md)
- [data-model.md](./data-model.md)
- [analysis-pipeline.md](./analysis-pipeline.md)
- [operations.md](./operations.md)
- [implementation-plan.md](./implementation-plan.md)
- [task-board.md](./task-board.md)
