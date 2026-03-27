# Trace View Core Spring Plugin Architecture

## 1. 문서 목적

이 문서는 Trace View Core의 Spring 범용 제품화를 위한 플러그인/어댑터 구조를 정의한다.

## 2. 설계 원칙

- Core Analyzer는 Spring 표준 패턴만 책임진다.
- 프로젝트별 특수 규칙은 Adapter로 분리한다.
- Adapter가 없어도 Core만으로 기본 분석이 가능해야 한다.

## 3. 구조

### 3.1 Core Analyzer

책임:

- Spring MVC endpoint parsing
- Bean dependency parsing
- Service chain parsing
- Repository parsing
- External client parsing
- Evidence 생성

### 3.2 Adapter Interface

책임:

- 커스텀 endpoint wrapper 해석
- 조직별 naming rule 해석
- 베이스 클래스 / 공통 추상화 해석
- 커스텀 annotation 해석

## 4. Adapter가 필요한 대표 지점

- 사내 공통 Controller base class
- 공통 API 응답 래퍼
- 커스텀 Service naming
- 커스텀 Repository abstraction
- 조직 전용 HTTP client wrapper

## 5. 실행 흐름

1. Core Analyzer가 표준 구조를 먼저 분석
2. Adapter가 추가 규칙을 적용
3. Mapping Engine이 결과를 병합
4. Evidence와 confidence를 조정

## 6. MVP 기준

- Adapter 없이도 Spring 표준 프로젝트 분석 가능
- Adapter 추가 시 관계 보강 가능
- Core와 Adapter의 결과는 동일 모델로 저장
