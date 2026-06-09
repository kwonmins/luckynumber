# Kakao Login Setup

앱은 카카오 로그인만 사용한다.

## Android App Info

- Package name: `com.example.unum`
- Debug Kakao key hash: `RY4drQdBbWHDzQJCe4/mMexIgRI=`
- Kakao redirect scheme: `kakao{NATIVE_APP_KEY}://oauth`
- App display name: `수리의 운세노트`

## local.properties

```properties
kakao.native.app.key=카카오_네이티브_앱_키
supabase.url=https://프로젝트_ID.supabase.co
supabase.anon.key=SUPABASE_ANON_KEY
```

`local.properties`는 `.gitignore`에 포함되어 GitHub에 올라가지 않는다.

## Kakao Console Checklist

1. Kakao Developers에서 앱을 만든다.
2. Android 플랫폼을 추가한다.
3. Package name에 `com.example.unum`을 입력한다.
4. Key hash에 `RY4drQdBbWHDzQJCe4/mMexIgRI=`를 입력한다.
5. Kakao Login을 ON으로 설정한다.
6. Native app key를 `local.properties`의 `kakao.native.app.key`에 넣는다.

## Verification

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:assembleDebug
```

앱 첫 화면에서 `카카오 로그인하고 시작하기`를 누르면 카카오 로그인 페이지로 이동한다. 로그인 성공 후 앱 홈으로 진입하고, Supabase 동기화가 가능한 상태가 된다.
