$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -Scope Global -ErrorAction SilentlyContinue) {
    $global:PSNativeCommandUseErrorActionPreference = $false
}

$root = Split-Path -Parent $PSScriptRoot
$localPropertiesPath = Join-Path $root "local.properties"
$props = @{}

if (Test-Path $localPropertiesPath) {
    Get-Content $localPropertiesPath |
        Where-Object { $_ -match "^[^#].*=" } |
        ForEach-Object {
            $idx = $_.IndexOf("=")
            if ($idx -gt 0) {
                $props[$_.Substring(0, $idx).Trim()] = $_.Substring($idx + 1).Trim()
            }
        }
}

function HasValue($name) {
    return [bool]($props[$name])
}

$keytool = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
$debugKeystore = Join-Path $env:USERPROFILE ".android\debug.keystore"
$kakaoHash = ""

if ((Test-Path $keytool) -and (Test-Path $debugKeystore)) {
    $certPath = Join-Path $env:TEMP "unum_debug_cert.der"
    if (Test-Path $certPath) {
        Remove-Item -LiteralPath $certPath -Force
    }
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $keytool -exportcert -alias androiddebugkey -keystore $debugKeystore -storepass android -keypass android -file $certPath 2>$null | Out-Null
    $ErrorActionPreference = $previousErrorActionPreference
    $bytes = [System.IO.File]::ReadAllBytes($certPath)
    $sha1 = [System.Security.Cryptography.SHA1]::Create()
    $kakaoHash = [Convert]::ToBase64String($sha1.ComputeHash($bytes))
}

[pscustomobject]@{
    PackageName = "com.example.unum"
    KakaoDebugKeyHash = $kakaoHash
    HasKakaoNativeAppKey = HasValue "kakao.native.app.key"
    HasSupabaseUrl = HasValue "supabase.url"
    HasSupabaseAnonKey = HasValue "supabase.anon.key"
} | Format-List
