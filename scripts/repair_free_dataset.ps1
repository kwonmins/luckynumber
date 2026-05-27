$ErrorActionPreference = "Stop"

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$scriptRoot = if ($PSScriptRoot) { $PSScriptRoot } else { Join-Path (Get-Location) "scripts" }
$assetRoot = Join-Path $scriptRoot "..\\app\\src\\main\\assets"

$profileFiles = @(
    @{ Path = Join-Path $assetRoot "destiny_profiles.json"; Key = "default" },
    @{ Path = Join-Path $assetRoot "destiny_profiles_male.json"; Key = "male" },
    @{ Path = Join-Path $assetRoot "destiny_profiles_female.json"; Key = "female" }
)

$recordFiles = Get-ChildItem -Path $assetRoot -Filter "life_records*.jsonl" | Sort-Object Name

$cautionStateMap = @{
    0 = "결정을 미루다 기운이 가라앉는 반응"
    1 = "혼자 서둘러 결론을 내리는 태도"
    2 = "속마음을 삼킨 채 눈치를 보는 태도"
    3 = "말이 앞서며 집중이 흩어지는 모습"
    4 = "기준만 붙들다 경직되는 태도"
    5 = "가능성만 좇다 무리수를 두는 모습"
    6 = "책임을 혼자 떠안아 지치고 건강 신호를 놓치는 태도"
    7 = "몰입이 지나쳐 예민해지는 모습"
    8 = "관계를 넓히다 경계가 흐려지는 모습"
    9 = "완벽을 붙들다 스스로를 소모하는 태도"
}

$phraseReplacements = @{
    "방향을 미루며 기운이 꺼지는 모습" = "결정을 미루다 기운이 가라앉는 반응"
    "조급하게 혼자 결론을 내리는 태도" = "혼자 서둘러 결론을 내리는 태도"
    "가능성을 넓히다 무리수를 두는 모습" = "가능성만 좇다 무리수를 두는 모습"
    "책임을 혼자 떠안아 지치는 태도" = "책임을 혼자 떠안아 지치고 건강 신호를 놓치는 태도"
}

function Get-Particle {
    param(
        [string]$text,
        [string]$batchimParticle,
        [string]$noBatchimParticle
    )

    if ([string]::IsNullOrWhiteSpace($text)) {
        return $noBatchimParticle
    }

    $lastChar = $text[$text.Length - 1]
    $code = [int][char]$lastChar
    if ($code -ge 0xAC00 -and $code -le 0xD7A3) {
        $hasBatchim = (($code - 0xAC00) % 28) -ne 0
        if ($hasBatchim) {
            return $batchimParticle
        }
    }

    return $noBatchimParticle
}

function With-Particle {
    param(
        [string]$text,
        [string]$batchimParticle,
        [string]$noBatchimParticle
    )

    return "$text$(Get-Particle $text $batchimParticle $noBatchimParticle)"
}

function Get-ProfileMap {
    param([object[]]$profiles)

    $map = @{}
    foreach ($profile in $profiles) {
        $map[[int]$profile.destiny] = $profile
    }
    return $map
}

function Build-StageStructureLine {
    param(
        [string]$earlyLabel,
        [string]$middleLabel,
        [string]$lateLabel
    )

    if (($earlyLabel -eq $middleLabel) -and ($middleLabel -eq $lateLabel)) {
        return "초년부터 말년까지 모두 $(With-Particle $earlyLabel '이' '가') 같은 결로 이어져, 한 번 익힌 과제가 오래 반복되는 구조입니다."
    }

    if ($earlyLabel -eq $middleLabel) {
        return "초년과 중년에는 $(With-Particle $earlyLabel '이' '가') 길게 이어지고, 말년에는 $(With-Particle $lateLabel '이' '가') 더해지며 흐름의 결이 바뀝니다."
    }

    if ($middleLabel -eq $lateLabel) {
        return "초년에는 $(With-Particle $earlyLabel '이' '가') 먼저 바탕을 만들고, 중년과 말년에는 $(With-Particle $middleLabel '이' '가') 길게 이어지며 후반을 이끕니다."
    }

    if ($earlyLabel -eq $lateLabel) {
        return "초년과 말년에는 $(With-Particle $earlyLabel '이' '가') 서로 닮은 결로 이어지고, 중년에는 $(With-Particle $middleLabel '이' '가') 중심을 바꾸는 역할을 합니다."
    }

    return "초년에는 $(With-Particle $earlyLabel '이' '가'), 중년에는 $(With-Particle $middleLabel '이' '가'), 말년에는 $(With-Particle $lateLabel '이' '가') 차례로 이어지며 삶의 결을 만듭니다."
}

function Build-StageAdviceLine {
    param(
        [string]$earlyLabel,
        [string]$middleLabel,
        [string]$lateLabel
    )

    if (($earlyLabel -eq $middleLabel) -and ($middleLabel -eq $lateLabel)) {
        return "같은 과제가 오래 반복되는 구조일수록 조급하게 바꾸기보다 한 가지 방식을 꾸준히 다듬는 편이 훨씬 효과적입니다."
    }

    if ($earlyLabel -eq $middleLabel) {
        return "초년과 중년의 과제는 같은 결로 이어지니 한 번 세운 기준을 오래 가져가고, 말년에는 새로운 정리 방식을 보태는 편이 좋습니다."
    }

    if ($middleLabel -eq $lateLabel) {
        return "중년과 말년의 과제가 닮아 있으니 후반으로 갈수록 같은 원칙을 더 단단히 다듬고, 초년의 방식은 경험으로만 남겨 두는 편이 좋습니다."
    }

    if ($earlyLabel -eq $lateLabel) {
        return "초년과 말년의 과제가 닮아 있어도 중년의 전환을 어떻게 지나느냐에 따라 흐름의 밀도가 달라지니, 가운데 시기의 선택을 가볍게 보지 않는 편이 좋습니다."
    }

    return "각 시기의 과제를 따로 구분해 바라볼수록 흐름을 더 차분하고 선명하게 다룰 수 있습니다."
}

function Build-StageCautionLine {
    param(
        [string]$destinyState,
        [string]$earlyState,
        [string]$middleState,
        [string]$lateState
    )

    if (($earlyState -eq $middleState) -and ($middleState -eq $lateState)) {
        return "흔들릴 때는 $(With-Particle $destinyState '이' '가') 한 시기에만 그치지 않고 삶 전반으로 번지기 쉬우니, 한 번에 모든 것을 감당하려 하지 않는 편이 좋습니다."
    }

    if ($earlyState -eq $middleState) {
        return "흔들릴 때는 초년과 중년에서 $(With-Particle $earlyState '이' '가') 길게 이어지고, 말년에는 $lateState 쪽으로 무게가 옮겨가기 쉬워 시기별로 돌보는 방식이 필요합니다."
    }

    if ($middleState -eq $lateState) {
        return "흔들릴 때는 초년의 반응보다 중년과 말년에서 $(With-Particle $middleState '이' '가') 오래 이어지기 쉬우니, 후반으로 갈수록 기준을 더 또렷하게 세우는 편이 좋습니다."
    }

    if ($earlyState -eq $lateState) {
        return "흔들릴 때는 초년과 말년에서 $(With-Particle $earlyState '이' '가') 다시 살아나고, 중년에는 $middleState 쪽으로 결이 달라질 수 있어 중심을 자주 점검하는 편이 좋습니다."
    }

    return "흔들릴 때는 초년에는 $earlyState, 중년에는 $middleState, 말년에는 $lateState 쪽으로 반응이 달라지기 쉬우니, 같은 문제라도 시기마다 다르게 다루는 감각이 필요합니다."
}

function Replace-Phrases {
    param([string]$text)

    $updated = $text
    foreach ($entry in $phraseReplacements.GetEnumerator()) {
        $updated = $updated.Replace($entry.Key, $entry.Value)
    }
    return $updated
}

function Replace-RegexSegment {
    param(
        [string]$text,
        [string]$pattern,
        [string]$replacement
    )

    $match = [regex]::Match($text, $pattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if (-not $match.Success) {
        return $text
    }

    return $text.Substring(0, $match.Index) + $replacement + $text.Substring($match.Index + $match.Length)
}

function Update-Profile {
    param(
        [pscustomobject]$profile,
        [string]$variantKey
    )

    if ([int]$profile.destiny -ne 6) {
        return $profile
    }

    $profile.destinyText = $profile.destinyText.Replace(
        "다만 걱정이 많아지고 책임을 과하게 짊어지면 결단이 늦어지고 몸과 마음의 피로가 쉽게 쌓일 수 있습니다.",
        "다만 걱정이 많아지고 책임을 과하게 짊어지면 컨디션이 떨어지거나 작은 사고가 겹치기 쉬우니, 몸과 마음의 신호를 가볍게 넘기지 않는 편이 좋습니다."
    )
    $profile.destinyText = $profile.destinyText.Replace(
        "도와주는 것과 대신 짊어지는 것을 구분할수록 책임감이 오히려 더 건강하게 작동합니다.",
        "도와주는 것과 대신 짊어지는 것을 구분할수록 책임감이 더 건강하게 작동하고, 컨디션 관리도 안정적으로 이어집니다."
    )

    switch ($variantKey) {
        "male" {
            $profile.cautionKeywords = @("걱정 과다", "자기희생", "느린 결단", "건강 주의", "속앓이")
            $profile.oneLineAdvice = "도와주는 것과 대신 짊어지는 것을 구분하고, 피로와 건강 신호를 미루지 마세요. 오래 참고 미루기보다 핵심 감정을 짧게라도 표현해보세요."
        }
        "female" {
            $profile.cautionKeywords = @("걱정 과다", "자기희생", "느린 결단", "건강 주의", "과잉 배려")
            $profile.oneLineAdvice = "도와주는 것과 대신 짊어지는 것을 구분하고, 피로와 건강 신호를 미루지 마세요. 배려가 깊을수록 내 뜻도 같은 무게로 밖에 꺼내보세요."
        }
        default {
            $profile.cautionKeywords = @("걱정 과다", "자기희생", "느린 결단", "부담 축적", "건강 주의")
            $profile.oneLineAdvice = "도와주는 것과 대신 짊어지는 것을 구분하고, 피로와 건강 신호를 미루지 마세요."
        }
    }

    return $profile
}

function Build-CombinedKeywords {
    param(
        [int[]]$numbers,
        [hashtable]$profileMap,
        [string]$propertyName
    )

    $items = New-Object System.Collections.Generic.List[string]
    foreach ($number in $numbers) {
        foreach ($keyword in $profileMap[$number].$propertyName) {
            if (-not $items.Contains($keyword)) {
                $items.Add($keyword)
            }
        }
    }

    return @($items | Select-Object -First 6)
}

function Update-Record {
    param(
        [pscustomobject]$record,
        [hashtable]$profileMap
    )

    $destinyProfile = $profileMap[[int]$record.destiny]
    $earlyProfile = $profileMap[[int]$record.early]
    $middleProfile = $profileMap[[int]$record.middle]
    $lateProfile = $profileMap[[int]$record.late]
    $destinyCaution = $cautionStateMap[[int]$record.destiny]
    $earlyCaution = $cautionStateMap[[int]$record.early]
    $middleCaution = $cautionStateMap[[int]$record.middle]
    $lateCaution = $cautionStateMap[[int]$record.late]

    $structureLine = Build-StageStructureLine $earlyProfile.title $middleProfile.title $lateProfile.title
    $cautionLine = Build-StageCautionLine $destinyCaution $earlyCaution $middleCaution $lateCaution
    $adviceTail = Build-StageAdviceLine $earlyProfile.title $middleProfile.title $lateProfile.title

    $record.earlyText = Replace-Phrases $record.earlyText
    $record.middleText = Replace-Phrases $record.middleText
    $record.lateText = Replace-Phrases $record.lateText
    $record.lifeText = Replace-Phrases $record.lifeText

    $record.earlyText = Replace-RegexSegment `
        $record.earlyText `
        '주변이 안정적이면 장점이 차분히 자리 잡지만, 반대로 흔들림이 크면 .*? 초년의 과제는' `
        "주변이 안정적이면 장점이 차분히 자리 잡지만, 반대로 흔들림이 크면 $(With-Particle $earlyCaution '이' '가') 더 쉽게 드러날 수 있습니다. 초년의 과제는"

    $middleCautionSentence = if ([int]$record.destiny -eq [int]$record.middle) {
        "압박이 커질수록 $middleCaution 쪽으로 기울며 판단이 흐려질 수 있습니다."
    } else {
        "압박이 커질수록 운명수 $($record.destiny)의 $(With-Particle $destinyCaution '과' '와') 중년수 $($record.middle)의 $(With-Particle $middleCaution '이' '가') 함께 올라오며 판단이 흐려질 수 있습니다."
    }
    $record.middleText = Replace-RegexSegment `
        $record.middleText `
        '압박이 커질수록 .*? 판단(?:을 흐릴 수|이 흐려질 수) 있습니다\.' `
        $middleCautionSentence

    $record.lateText = Replace-RegexSegment `
        $record.lateText `
        '정리의 타이밍을 놓치면 .*? 마음을 무겁게 만들 수 있습니다\.' `
        "정리의 타이밍을 놓치면 $(With-Particle $lateCaution '이' '가') 다시 올라와 마음을 무겁게 만들 수 있습니다."

    $record.lifeText = Replace-RegexSegment `
        $record.lifeText `
        '흔들릴 때는 .*?(?= 이럴수록)' `
        $cautionLine

    $record.summaryText = "운명수 $($record.destiny)의 중심에는 $($destinyProfile.title)이 있고, $structureLine 감정의 뿌리, 현실의 선택, 후반의 정리를 나누어 볼수록 흐름이 더 또렷해집니다."
    $record.oneLineAdvice = "$($destinyProfile.oneLineAdvice) $adviceTail"
    $record.keywords = Build-CombinedKeywords @([int]$record.destiny, [int]$record.early, [int]$record.middle, [int]$record.late) $profileMap "coreKeywords"
    $record.cautionKeywords = Build-CombinedKeywords @([int]$record.destiny, [int]$record.early, [int]$record.middle, [int]$record.late) $profileMap "cautionKeywords"

    return $record
}

$profileMaps = @{}
foreach ($profileFile in $profileFiles) {
    $profiles = Get-Content -Path $profileFile.Path -Raw -Encoding UTF8 | ConvertFrom-Json
    $updatedProfiles = foreach ($profile in $profiles) {
        Update-Profile $profile $profileFile.Key
    }

    $profileMaps[$profileFile.Key] = Get-ProfileMap $updatedProfiles
    [System.IO.File]::WriteAllText(
        (Resolve-Path $profileFile.Path),
        ($updatedProfiles | ConvertTo-Json -Depth 8),
        $utf8NoBom
    )
}

foreach ($recordFile in $recordFiles) {
    $variantKey = if ($recordFile.Name -like "*_male_*") {
        "male"
    } elseif ($recordFile.Name -like "*_female_*") {
        "female"
    } else {
        "default"
    }

    $profileMap = $profileMaps[$variantKey]
    $updatedLines = foreach ($line in Get-Content -Path $recordFile.FullName -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }

        $record = $line | ConvertFrom-Json
        $updated = Update-Record $record $profileMap
        $updated | ConvertTo-Json -Compress -Depth 8
    }

    [System.IO.File]::WriteAllLines($recordFile.FullName, $updatedLines, $utf8NoBom)
}

Write-Output "Free dataset repair complete in $assetRoot"
