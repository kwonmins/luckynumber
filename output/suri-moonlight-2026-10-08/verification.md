# 수리운세: 파스텔 아이콘 · 달빛 표지 · AI 프롬프트

- APK: `suri-moonlight-pastel-noads-debug.apk` (`suri-latest.apk`와 동일)
- SHA-256: `87AAD7A70E6D06FBB72A558C6FFC9493421200EDD16FC232A201907D7CF5E412`
- Debug APK 및 Android 테스트 APK 빌드 성공.
- JVM 단위 테스트: 19개 통과, 실패 0개.
- Android 에뮬레이터 테스트: 5개 통과. 타로 중복 요청·저장, 앱 메뉴·타로 재열기·출석, 12종 표지 접근성·선택, PDF 표지 출력, 스프레드 선택·드래그 확인.
- `moonlight-cover-board.png`: 앱/PDF 공통 렌더러로 그린 12종 표지. 부제는 동일한 테스트 문구를 넣어 배치를 확인한 것이며, 실제 표지에는 생성된 책 제목이 표시됩니다.
- `moonlight-pdf-cover.png`: 실제로 저장한 PDF의 첫 페이지를 다시 렌더링한 이미지. 글자와 문양 배치 확인 완료.
- 실제 AI 호출은 하지 않았습니다. 프롬프트 데이터·JSON 형식·응답 저장·월별 이유 보존은 고정 응답으로 검증했으며 문장 품질 평가는 별도입니다.
- 새 프롬프트는 새로 생성되는 해설에 적용됩니다. 기존 저장 해설은 다시 생성하지 않습니다.
- 광고 로드·표시는 계속 비활성화 (`ADS_ENABLED = false`).

프롬프트와 디자인 관리 설명: `docs/premium-writing-and-covers.md`.
