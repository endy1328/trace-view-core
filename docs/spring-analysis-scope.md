# Trace View Core Spring Analysis Scope

## 1. 문서 목적

이 문서는 Spring 범용 제품으로서 Trace View Core가 분석할 범위를 정의한다.

## 2. 1차 분석 대상

### 엔드포인트 계층

- `@Controller`
- `@RestController`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@PatchMapping`

### 비즈니스 계층

- `@Service`
- `@Component`
- UseCase 성격의 서비스 클래스
- Bean 주입 기반 서비스 의존 관계

### 데이터 계층

- `@Repository`
- Spring Data JPA Repository
- Repository 인터페이스 및 구현체

### 외부 호출 계층

- `RestTemplate`
- `WebClient`
- `FeignClient`

### 메타데이터 계층

- `@Transactional`
- Validation annotation
- Global Exception Handler
- Security annotation 일부

## 3. 분석 결과 목표

- Endpoint -> Handler
- Handler -> Service
- Service -> Repository
- Service -> External Call
- Endpoint / Service 관련 메타데이터

## 4. 2차 확장 대상

- Spring Batch
- Scheduler
- Event Listener
- Kafka / MQ consumer
- WebFlux 심화 구조
- AOP 기반 간접 호출 추적

## 5. 범위 밖 항목

- 비 Spring 백엔드
- 조직 전용 프레임워크 래퍼의 완전 자동 지원
- 런타임만으로 동작하는 동적 로직의 완전 해석

## 6. 제품화 원칙

- 표준 어노테이션과 Bean 구조를 우선 사용
- 명시적 구조는 확정 관계
- 규칙 기반 추론은 추론 관계
- 프로젝트별 특수 규칙은 Adapter로 분리
