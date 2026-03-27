# Trace View Core Spring Sample Selection

## 1. 문서 목적

이 문서는 Spring 범용 제품 MVP를 검증할 샘플 코드베이스 선정 기준과 현재 선정 상태를 정의한다.

주의:

- 본 문서의 샘플은 `Core Analyzer MVP 검증용 내장 참조 샘플`이다.
- 실제 제품 적용 대상 외부 코드베이스 선정과 구조 분석은 `target-codebase-intake.md`, `task-board.md`의 별도 작업으로 관리한다.

## 2. 현재 선정 상태

- 상태: Done
- 선정 결과: `/home/u24/projects/trace_view_core/samples/spring-reference-app`
- 선정 이유:
  - `Controller -> Service -> Repository -> FeignClient` 흐름이 모두 포함됨
  - Spring Boot 기반이며 MVC endpoint가 명확함
  - 로컬 워크스페이스 내에서 즉시 재현 가능함
  - Trace View Core 테스트에서 직접 분석 대상으로 사용 가능함

## 3. 샘플 선정 기준

- Spring Boot 기반
- MVC endpoint가 명확히 존재
- Service / Repository 계층이 분리되어 있음
- JPA 또는 대표 저장소 패턴이 존재
- 외부 호출 패턴이 최소 1개 이상 존재
- 빌드와 테스트가 재현 가능

## 4. 우선 선호 구조

- `Controller -> Service -> Repository`가 명확한 구조
- `RestTemplate`, `WebClient`, `FeignClient` 중 최소 하나 사용
- 커스텀 래퍼가 과도하지 않은 구조

## 5. 피해야 할 초기 샘플

- AOP와 사내 프레임워크 래퍼가 과도한 프로젝트
- Batch / MQ / Event 기반이 주 흐름인 프로젝트
- 실행 재현이 어려운 대형 모노레포

## 6. 샘플 확보 후 바로 할 일

1. `repo-structure-analysis.md` 채우기
2. `task-board.md`의 DEV-004 이후 상태 갱신
3. Spring Core Analyzer 구현 입력값 확정

## 7. 현재 반영 상태

- 샘플 프로젝트 생성 완료
- Spring 코어 분석 테스트 대상 연결 완료
- 다음 단계는 `repo-structure-analysis.md`에 본 샘플 구조를 반영하는 것
