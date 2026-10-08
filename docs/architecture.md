# 수리운세 수정 안내

풀이를 바꿀 때는 아래 5개 파일부터 확인한다.

| 목적 | 파일 |
|---|---|
| 모든 무료 문구 | `app/src/main/assets/FreeFortuneContent.json` |
| 무료 풀이 선택·조합·오늘 점수 | `data/content/FreeFortuneEngine.kt` |
| 개인 AI 지시문·출력 항목 | `data/ai/premium/PersonalPrompt.kt` |
| 궁합 AI 지시문·관계별 항목 | `data/ai/compatibility/CompatibilityPrompt.kt` |
| AI 실행·응답 검증 | `data/ai/AiConsultationService.kt` |

## 무료 데이터

- profiles: 숫자 0~9의 기본 성향과 생애·관계·일·돈 문구.
- interactions: 핵심 숫자별 초년·중년 숫자 조합 100개.
- genderContext: 성별에 따른 맥락 문구.
- daily: 오늘의 제목·요약·분야 문구·키워드·행운 색·행운 시간.
- version: 현재 1. 구조를 변경할 때 엔진과 함께 수정한다.

기존 프로필 10개, 숫자 조합 문구 100개, 오늘 문구 50개는 그대로 옮겼다. 건강·행운 분야 문구는 새 영역으로 추가했다.
점수는 날짜와 수리 숫자로 결정되는 65~95 범위의 참고 지표이며 확률이나 검증된 예측 정확도가 아니다. 동일한 입력과 날짜는 동일한 결과를 만든다.

## 독립적으로 유지하는 영역

- API 통신: `data/remote/ai/OpenAiChatClient.kt`, `domain/service/JsonChatClient.kt`.
- 계산: `domain/NumerologyCalculator.kt`. 양력·음력 변환 규칙을 유지한다.
- 저장·동기화: `data/repository/books`, `data/repository/user`.
- 화면: `presentation`의 기능별 폴더.
- 디자인: `ui/theme`, `ui/components/QuietDesign.kt`, `PastelArt.kt`.
- 음성: `BuildSuriSpeechScriptUseCase.kt`는 생성 결과를 읽는 연결 문구만 담당한다.

## 새 화면 기준

첨부 기획의 Cream #FBF7F2, Surface #FFFDFC, Light Lavender #EEE8F5, Main Lavender #9A86B8, Deep Purple #382A55를 적용했다.
홈은 인사 → 오늘의 점수 → 무료 3×2 → Premium 구성이다. 하단 메뉴는 홈 → 오늘운세 → Premium → 보관함 → 마이다.
마스코트는 시작·작은 인사·도움말·로딩에만 작게 사용하고 결과 본문에서는 제외한다.
질문 CTA는 고정 하단에 배치한다. 오늘 결과는 분야별 점수·키워드·해석과 행운 요소로 구성한다.
기존 상세 수리 풀이는 오늘 결과의 ‘나의 기본 수리 풀이 보기’에서 계속 확인할 수 있다.
AI 결과는 여백과 글자 구조로 구분하며 화면마다 카드를 반복하지 않는다.

실제 서버 이전, DB 스키마 변경, 스토어 결제 연동은 이번 변경 범위에 포함되지 않는다.
