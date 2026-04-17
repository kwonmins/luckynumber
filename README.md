# luckynumber

# Unum Local 10k App - Chunked Assets Version

- Kotlin + Jetpack Compose + MVVM
- `assets/destiny_profiles.json` : 운명수 공통 성향 10개
- `assets/life_records_0000_0999.jsonl` ~ `assets/life_records_9000_9999.jsonl` : 4자리 code 1만건 전체 인생 흐름
- 서버 없이 로컬 assets 로드
- 프리미엄 상담 전에는 데이터셋만 사용
- 전체 1만건을 시작 시 한 번에 로드하지 않고, **1000개 단위 chunk lazy loading** 방식 사용

## 핵심 구조
- 앱은 생년월일로 `code`를 계산합니다.
- 운명수 공통 프로필 10개는 `destiny_profiles.json`에서 한 번만 읽습니다.
- 인생 해석 1만건은 `code`가 속한 chunk만 필요할 때 로드합니다.
  - 예: `0781` -> `life_records_0000_0999.jsonl`
  - 예: `2335` -> `life_records_2000_2999.jsonl`
  - 예: `7818` -> `life_records_7000_7999.jsonl`
- `LocalAssetNumerologyRepository`는 최근 접근 chunk만 메모리에 유지하는 **LRU 유사 캐시(기본 3개)** 를 사용합니다.

## 장점
- 앱 시작 시 52MB 전체를 한 번에 파싱하지 않음
- 초기 진입 속도 개선
- 메모리 사용량 감소
- 로컬-only 구조 유지

## 주의
- 총 assets 용량 자체는 여전히 큽니다.
- 완전한 영업기밀 보호는 불가능합니다. 로컬에 포함된 데이터는 원칙적으로 추출 가능성이 있습니다.
- 더 강한 보호가 필요하면 나중에 Edge Function / 서버 조회형으로 바꿔야 합니다.

## 확장 포인트
- chunk 크기를 500 / 1000 / 2000 단위로 조정 가능
- Room prepackaged DB로 교체 가능
- 최근 결과 Prefs/Room 저장 가능
