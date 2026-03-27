# Trace View Core Analysis Pipeline

## 1. 문서 목적

이 문서는 Trace View Core의 분석 파이프라인을 정의한다.
목표는 정적 분석, 런타임 추적, 수동 보강이 어떤 입력과 출력을 갖고 연결되는지 고정하는 것이다.

## 2. 파이프라인 개요

Trace View Core의 파이프라인은 아래 흐름을 따른다.

1. 프론트 정적 분석
2. 백엔드 정적 분석
3. 관계 후보 병합
4. 런타임 추적 결합
5. 설명 생성
6. 검증 및 승인
7. 스냅샷 저장
8. 검색 및 UI 제공

## 3. Frontend Analyzer

입력:

- 라우트 정의 파일
- 화면 컴포넌트
- API client wrapper
- fetch, axios, SDK 호출 코드

출력:

- Screen
- FrontendRoute
- Screen calls ApiEndpoint 후보
- Evidence 후보

주요 규칙:

- 라우트 단위 페이지를 Screen으로 식별
- 공통 wrapper를 경유하는 호출은 추론 관계로 생성

## 4. Backend Analyzer

입력:

- 컨트롤러
- 라우터
- 핸들러
- 서비스 클래스
- 저장소 접근 코드
- 외부 API 호출 코드

출력:

- ApiEndpoint
- BackendEntryPoint
- ServiceMethod
- RepositoryCall
- ExternalCall
- 관계 후보와 Evidence 후보

## 5. Mapping Engine

역할:

- 프론트와 백엔드 결과를 공통 그래프 모델로 병합
- 중복 노드를 정규화
- 확정 관계와 추론 관계를 구분
- confidence 계산

출력:

- 통합 그래프 노드
- 통합 그래프 관계
- Evidence 엔터티와 relation.evidence_id 연결 정보

## 6. Runtime Trace Collector

입력:

- 브라우저 화면 진입 이벤트
- XHR 또는 fetch 호출 정보
- 백엔드 trace와 span 정보

출력:

- TraceSession
- Screen traced_to ApiEndpoint
- ApiEndpoint traced_to ServiceMethod 보강 정보

운영 원칙:

- 운영 환경 데이터는 마스킹 정책 적용 후 저장
- 런타임 결과는 정적 분석 보강 용도로 사용

## 7. Summary Generator

입력:

- ServiceMethod
- RepositoryCall
- ExternalCall
- 예외 처리 및 조건 분기 패턴

출력:

- Draft 상태의 비즈니스 로직 요약

규칙:

- 핵심 검증
- 저장/조회
- 외부 연동
- 주요 예외 흐름

초기 전략:

- 룰 기반 생성 우선
- 이후 LLM 보조 검토 가능

## 8. Validation Gate

역할:

- 문서와 분석 결과의 품질 검증
- 누락, 모순, 과도한 가정, 근거 부족 탐지

출력:

- Must Fix
- Recommended Improvements
- Open Questions
- Validation Status

## 9. Approval Flow

1. 자동 분석 결과 생성
2. Draft 상태 저장
3. 검증 에이전트 리뷰
4. 담당자 승인 또는 반려
5. Approved 결과만 기본 노출

## 10. 스냅샷 저장 흐름

1. 분석 실행 시작
2. 입력 코드 버전 식별
3. 분석 결과 생성
4. 검증 결과 연결
5. Snapshot으로 저장
6. 이전 스냅샷과 비교 가능하게 인덱싱

Snapshot에는 최소 아래가 함께 저장된다.

- 노드 목록
- 관계 목록
- Evidence 목록
- 검증 결과 메타데이터

## 11. 오류 및 불확실성 처리

- 낮은 confidence 관계는 추론 관계로만 노출
- 중복 노드는 병합 후보로 표시
- 근거가 부족한 관계는 저장하지 않음
- 런타임과 정적 분석이 충돌하면 검토 대상 표시

## 12. MVP 범위

- 단일 프론트 앱
- 단일 백엔드 서비스 또는 BFF
- 대표 화면군
- 정적 분석 우선
- 런타임 추적은 후속 보강

## 13. Open Questions

상세 Open Questions는 `open-questions.md`를 기준으로 관리한다.

- 공통 API wrapper 내부 추적 깊이를 어디까지 둘 것인가
- 백엔드 내부 호출을 한 단계까지만 노출할지 연쇄적으로 모두 노출할지
- 요약 생성 실패를 어떤 기준으로 감지할지
