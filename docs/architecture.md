# 수리운세 앱 구조와 변경 위치

현재 구조는 하나의 Android 앱 모듈 안에서 책임을 패키지와 인터페이스로 분리한다.
별도 서버 배포나 DB 테이블 변경은 이번 구조 정리에 포함되지 않는다.

| 책임 | 위치 | 수정할 때 |
|---|---|---|
| 화면 | `presentation/{home,input,result,premium,library,reader,settings,onboarding,payment}` | 화면 배치와 사용자 동작 |
| 공통 디자인 | `ui/components`, `ui/theme` | 여러 화면에서 사용하는 디자인 |
| 화면 상태 조정 | `presentation/AppViewModel.kt`, `AppUiState.kt` | 기능 간 화면 상태 연결 |
| 앱 구성 | `di/AppContainer.kt` | 구현체 선택과 의존성 연결 |
| 수리 계산 및 무료 결과 생성 | `domain/usecase`, `domain/NumerologyCalculator.kt` | 계산 규칙과 결과 조립 |
| 무료 원본 콘텐츠 | `assets/destiny_profiles.json`, `data/content` | 숫자별 문구와 조합 문구 |
| 무료 결과 문구 조립 | `data/content/FreeReadingRecordComposer.kt` | 무료 리포트의 항목별 구성 |
| AI 실행 | `domain/usecase/Generate*ConsultationUseCase.kt` | 프롬프트 → 호출 → 파싱 순서 |
| AI 호출 계약 | `domain/service/JsonChatClient.kt` | 전송 구현을 교체할 때 |
| HTTP 구현 | `data/remote/ai/OpenAiChatClient.kt` | 통신, 타임아웃, API 오류 |
| AI 프롬프트 | `data/ai/{premium,compatibility}/*Prompts.kt` | AI 지시문과 모델 설정 |
| AI 응답 처리 | 같은 폴더의 `*ResponseParser.kt` | JSON 해석, 정규화, 길이 제한 |
| AI 공통 해석 규칙 | 같은 폴더의 `*Rules.kt` | 프롬프트와 응답 처리에서 공유하는 구조 |
| 책자 저장 계약 | `data/repository/books/FortuneBookRepository.kt` | 화면이 사용하는 저장 작업 |
| 책자 저장·동기화 | `DefaultFortuneBookRepository.kt` | 로컬/원격 결과 병합과 저장 |
| 책자 정렬·해석 규칙 | `domain/books/FortuneBookPolicy.kt` | 충돌 해결, 정렬, 월별 해석 갱신 |
| 로컬 저장 | `data/repository/*Store.kt` | SharedPreferences 저장 형식 |
| 원격 DB | `data/repository/user` | 사용자·책자·지갑 데이터 접근 |
| DB 스키마 | `supabase/migrations` | 테이블 및 접근 정책 |

## 의존성과 변경 규칙

- 화면은 HTTP 또는 Supabase SQL을 직접 다루지 않는다.
- AI 실행 UseCase는 `JsonChatClient` 계약을 사용한다. 구체적인 HTTP 구현은 AppContainer에서 주입한다.
- 프롬프트와 파서는 통신을 하지 않는다. 같은 기능 폴더의 Rules를 공유한다.
- 책자 화면 상태와 로딩 표시는 ViewModel이, 저장과 동기화 절차는 Repository가 담당한다.
- 책자 데이터 형식을 바꿀 때는 `Models.kt`, `FortuneBookJsonCodec.kt`, 원격 `book_json`의 이전 데이터 호환성을 함께 확인한다.
- 화면 함수 이름과 내비게이션 경로는 유지한다. 상담 화면은 진입점, 개인 입력, 궁합 입력, 공통 입력 UI, 진행 단계, 책자 화면으로 분리했다.

## 현재 저장 동작 및 남아 있는 경계

- 무료 결과는 로컬 원본 콘텐츠와 계산 규칙으로 재생성한다. 별도의 무료 결과 스냅샷 DB는 없다.
- 최근 검색은 메모리이며 생년월일 입력은 로컬 설정에 저장한다.
- 상담 책자는 로컬 JSON 저장소와 로그인 사용자의 원격 `fortune_books`에 저장한다.
- AppViewModel은 아직 여러 기능의 상태를 조정한다. 기능별 ViewModel 분리는 화면 간 결과 전달 설계를 함께 변경해야 한다.
- OpenAI 호출은 현재 앱에서 실행되며 API 키도 기존 BuildConfig 경로를 유지한다. 서버 API 이전은 별도 작업이다.
- 기존 계정 간 로컬 책자 병합 방식과 실패 시 동기화 방식은 유지했다. 계정별 저장 영역과 재시도 큐는 별도 설계가 필요하다.
- 패키지 분리는 빌드 모듈의 강제 경계가 아니다. 추후 필요하면 API·저장·기능별 Gradle 모듈로 확장한다.

## 검증

`gradlew.bat --no-daemon :app:assembleDebug :app:testDebugUnitTest`

책자 정책 테스트는 최신 결과 병합, 동일 시각의 로컬 변경 보존, 정렬 우선순위를 검증한다.
