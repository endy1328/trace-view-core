# Trace View Core Repo Structure Analysis

## 1. 문서 목적

이 문서는 내장 참조 샘플과 실제 외부 대상 코드베이스에 대한 구조 분석 결과를 기록하는 문서다.
현재는 `내장 참조 샘플 분석 결과`가 반영되어 있고, `실제 외부 대상 코드베이스 분석`은 대기 상태다.

## 2. 현재 상태

- 문서 상태: In Progress
- 내장 참조 샘플 분석: Done
- 실제 외부 대상 코드베이스 분석: On Hold
- 보류 사유: 실제 대상 프론트/백엔드 코드베이스가 현재 작업공간에 존재하지 않음

## 3. 분석 절차

### Step 1. 프론트 구조 분석

확인 대상:

- 라우트 파일
- 화면 컴포넌트 디렉터리
- API wrapper
- fetch/axios 사용 위치

출력:

- 화면 경로 목록
- 라우트 정의 방식
- API 호출 패턴 분류

### Step 2. 백엔드 구조 분석

확인 대상:

- 컨트롤러/라우터
- 서비스 계층
- 저장소 계층
- 외부 연동 코드

출력:

- 엔드포인트 선언 방식
- 서비스 호출 체인 패턴
- 저장소/외부 호출 패턴

### Step 3. 연결 규칙 분석

확인 대상:

- 프론트 endpoint path 생성 방식
- 백엔드 path 매핑 방식
- 공통 네이밍 규칙

출력:

- 정적 분석기 구현 기준
- Mapping Engine 초기 규칙

## 4. 기록 형식

### 프론트

- 리포지토리:
- 프레임워크:
- 라우트 위치:
- API wrapper 위치:
- 대표 화면 디렉터리:

### 백엔드

- 리포지토리:
- 프레임워크:
- 엔드포인트 선언 위치:
- 서비스 위치:
- 저장소 위치:
- 외부 연동 위치:

## 5. 내장 참조 샘플 분석 결과

### 대상

- 경로: `/home/u24/projects/trace_view_core/samples/spring-reference-app`
- 목적: Spring Core Analyzer MVP 검증
- 범위: 백엔드 Spring Boot 참조 애플리케이션

### 구조 요약

- 애플리케이션 진입점:
  - `com.example.orders.ReferenceApplication`
- 엔드포인트 선언 위치:
  - `com.example.orders.controller.OrderController`
- 서비스 계층 위치:
  - `com.example.orders.service.OrderService`
- 저장소 계층 위치:
  - `com.example.orders.repository.OrderRepository`
- 외부 연동 위치:
  - `com.example.orders.client.BillingClient`

### 확인된 연결 패턴

- `OrderController -> OrderService -> OrderRepository`
- `OrderController -> OrderService -> BillingClient`
- Spring MVC endpoint, service 계층, repository 계층, external client 패턴이 모두 존재함

### 분석 결과 메모

- Spring 범용 코어의 `endpoint -> service -> repository/external` 흐름 검증용 샘플로 적합함
- `SpringReferenceSampleIntegrationTest`에서 Trace View Core 분석 대상으로 사용 중임
- 실제 제품 적용 대상 구조 분석 결과와 혼동하지 않도록 별도 섹션으로 유지함

## 6. 실제 외부 대상 코드베이스 분석 상태

- 현재 미작성
- 실제 외부 대상 코드베이스 연결 후 아래 항목을 채운다.
- 프론트 구조 분석
- 백엔드 구조 분석
- 연결 규칙 분석
