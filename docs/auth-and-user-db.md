# Auth and User Database Setup

수리의 운세노트는 카카오 로그인과 사용자별 책자 저장소를 분리해서 구성한다.

- `AuthRepository`: 카카오 로그인과 로그아웃
- `UserDataRepository`: 사용자 프로필과 책자 동기화
- `RemoteUserDatabase`: 원격 DB 포트
- `SupabaseRestUserDatabase`: 현재 Supabase 구현체

나중에 라즈베리파이 서버 DB로 바꿀 때는 `RemoteUserDatabase` 구현체를 새로 만들고 `ServiceLocator.userDataRepository`에서 구현체만 교체하면 된다.

## local.properties

```properties
kakao.native.app.key=카카오_네이티브_앱_키
supabase.url=https://프로젝트_ID.supabase.co
supabase.anon.key=SUPABASE_ANON_KEY
```

카카오 개발자 콘솔에는 Android 플랫폼 패키지명 `com.example.unum`과 키 해시 `RY4drQdBbWHDzQJCe4/mMexIgRI=`를 등록해야 한다.

## Supabase Tables

현재 앱은 `public.app_users`와 `public.fortune_books`를 사용한다.

- `app_users`: 카카오 사용자 프로필
- `fortune_books`: 사용자별 프리미엄 책자 JSON

RLS는 `x-unum-user-id` 헤더를 기준으로 자기 데이터만 읽고 쓸 수 있게 구성되어 있다. 프로덕션에서 더 강한 보안이 필요하면 Supabase Auth 또는 자체 서버 토큰 검증 레이어를 중간에 두면 된다.
