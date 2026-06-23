# UI/UX 모듈화 계획

## 목표

- 기획 변경이 들어오면 화면 파일 전체를 뒤지지 않고 스펙 파일부터 수정한다.
- 화면은 `Screen`, `Section`, `Card`, `Spec` 단위로 나눈다.
- 긴 파일은 기능별로 분리하고, 공통 UI는 `ui/components`에만 둔다.

## 권장 구조

```text
presentation/
  spec/                 # 기획서에서 자주 바뀌는 라벨, 순서, 분류 규칙
  home/                 # 홈 화면 전용 섹션과 카드
  premium/
    personal/           # 운세노트
    compatibility/      # 궁합, 짝사랑, 재회
    book/               # 책자 제작/표지/목차
  library/              # 보관함, 필터, PDF 공유
  settings/             # 설정, 동기화, 계정
```

## 1차 적용

- `presentation/spec/FeatureSpecs.kt`를 추가했다.
- 분야 라벨, 보관함 분류 키워드, 책자 라벨을 한 곳에서 관리하도록 시작했다.

## 다음 정리 순서

1. `PremiumScreen.kt`에서 궁합 입력 영역을 `premium/compatibility`로 분리한다.
2. 운세노트 입력 영역을 `premium/personal`로 분리한다.
3. 책자 제작/표지/목차/본문 화면을 `premium/book`으로 분리한다.
4. `PremiumComponents.kt`의 보관함/리더 컴포넌트를 `library`와 `reader` 전용 파일로 나눈다.
5. 홈의 분야별 운세 카드도 `FeatureSpecs`를 읽도록 연결한다.

## 기획서 반영 규칙

- 분야 이름, 순서, 보관함 키워드 변경: `FeatureSpecs.kt`
- 화면 문구 변경: 해당 feature의 `Copy` 또는 `Spec` 파일
- 레이아웃 변경: 해당 feature의 `Section` 또는 `Card` 파일
- 데이터셋 변경: `assets`와 repository/parser
