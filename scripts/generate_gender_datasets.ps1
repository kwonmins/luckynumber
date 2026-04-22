Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$assetsDir = Join-Path $root "app\src\main\assets"

function Get-Polarity([int]$destiny) {
    if ($destiny -eq 0) { return "neutral" }
    if ($destiny % 2 -eq 0) { return "yin" }
    return "yang"
}

function Get-GenderFlavor([string]$gender, [int]$destiny) {
    $polarity = Get-Polarity $destiny

    if ($polarity -eq "neutral") {
        if ($gender -eq "male") {
            return @{
                profile = "운명수 0은 비워 두는 힘이 큰 수라 성별보다 현재의 선택 방식이 더 크게 작용하지만, 남성의 흐름에서는 답을 오래 혼자 짊어지지 않는 태도가 특히 중요합니다."
                early = "어린 시절에는 감정을 바로 꺼내기보다 혼자 정리하려는 반응이 먼저 자리 잡기 쉽습니다."
                middle = "중년에는 책임을 혼자 떠안지 않고 일을 나누는 태도가 흐름을 더 편안하게 만듭니다."
                late = "후반으로 갈수록 비워낼 것과 붙들 것을 차분히 가르는 태도가 큰 힘이 됩니다."
                life = "이 흐름은 남성의 자리에서 여백을 버팀목으로 바꾸는 방식이 중요하게 작용합니다."
                summary = "남성 흐름에서는 비워 두는 힘이 답답함이 되지 않도록 작은 결정부터 마무리하는 편이 좋습니다."
                advice = "혼자 오래 끌어안기보다 오늘 끝낼 작은 한 가지를 먼저 정해보세요."
                keyword = "재정비"
                caution = "답답함"
            }
        }

        return @{
            profile = "운명수 0은 여백과 전환의 수라 성별보다 현재의 선택 방식이 더 크게 작용하지만, 여성의 흐름에서는 주변의 결을 먼저 읽느라 내 뜻을 뒤로 미루지 않는 태도가 중요합니다."
            early = "어린 시절에는 분위기를 먼저 읽고 맞추려는 감각이 빠르게 자리 잡을 수 있습니다."
            middle = "중년에는 여유를 남기면서도 내 기준을 먼저 세우는 태도가 흐름을 안정시킵니다."
            late = "후반으로 갈수록 비워낼 것과 지킬 것을 나누는 직관이 더 또렷해집니다."
            life = "이 흐름은 여성의 자리에서 여백을 직관으로 바꾸는 방식이 중요하게 작용합니다."
            summary = "여성 흐름에서는 여백이 망설임이 되지 않도록 마음의 기준을 먼저 붙잡는 편이 좋습니다."
            advice = "주변의 반응을 보기 전에 내 마음의 기준 한 줄을 먼저 적어보세요."
            keyword = "직관"
            caution = "망설임"
        }
    }

    if ($gender -eq "male" -and $polarity -eq "yang") {
        return @{
            profile = "남성의 흐름 안에서 이 수의 양의 기운이 더 힘 있게 살아나 추진력과 결단이 빠르게 드러나는 편입니다. 좋은 때는 길을 여는 힘이 되지만, 서두름은 줄일수록 안정감이 커집니다."
            early = "어린 시절에는 하고 싶은 마음이 빨리 올라와 스스로 방향을 잡으려는 성향이 일찍 드러날 수 있습니다."
            middle = "중년에는 결정과 실행의 속도가 장점이 되지만, 중요한 선택일수록 한 번 더 확인하는 태도가 성과를 오래 남깁니다."
            late = "후반으로 갈수록 주도권을 놓지 않으려는 힘이 커질 수 있어, 속도보다 의미를 먼저 고르는 균형이 중요합니다."
            life = "이 흐름은 남성의 자리에서 양의 기운과 공명해 삶의 추진축이 또렷해지는 편입니다."
            summary = "남성 흐름과 양의 기운이 만나면 결단은 빨라지지만, 속도를 조절할수록 운이 더 안정됩니다."
            advice = "밀어붙이는 힘이 좋을수록 한 템포 쉬어 판단의 여백을 두세요."
            keyword = "추진력"
            caution = "성급함"
        }
    }

    if ($gender -eq "female" -and $polarity -eq "yin") {
        return @{
            profile = "여성의 흐름 안에서 이 수의 음의 기운이 더 선명해져 감정의 결을 읽고 균형을 맞추는 힘이 크게 살아납니다. 다만 너무 오래 배려만 하면 자기 뜻이 흐려질 수 있습니다."
            early = "어린 시절에는 주변 분위기를 빨리 읽고 맞추려는 감각이 또렷해질 수 있습니다."
            middle = "중년에는 사람과 상황의 결을 읽는 장점이 커지지만, 중요한 순간엔 내 기준을 먼저 세우는 편이 좋습니다."
            late = "후반으로 갈수록 관계와 안정의 무게를 세심하게 살피게 되므로, 감정을 안에만 두지 않는 연습이 도움이 됩니다."
            life = "이 흐름은 여성의 자리에서 음의 기운과 공명해 섬세함과 조율 능력이 더 크게 살아나는 편입니다."
            summary = "여성 흐름과 음의 기운이 만나면 감정의 결은 깊어지지만, 자기 기준을 분명히 할수록 운이 더 편안해집니다."
            advice = "배려가 깊을수록 내 뜻도 같은 무게로 밖에 꺼내보세요."
            keyword = "균형감"
            caution = "과잉 배려"
        }
    }

    if ($gender -eq "male") {
        return @{
            profile = "남성의 흐름 안에서 이 수의 음의 기운은 겉보다 안쪽에서 힘을 쓰는 방식으로 드러납니다. 겉으로는 차분해 보여도 내면의 감각이 예민할 수 있어, 말을 아끼다 속으로만 쌓지 않는 편이 중요합니다."
            early = "어린 시절에는 감정을 드러내기보다 혼자 정리하려는 습관이 먼저 자리 잡기 쉽습니다."
            middle = "중년에는 신중함이 강점이 되지만, 지나친 망설임으로 기회를 놓치지 않도록 기준을 분명히 두는 편이 좋습니다."
            late = "후반에는 조용히 버티는 힘이 커지므로, 혼자 견디기보다 적절히 도움을 나누는 태도가 중요합니다."
            life = "이 흐름은 남성의 자리에서 음의 기운이 조율되며 드러나 세심함과 신중함이 삶의 바탕으로 작용합니다."
            summary = "남성 흐름과 음의 기운이 만나면 속도는 느려져도 판단은 깊어질 수 있습니다."
            advice = "오래 참고 미루기보다 핵심 감정을 짧게라도 표현해보세요."
            keyword = "신중함"
            caution = "속앓이"
        }
    }

    return @{
        profile = "여성의 흐름 안에서 이 수의 양의 기운은 부드러운 추진력으로 드러납니다. 결정을 미루지 않는 힘이 장점이지만, 혼자 너무 빨리 결론내리지 않도록 리듬을 조절하는 것이 중요합니다."
        early = "어린 시절에는 스스로 정하고 움직이려는 마음이 또래보다 빠르게 올라올 수 있습니다."
        middle = "중년에는 실행력이 큰 장점이 되지만, 중요한 관계일수록 상대의 속도와도 균형을 맞추는 편이 좋습니다."
        late = "후반에는 내 의지로 정리하고 결정하는 힘이 커지므로, 성급한 단정보다 여유를 남기는 태도가 도움이 됩니다."
        life = "이 흐름은 여성의 자리에서 양의 기운이 조율되며 드러나 결단력과 부드러운 리더십이 함께 자랍니다."
        summary = "여성 흐름과 양의 기운이 만나면 실행력은 살아나고, 속도를 다듬을수록 결과가 편안해집니다."
        advice = "결심이 빠를수록 한 번 더 숨을 고르고 주변의 호흡도 함께 살펴보세요."
        keyword = "결단력"
        caution = "과속"
    }
}

function Add-UniqueKeyword {
    param(
        [object[]]$Items,
        [string]$NewItem
    )

    $set = New-Object System.Collections.Generic.List[string]
    foreach ($item in $Items) {
        $text = [string]$item
        if (-not [string]::IsNullOrWhiteSpace($text) -and -not $set.Contains($text)) {
            $set.Add($text)
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($NewItem) -and -not $set.Contains($NewItem)) {
        $set.Add($NewItem)
    }
    return @($set.ToArray())
}

function Join-Sentence {
    param(
        [string]$Text,
        [string]$Addon
    )

    if ([string]::IsNullOrWhiteSpace($Addon)) { return $Text }
    if ([string]::IsNullOrWhiteSpace($Text)) { return $Addon }
    return "$Text $Addon"
}

function Convert-Profile {
    param(
        $Profile,
        [string]$Gender
    )

    $flavor = Get-GenderFlavor -gender $Gender -destiny ([int]$Profile.destiny)
    return [pscustomobject][ordered]@{
        destiny = [int]$Profile.destiny
        title = [string]$Profile.title
        polarity = [string]$Profile.polarity
        coreKeywords = Add-UniqueKeyword -Items $Profile.coreKeywords -NewItem $flavor.keyword
        cautionKeywords = Add-UniqueKeyword -Items $Profile.cautionKeywords -NewItem $flavor.caution
        destinyText = Join-Sentence -Text ([string]$Profile.destinyText) -Addon $flavor.profile
        oneLineAdvice = Join-Sentence -Text ([string]$Profile.oneLineAdvice) -Addon $flavor.advice
    }
}

function Convert-Record {
    param(
        $Record,
        [string]$Gender
    )

    $flavor = Get-GenderFlavor -gender $Gender -destiny ([int]$Record.destiny)
    return [pscustomobject][ordered]@{
        code = [string]$Record.code
        destiny = [int]$Record.destiny
        early = [int]$Record.early
        middle = [int]$Record.middle
        late = [int]$Record.late
        destinyProfileKey = [int]$Record.destinyProfileKey
        lifeTitle = [string]$Record.lifeTitle
        earlyText = Join-Sentence -Text ([string]$Record.earlyText) -Addon $flavor.early
        middleText = Join-Sentence -Text ([string]$Record.middleText) -Addon $flavor.middle
        lateText = Join-Sentence -Text ([string]$Record.lateText) -Addon $flavor.late
        lifeText = Join-Sentence -Text ([string]$Record.lifeText) -Addon $flavor.life
        summaryText = Join-Sentence -Text ([string]$Record.summaryText) -Addon $flavor.summary
        keywords = Add-UniqueKeyword -Items $Record.keywords -NewItem $flavor.keyword
        cautionKeywords = Add-UniqueKeyword -Items $Record.cautionKeywords -NewItem $flavor.caution
        oneLineAdvice = Join-Sentence -Text ([string]$Record.oneLineAdvice) -Addon $flavor.advice
    }
}

$profiles = Get-Content -Encoding UTF8 -Raw -Path (Join-Path $assetsDir "destiny_profiles.json") | ConvertFrom-Json
foreach ($gender in @("male", "female")) {
    $profileOutput = foreach ($profile in $profiles) {
        Convert-Profile -Profile $profile -Gender $gender
    }
    $profilePath = Join-Path $assetsDir "destiny_profiles_${gender}.json"
    $profileOutput | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 -Path $profilePath
}

$chunkFiles = Get-ChildItem -Path $assetsDir -Filter "life_records_????_????.jsonl" | Sort-Object Name
foreach ($chunkFile in $chunkFiles) {
    $lines = Get-Content -Encoding UTF8 -Path $chunkFile.FullName
    foreach ($gender in @("male", "female")) {
        $outputLines = foreach ($line in $lines) {
            if ([string]::IsNullOrWhiteSpace($line)) { continue }
            $record = $line | ConvertFrom-Json
            (Convert-Record -Record $record -Gender $gender) | ConvertTo-Json -Compress -Depth 8
        }
        $outputName = $chunkFile.Name -replace "^life_records_", "life_records_${gender}_"
        $outputPath = Join-Path $assetsDir $outputName
        $outputLines | Set-Content -Encoding UTF8 -Path $outputPath
    }
}

Write-Output "Gender-specific datasets generated in $assetsDir"

