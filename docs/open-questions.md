# Trace View Core Open Questions Register

## 1. 문서 목적

이 문서는 기획·설계 및 구현 전환 과정에서 남아 있는 Open Questions를 한 곳에 분리해 관리한다.
PoC 인수 조건과 검증 기준에서 요구하는 `Open Questions 분리`의 기준 문서로 사용한다.

## 2. 운영 규칙

- Open Question은 반드시 원문서 출처를 함께 남긴다.
- 해결되면 상태를 `Closed`로 변경하고 관련 문서 반영 위치를 기록한다.
- 아직 판단이 필요한 질문만 `Open`으로 유지한다.

## 3. 상태 규칙

| 상태 | 의미 |
| --- | --- |
| Open | 아직 결정되지 않음 |
| In Review | 검토 중 |
| Closed | 결정 및 문서 반영 완료 |

## 4. Open Questions

| ID | 주제 | 내용 | 출처 문서 | 상태 | 다음 액션 |
| --- | --- | --- | --- | --- | --- |
| OQ-001 | Frontend Scope | 프론트 분석 범위를 MVP에서 제외한 상태로 유지할지, 후속 단계에서 어떤 기준으로 재도입할지 | architecture.md | Open | 제품 레이어 착수 전 범위 재판단 |
| OQ-002 | API Wrapper Depth | 공통 API wrapper 추적 깊이를 1단계로 제한할지, wrapper 내부까지 추적할지 | architecture.md, analysis-pipeline.md, PRD.md | Open | Frontend analyzer 설계 시 고정 |
| OQ-003 | Service Chain Depth | 백엔드 내부 호출을 엔드포인트 기준 몇 단계까지 기본 노출할지 | architecture.md, analysis-pipeline.md | Open | 그래프/UI 계약 확정 시 결정 |
| OQ-004 | BusinessModule Strategy | BusinessModule 분류를 규칙 기반으로 둘지 수동 보강 중심으로 둘지 | architecture.md, data-model.md | Open | 데이터 모델 확정 라운드에서 결정 |
| OQ-005 | Screen Granularity | 모달, 위저드, 탭을 언제 독립 화면으로 승격할지 | PRD.md, screen-design.md | Open | 화면 모델 확장 시 결정 |
| OQ-006 | Runtime Policy | 운영 환경 추적을 어떤 조건에서 허용할지 | operations.md | Open | 보안/운영 정책 확정 시 결정 |

## 5. 최근 변경

- 2026-03-26: PoC 인수 조건 충족을 위해 분리 문서로 등록
