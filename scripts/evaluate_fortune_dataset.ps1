Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$datasetPath = Join-Path $projectRoot "app\src\main\assets\destiny_profiles.json"
$catalogPaths = @(
    (Join-Path $projectRoot "app\src\main\java\com\example\unum\data\content\NumerologyInteractionCatalog.kt"),
    (Join-Path $projectRoot "app\src\main\java\com\example\unum\data\content\DailyFortuneCatalog.kt")
)
$rulesPath = Join-Path $PSScriptRoot "fortune_dataset_quality_rules.json"
$profiles = Get-Content -Raw -Encoding UTF8 $datasetPath | ConvertFrom-Json
$rules = Get-Content -Raw -Encoding UTF8 $rulesPath | ConvertFrom-Json

$requiredFields = @(
    "summary",
    "strength",
    "caution",
    "actionGuide",
    "earlyScene",
    "middleScene",
    "lateScene",
    "relationshipScene",
    "workScene",
    "moneyScene"
)
$bannedPhrases = @($rules.bannedPhrases)
$sceneMarkers = @($rules.sceneMarkers)

function Normalize-Text([string]$text) {
    return (($text -replace "[^\p{L}0-9]", "").ToLowerInvariant())
}

function Get-Ngrams([string]$text, [int]$size = 4) {
    $normalized = Normalize-Text $text
    $set = [System.Collections.Generic.HashSet[string]]::new()
    if ($normalized.Length -lt $size) {
        [void]$set.Add($normalized)
        return $set
    }
    for ($index = 0; $index -le $normalized.Length - $size; $index++) {
        [void]$set.Add($normalized.Substring($index, $size))
    }
    return $set
}

function Get-Jaccard([string]$left, [string]$right) {
    $leftSet = Get-Ngrams $left
    $rightSet = Get-Ngrams $right
    $intersection = 0
    foreach ($item in $leftSet) {
        if ($rightSet.Contains($item)) { $intersection++ }
    }
    $union = $leftSet.Count + $rightSet.Count - $intersection
    if ($union -eq 0) { return 1.0 }
    return $intersection / $union
}

if ($profiles.Count -ne 10) {
    throw "Expected 10 destiny profiles, found $($profiles.Count)."
}

$texts = [System.Collections.Generic.List[object]]::new()
$profileTexts = [System.Collections.Generic.List[object]]::new()
$missingFields = [System.Collections.Generic.List[string]]::new()
foreach ($profile in $profiles) {
    foreach ($field in $requiredFields) {
        $value = [string]$profile.$field
        if ([string]::IsNullOrWhiteSpace($value)) {
            $missingFields.Add("$($profile.destiny).$field")
        } else {
            $entry = [pscustomobject]@{
                Id = "$($profile.destiny).$field"
                Text = $value
                Normalized = Normalize-Text $value
            }
            $texts.Add($entry)
            $profileTexts.Add($entry)
        }
    }
}

# 앱에 직접 노출되는 Kotlin 카탈로그 문장도 함께 검사한다. 짧은 제목과 내부 키는 제외한다.
foreach ($catalogPath in $catalogPaths) {
    $content = Get-Content -Raw -Encoding UTF8 $catalogPath
    $matches = [regex]::Matches($content, '"((?:\\.|[^"\\])*)"')
    $itemIndex = 0
    foreach ($match in $matches) {
        $value = $match.Groups[1].Value
        if ($value.Length -ge 20 -and $value -match '\p{IsHangulSyllables}') {
            $texts.Add([pscustomobject]@{
                Id = "$(Split-Path -Leaf $catalogPath).$itemIndex"
                Text = $value
                Normalized = Normalize-Text $value
            })
            $itemIndex++
        }
    }
}

$duplicateGroups = @(
    $texts |
        Group-Object Normalized |
        Where-Object Count -gt 1
)

$bannedHits = [System.Collections.Generic.List[string]]::new()
foreach ($item in $texts) {
    foreach ($phrase in $bannedPhrases) {
        if ($item.Text.Contains($phrase)) {
            $bannedHits.Add("$($item.Id): $phrase")
        }
    }
}

$specificCount = 0
foreach ($item in $profileTexts) {
    $hasMarker = $false
    foreach ($marker in $sceneMarkers) {
        if ($item.Text.Contains($marker)) {
            $hasMarker = $true
            break
        }
    }
    if ($hasMarker) { $specificCount++ }
}
$specificityRate = if ($profileTexts.Count -eq 0) { 0 } else { $specificCount / $profileTexts.Count }

$highSimilarityPairs = [System.Collections.Generic.List[string]]::new()
for ($leftIndex = 0; $leftIndex -lt $texts.Count; $leftIndex++) {
    for ($rightIndex = $leftIndex + 1; $rightIndex -lt $texts.Count; $rightIndex++) {
        $score = Get-Jaccard $texts[$leftIndex].Text $texts[$rightIndex].Text
        if ($score -ge 0.78) {
            $highSimilarityPairs.Add(
                "$($texts[$leftIndex].Id) <> $($texts[$rightIndex].Id): $([math]::Round($score, 3))"
            )
        }
    }
}

$metrics = [pscustomobject]@{
    profiles = $profiles.Count
    evaluatedTexts = $texts.Count
    profileTexts = $profileTexts.Count
    catalogTexts = $texts.Count - $profileTexts.Count
    exactDuplicateGroups = $duplicateGroups.Count
    highSimilarityPairs = $highSimilarityPairs.Count
    bannedPhraseHits = $bannedHits.Count
    specificityRate = [math]::Round($specificityRate, 3)
}
$metrics | Format-List

$failures = [System.Collections.Generic.List[string]]::new()
if ($missingFields.Count -gt 0) { $failures.Add("Missing fields: $($missingFields -join ', ')") }
if ($duplicateGroups.Count -gt 0) { $failures.Add("Exact duplicate groups: $($duplicateGroups.Count)") }
if ($highSimilarityPairs.Count -gt 0) { $failures.Add("High similarity pairs: $($highSimilarityPairs -join '; ')") }
if ($bannedHits.Count -gt 0) { $failures.Add("Banned phrases: $($bannedHits -join '; ')") }
if ($specificityRate -lt 0.85) { $failures.Add("Specificity rate below 0.85: $specificityRate") }

if ($failures.Count -gt 0) {
    throw ($failures -join [Environment]::NewLine)
}

Write-Output "Fortune dataset quality checks passed."
