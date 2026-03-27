# Trace View Core Spring MVP Backlog

## 1. 문서 목적

이 문서는 Spring 범용 제품 기준의 MVP 개발 백로그를 정의한다.

## 2. Epic 목록

### Epic S1. Spring Core Analyzer

- Spring MVC endpoint parser
- Bean dependency parser
- Service chain parser
- Repository parser

### Epic S2. External Call Analyzer

- RestTemplate parser
- WebClient parser
- FeignClient parser

### Epic S3. Core Model and Evidence

- Endpoint / Service / Repository / ExternalCall 모델
- Evidence 저장
- Snapshot 저장

### Epic S4. Query Product Layer

- Search API
- Endpoint detail API
- Service chain API
- Graph API

### Epic S5. Product UI

- Search UI
- Endpoint detail UI
- Graph UI

### Epic S6. Review and Approval

- Annotation
- Draft / Approved / Rejected
- Review result 저장

### Epic S7. Adapter Framework

- Adapter interface
- sample adapter
- adapter merge flow

## 3. P0 작업

- Spring endpoint parser
- Bean dependency parser
- Service chain parser
- Repository parser
- Evidence 저장
- Search / Endpoint detail API
- Search / Detail UI

## 4. P1 작업

- External call parser
- Graph API / UI
- Review / Approval 흐름
- Adapter interface

## 5. P2 작업

- Sample adapter pack
- Runtime trace 보강
- Batch / event 확장

## 6. 완료 기준

- Spring 표준 프로젝트에서 endpoint -> service -> repository 흐름이 조회 가능
- 기본 검색과 상세 탐색이 가능
- Evidence가 동작하고 승인 상태 모델은 최소 Draft/Approved/Rejected 기준으로 연동 가능
- Adapter 없이도 MVP가 성립

주의:

- 승인 상태의 전체 운영 흐름은 P1 트랙이지만, MVP 완료 기준에서는 최소 상태 모델 연동만 요구한다.
