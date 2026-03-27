# Trace View Core Spring Product Strategy

## 1. 문서 목적

이 문서는 Trace View Core를 특정 프로젝트용 분석 도구에서 `Spring 기반 범용 제품`으로 전환하기 위한 제품 전략을 정의한다.

## 2. 제품 방향 전환

기존 방향:

- 특정 프론트/백엔드 프로젝트를 연결해 화면, API, 비즈니스 로직을 추적하는 내부 도구

전환 방향:

- Spring 기반 백엔드 프로젝트에서 공통적으로 동작하는 분석 코어를 제공하는 범용 제품
- 프로젝트별 차이는 플러그인 또는 어댑터로 보완

## 3. 핵심 전략

- Spring 표준 패턴을 우선 지원한다.
- 범용 코어와 프로젝트별 어댑터를 분리한다.
- 100퍼센트 자동화를 목표로 하지 않고 `표준 패턴 70~80퍼센트 자동 분석 + 나머지 확장 포인트` 구조로 설계한다.
- MVP는 Spring MVC 중심으로 제한한다.

## 4. 범용 코어 지원 범위

MVP에서 우선 지원할 범위는 아래와 같다.

- `@Controller`, `@RestController`
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`
- Controller -> Service -> Repository 호출 체인
- `@Service`, `@Component`, `@Repository`
- Spring Bean 주입 기반 의존관계
- JPA Repository 또는 Spring Data Repository 패턴
- `RestTemplate`, `WebClient`, `FeignClient` 기반 외부 호출
- `@Transactional`, Validation, Global Exception Handler 같은 공통 메타데이터

## 5. 1차 제외 범위

- WebFlux 전용 심화 분석
- Batch, Scheduler, MQ, Event-driven 전체 추적
- 멀티 모듈 초대형 모노레포 전체 자동 최적화
- 커스텀 DSL 기반 라우팅
- 조직별 복잡한 프레임워크 래퍼의 완전 자동 해석

## 6. 제품 구조

### 6.1 Core Analyzer

역할:

- Spring 표준 구조 분석
- 엔드포인트, 서비스 체인, 저장소, 외부 호출 추출
- 공통 모델과 Evidence 생성

### 6.2 Adapter Layer

역할:

- 조직별 네이밍 규칙 보강
- 공통 API wrapper 보강
- 커스텀 어노테이션 및 베이스 클래스 해석

### 6.3 Product Layer

역할:

- 검색
- 상세 조회
- 그래프
- 검증 및 승인

## 7. 범용 제품으로서의 차별점

- Swagger 문서 도구가 아니라 코드 기반 호출 체인 분석
- APM 도구가 아니라 코드와 비즈니스 로직 관점 탐색
- Spring 공통 구조를 기반으로 빠른 온보딩과 영향도 분석에 초점

## 8. MVP 성공 조건

- Spring MVC 프로젝트에서 엔드포인트와 서비스 체인을 자동 추출할 수 있어야 한다.
- 대표적인 JPA/Repository 패턴을 자동으로 연결할 수 있어야 한다.
- 외부 호출 패턴을 최소 하나 이상 지원해야 한다.
- 결과를 검색/상세/그래프 형태로 조회할 수 있어야 한다.
- 프로젝트별 특이사항은 어댑터로 보완 가능해야 한다.

## 9. 다음 단계

- Spring 분석 범위 문서 작성
- Spring 플러그인/어댑터 구조 문서 작성
- Spring 기준 MVP 백로그 재구성
