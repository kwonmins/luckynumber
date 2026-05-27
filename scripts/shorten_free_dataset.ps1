param(
    [string[]]$OnlyFiles = @()
)

$ErrorActionPreference = "Stop"

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$utf8 = New-Object System.Text.UTF8Encoding($false)
$scriptRoot = if ($PSScriptRoot) { $PSScriptRoot } else { Join-Path (Get-Location) "scripts" }
$assetRoot = Join-Path $scriptRoot "..\app\src\main\assets"

$profileFiles = @(
    @{ Name = "destiny_profiles.json"; Variant = "default" },
    @{ Name = "destiny_profiles_male.json"; Variant = "male" },
    @{ Name = "destiny_profiles_female.json"; Variant = "female" }
)

$recordFiles = Get-ChildItem -Path $assetRoot -Filter "life_records*.jsonl" | Sort-Object Name
if ($OnlyFiles.Count -gt 0) {
    $wanted = @{}
    foreach ($name in $OnlyFiles) { $wanted[$name] = $true }
    $recordFiles = @($recordFiles | Where-Object { $wanted.ContainsKey($_.Name) })
}

$specs = @{
    0 = [ordered]@{
        title = "가능성"
        polarity = "중성"
        core = @("가능성", "반전", "유연함", "전환력")
        caution = @("방향 상실", "무기력", "우유부단", "극단성")
        profile = "무엇이든 될 수 있는 수입니다. 한마디로 모 아니면 도에 가까워 환경에 따라 폭이 크게 달라집니다. 가능성을 빠르게 읽고 새 판을 만드는 감각이 강하지만, 한번 흔들리면 무기력해지기 쉬워 방황하기 쉽습니다. 생활에서 지킬 기준 하나를 먼저 정해 두세요."
        advice = "가능성을 읽는 힘은 살리되, 방향을 오래 미루는 습관은 줄여보세요."
        early = "초반에는 환경이 자주 바뀌어 작은 선택도 큰 차이를 만들 수 있습니다. 예상 밖의 기회를 읽는 감각이 좋지만, 기준 없이 움직이면 마음이 쉽게 가라앉을 수 있어요. 생활에서 지킬 약속 하나를 먼저 세우는 편이 좋습니다."
        middle = "중반에는 여러 선택지가 동시에 열리며 판단이 흔들리기 쉽습니다. 새 가능성을 보는 눈은 장점이지만, 결정이 늦어지면 중요한 기회를 놓칠 수 있어요. 일과 관계에서 우선순위를 먼저 정하면 생활이 안정됩니다."
        late = "후반에는 삶에서 무엇을 이어갈지 스스로 정하는 힘이 중요합니다. 새로운 가능성은 여전히 남아 있지만, 방향을 자주 바꾸면 마음이 쉽게 지칠 수 있어요. 오래 붙잡을 일과 내려놓을 일을 차분히 나누세요."
    }
    1 = [ordered]@{
        title = "개척"
        polarity = "양"
        core = @("시작", "독립", "주도성", "실행력")
        caution = @("조급함", "고립", "완벽주의", "독선")
        profile = "스스로 길을 여는 힘이 강한 수입니다. 남이 만든 판을 따르기보다 내 결단으로 일을 시작하는 편입니다. 추진력은 좋지만 조급해지면 혼자 버티려는 마음이 커질 수 있어요. 무엇을 향해 움직일지 먼저 또렷하게 정하세요."
        advice = "개척력은 살리되, 혼자 서두르는 습관은 줄여보세요."
        early = "초반부터 스스로 선택해야 할 일이 자주 생깁니다. 먼저 나서는 힘은 좋지만, 너무 빨리 결론을 내리면 주변과 거리가 생길 수 있어요. 목표를 작게 나누고 도움을 구하는 연습이 필요합니다."
        middle = "중반에는 결정 속도가 빨라지고 책임도 함께 커집니다. 앞에서 이끄는 힘은 강하지만, 모든 일을 혼자 처리하려 하면 부담이 커져요. 중요한 일일수록 함께 맞출 사람과 기준을 먼저 정하세요."
        late = "후반에는 내가 열어 둔 일들을 안정적으로 마무리하는 힘이 중요합니다. 주도성은 여전히 강하지만, 고집이 세지면 관계가 뻣뻣해질 수 있어요. 결과보다 함께 가는 방식을 더 살피면 좋습니다."
    }
    2 = [ordered]@{
        title = "조화"
        polarity = "음"
        core = @("관계", "조화", "민감함", "인내")
        caution = @("속앓이", "눈치 과다", "우유부단", "정서 소진")
        profile = "사람과 감정을 섬세하게 읽는 수입니다. 직접 밀어붙이기보다 관계의 온도를 맞추며 분위기를 부드럽게 만듭니다. 배려와 조율 감각은 강하지만, 마음을 오래 삼키면 속앓이가 깊어질 수 있어요. 참기만 하지 말고 내 생각도 짧게 꺼내 보세요."
        advice = "조율 감각은 살리되, 속마음을 오래 묻어두는 습관은 줄여보세요."
        early = "초반에는 주변 분위기와 기대에 먼저 반응하기 쉽습니다. 사람을 편하게 만드는 장점이 있지만, 내 마음을 뒤로 미루면 피로가 쌓일 수 있어요. 작은 선택부터 내 의견을 말하는 연습이 도움이 됩니다."
        middle = "중반에는 사람 사이에서 중심을 맞추는 역할이 커집니다. 관계를 살피는 눈은 좋지만, 모두를 만족시키려 하면 내 생활이 흔들릴 수 있어요. 도와줄 일과 거절할 일을 분명히 나누는 편이 좋습니다."
        late = "후반에는 감정 소모를 줄이고 편안한 관계를 남기는 일이 중요합니다. 따뜻한 인연은 오래 이어지지만, 눈치를 많이 보면 마음이 쉽게 지칩니다. 가까운 사람에게도 내 한계를 부드럽게 알려주세요."
    }
    3 = [ordered]@{
        title = "표현"
        polarity = "양"
        core = @("표현", "언어", "해석", "기획")
        caution = @("말의 과속", "산만함", "감정 과열", "비꼼")
        profile = "말과 감각으로 분위기를 바꾸는 수입니다. 생각을 밖으로 꺼낼수록 사람들 앞에서 나의 생각과 개성이 더 또렷하게 드러납니다. 표현력은 좋지만 말이 빨라지면 오해도 함께 생길 수 있어요. 하고 싶은 말은 살리되 속도만 조금 늦추세요."
        advice = "표현력은 살리되, 말이 앞서가는 습관은 줄여보세요."
        early = "초반에는 말과 표현이 기회를 만들기 쉽습니다. 생각을 꺼내는 힘은 장점이지만, 기분에 따라 말이 앞서면 관계가 흔들릴 수 있어요. 중요한 말은 한 번 정리한 뒤 전하는 편이 좋습니다."
        middle = "중반에는 말의 속도와 감정선이 함께 빨라질 수 있습니다. 기획력과 해석력은 강하지만, 집중이 흩어지면 성과가 늦어져요. 일과 관계에서 먼저 끝낼 것을 정해 두면 좋습니다."
        late = "후반에는 하고 싶은 말을 정돈된 방식으로 전하는 힘이 중요합니다. 경험을 이야기로 풀어내는 능력은 좋지만, 감정이 섞이면 말이 날카로워질 수 있어요. 표현보다 전달될 마음을 먼저 살피세요."
    }
    4 = [ordered]@{
        title = "기반"
        polarity = "음"
        core = @("질서", "기준", "구조", "안정감")
        caution = @("고집", "경직성", "강박", "융통성 부족")
        profile = "무엇이든 기반을 단단히 다지는 수입니다. 급하게 넓히기보다 기준과 구조를 세우며 오래 가는 힘을 만듭니다. 안정감은 강하지만 기준이 지나치면 고집으로 보일 수 있어요. 원칙은 지키되 나 자신이 숨 쉴 틈도 남겨 두세요."
        advice = "기반을 다지는 힘은 살리되, 지나친 고집은 줄여보세요."
        early = "초반에는 기초를 다지는 태도가 결과 차이를 만듭니다. 성실하게 쌓는 힘은 좋지만, 작은 실수까지 붙잡으면 마음이 뻣뻣해질 수 있어요. 지킬 규칙과 가볍게 넘길 일을 구분해 보세요."
        middle = "중반에는 기준과 원칙이 더욱 또렷해집니다. 생활을 안정시키는 힘은 강하지만, 다른 방식까지 틀렸다고 보면 갈등이 생길 수 있어요. 일의 기준은 세우되 사람에게는 여유를 남겨 두세요."
        late = "후반에는 오래 지켜 온 기준을 유연하게 다루는 지혜가 중요합니다. 쌓아 온 기반은 힘이 되지만, 변화를 거부하면 답답함이 커질 수 있어요. 지킬 원칙과 바꿔도 되는 방식을 나누세요."
    }
    5 = [ordered]@{
        title = "변화"
        polarity = "양"
        core = @("변화", "활동성", "대담함", "기회")
        caution = @("과신", "무리수", "산만한 투자", "지속력 부족")
        profile = "변화의 기운이 강한 수입니다. 새로운 기회와 움직임 속에서 힘이 살아나고, 활동 반경을 넓히는 감각도 빠른 편입니다. 과감함은 장점이지만 일이 커질수록 무리수를 둘 수 있습니다. 기회는 잡되 그 일을 오래 감당할 수 있는지 꼭 확인하세요."
        advice = "변화 대응력은 살리되, 무리하게 벌이는 습관은 줄여보세요."
        early = "초반에는 새 기회가 빨리 들어오고 움직임도 많아집니다. 대담하게 시도하는 힘은 좋지만, 너무 많은 일을 동시에 잡으면 집중이 흐려질 수 있어요. 먼저 끝낼 일 하나를 정해 두는 편이 좋습니다."
        middle = "중반에는 활동 반경이 넓어지고 선택도 많아집니다. 기회를 잡는 눈은 강하지만, 검토 없이 뛰어들면 손해가 생길 수 있어요. 사람과 돈이 걸린 일은 한 번 더 확인하세요."
        late = "후반에는 벌여 둔 일과 관계를 잘 정돈해야 성과가 오래 남습니다. 새로운 시도는 여전히 힘이 되지만, 계속 넓히기만 하면 피로가 커져요. 이어갈 일과 멈출 일을 현실적으로 나누세요."
    }
    6 = [ordered]@{
        title = "책임"
        polarity = "음"
        core = @("책임", "성실", "생활력", "관리")
        caution = @("걱정 과다", "자기희생", "느린 결단", "건강 주의")
        profile = "책임감이 강한 수입니다. 맡은 것을 끝까지 끌고 가는 힘이 좋지만, 걱정이 길어지면 몸과 마음이 함께 무거워질 수 있습니다. 남들보다 피로감이 쉽게 쌓이며 특히 건강을 제1순위로 챙겨야 하는 수입니다. 책임은 나누고 컨디션은 미리 챙기세요."
        advice = "책임감은 살리되, 걱정과 피로는 오래 끌지 마세요."
        early = "초반에는 책임질 일이 늘며 생활 리듬이 무거워질 수 있습니다. 성실함은 큰 장점이지만, 남의 몫까지 떠안으면 금방 지칠 수 있어요. 몸의 신호와 쉬는 시간을 먼저 챙기는 습관이 필요합니다."
        middle = "중반에는 사람과 일 모두를 챙기느라 에너지가 크게 쓰입니다. 관리 능력은 좋지만, 걱정이 많아지면 결정이 늦어질 수 있어요. 맡을 일과 나눌 일을 분명히 정하세요."
        late = "후반에는 무리하지 않고 몸과 마음의 균형을 지키는 것이 중요합니다. 오래 쌓은 성실함은 든든하지만, 피로를 참고 넘기면 건강 관리가 흔들릴 수 있어요. 생활 리듬을 가장 먼저 안정시키세요."
    }
    7 = [ordered]@{
        title = "집중"
        polarity = "양"
        core = @("집중", "몰입", "추진", "규율")
        caution = @("집착", "번아웃", "예민함", "경직")
        profile = "한곳에 깊이 파고드는 수입니다. 가볍게 넓히기보다 한 가지 일에서 결과를 내는 힘이 강하게 드러납니다. 집중력은 좋지만 몰입이 지나치면 예민함과 번아웃이 따라올 수 있어요. 몰입은 살리고 몸과 마음의 긴장은 조금 풀어 주세요."
        advice = "집중력은 살리되, 지나친 집착은 줄여보세요."
        early = "초반에는 한 가지에 깊이 몰입하는 힘이 강해집니다. 집중력은 장점이지만, 시야가 좁아지면 주변의 조언을 놓칠 수 있어요. 목표를 잡되 쉬는 시간도 계획에 넣어두세요."
        middle = "중반에는 성과 욕심과 예민함이 함께 올라오기 쉽습니다. 밀고 가는 힘은 좋지만, 몸과 마음이 긴장하면 판단이 날카로워질 수 있어요. 중요한 일일수록 중간 점검을 자주 하는 편이 좋습니다."
        late = "후반에는 힘을 덜어내고 오래 갈 리듬을 만드는 일이 중요합니다. 몰입의 깊이는 여전히 강하지만, 쉬지 않고 버티면 소진이 빨라질 수 있어요. 오래 할 일과 잠시 내려놓을 일을 나누세요."
    }
    8 = [ordered]@{
        title = "인연"
        polarity = "음"
        core = @("인맥", "기회", "매력", "활동성")
        caution = @("욕심", "관계 소모", "경계선 약화", "평판 의존")
        profile = "사람과 기회를 끌어오는 수입니다. 활동 반경이 넓어지고, 주변의 도움이나 제안 속에서 가능성이 커지는 편입니다. 대인 감각은 좋지만 욕심이 앞서면 사람에게 쉽게 지칠 수 있어요. 사람과 기회를 넓히는 힘은 살리되, 경계선은 분명하게 세우세요."
        advice = "사람을 끌어오는 힘은 살리되, 욕심과 관계 소모는 줄여보세요."
        early = "초반에는 사람과 기회가 함께 몰려와 활동 범위가 넓어지기 쉽습니다. 매력과 친화력은 장점이지만, 모두에게 맞추면 내 에너지가 빨리 닳아요. 가까이 둘 사람과 거리를 둘 사람을 구분해 보세요."
        middle = "중반에는 관계와 일이 동시에 커져 선택이 더 중요해집니다. 사람을 통해 기회가 열리지만, 평판에만 기대면 중심이 흔들릴 수 있어요. 내 기준에 맞는 제안인지 먼저 확인하세요."
        late = "후반에는 넓어진 인간관계와 일들을 정돈하고, 내 사람을 가리는 눈이 중요합니다. 좋은 인연은 힘이 되지만, 관계가 많을수록 피로도 커질 수 있어요. 진심으로 남길 사람을 천천히 가려보세요."
    }
    9 = [ordered]@{
        title = "완성"
        polarity = "양"
        core = @("완성", "마무리", "의미", "정돈")
        caution = @("과열", "소진", "완벽 강박", "종결 공포")
        profile = "결과를 완성하고 마무리하는 힘이 강한 수입니다. 일을 끝까지 끌고 가며 의미를 남기는 데 재능이 있습니다. 완성력은 좋지만 열이 너무 높아지면 주위 사람들이 피곤해지기 쉽습니다. 끝까지 가는 힘은 좋지만 속도를 너무 올리지는 마세요."
        advice = "완성력은 살리되, 과열된 태도는 줄여보세요."
        early = "초반에는 끝맺음과 정돈 감각이 남들보다 빨리 드러납니다. 결과를 보려는 힘은 좋지만, 완벽을 서두르면 과정이 버거워질 수 있어요. 작은 성취를 인정하며 차근차근 마무리하세요."
        middle = "중반에는 성과를 완성하려는 압박이 커질 수 있습니다. 책임감과 마무리 능력은 강하지만, 기대치가 너무 높으면 주변도 지치기 쉬워요. 결과만 보지 말고 함께 가는 사람들의 속도도 살피세요."
        late = "후반에는 삶에서 이어갈 의미와 내려놓을 부담을 분명히 나누는 힘이 중요합니다. 마무리 능력은 빛나지만, 모든 것을 완벽하게 끝내려 하면 소진될 수 있어요. 의미 있는 일부터 차분히 정돈하세요."
    }
}

function Get-VariantSpec {
    param(
        [int]$Number,
        [string]$Variant
    )

    $spec = [ordered]@{}
    foreach ($entry in $specs[$Number].GetEnumerator()) {
        if ($entry.Value -is [System.Array]) {
            $spec[$entry.Key] = @($entry.Value)
        } else {
            $spec[$entry.Key] = $entry.Value
        }
    }

    if ($Variant -eq "male") {
        if ($Number -eq 0) {
            $spec.core += "재정비"
            $spec.caution += "답답함"
            $spec.advice += " 혼자 오래 짊어지지 않는 것도 중요합니다."
        } elseif ($Number % 2 -eq 1) {
            $spec.core += "추진력"
            $spec.caution += "성급함"
        } else {
            $spec.core += "신중함"
            $spec.caution += "속앓이"
        }
    } elseif ($Variant -eq "female") {
        if ($Number -eq 0) {
            $spec.core += "직관"
            $spec.caution += "망설임"
            $spec.advice += " 주변의 결을 읽되 내 뜻을 너무 늦추지 마세요."
        } elseif ($Number % 2 -eq 0) {
            $spec.core += "균형감"
            $spec.caution += "과잉 배려"
        } else {
            $spec.core += "결단력"
            $spec.caution += "과속"
        }
    }

    return $spec
}

function Compress-Spaces {
    param([string]$Text)
    return [regex]::Replace($Text.Trim(), "\s+", " ")
}

function Take-Unique {
    param(
        [object[]]$Values,
        [int]$Count
    )

    $result = New-Object System.Collections.Generic.List[string]
    foreach ($value in $Values) {
        foreach ($item in @($value)) {
            $text = "$item".Trim()
            if ($text.Length -eq 0) { continue }
            if (-not $result.Contains($text)) {
                $result.Add($text)
            }
            if ($result.Count -ge $Count) { return @($result) }
        }
    }
    return @($result)
}

function Join-Unique {
    param(
        [object[]]$Values,
        [int]$Count = 2
    )
    return ((Take-Unique -Values $Values -Count $Count) -join ", ")
}

function Build-ProfileObject {
    param(
        [int]$Number,
        [string]$Variant
    )

    $spec = Get-VariantSpec $Number $Variant
    [ordered]@{
        destiny = $Number
        title = $spec.title
        polarity = $spec.polarity
        coreKeywords = @(Take-Unique -Values $spec.core -Count 6)
        cautionKeywords = @(Take-Unique -Values $spec.caution -Count 6)
        destinyText = Compress-Spaces $spec.profile
        oneLineAdvice = Compress-Spaces $spec.advice
    }
}

function Build-LifeTitle {
    param(
        [hashtable]$Destiny,
        [hashtable]$Early,
        [hashtable]$Middle,
        [hashtable]$Late
    )

    $stages = @(Take-Unique -Values @($Early.title, $Middle.title, $Late.title) -Count 3)
    $stageText = if ($stages.Count -eq 1 -and $stages[0] -eq $Destiny.title) {
        "$($Destiny.title) 중심 운명"
    } else {
        "$($Destiny.title) 바탕의 $($stages -join ", ") 운명"
    }
    return $stageText
}

function Build-LifeText {
    param(
        [hashtable]$Destiny,
        [hashtable]$Early,
        [hashtable]$Middle,
        [hashtable]$Late
    )

    $strengths = Join-Unique -Values @($Destiny.core, $Early.core, $Middle.core, $Late.core) -Count 3
    $cautions = Join-Unique -Values @($Destiny.caution, $Early.caution, $Middle.caution, $Late.caution) -Count 2
    if ($Early.title -eq $Middle.title -and $Middle.title -eq $Late.title) {
        return Compress-Spaces "기본 성향은 $($Destiny.title)에 가깝고, 전반적으로 $($Early.title)의 과제가 꾸준히 반복됩니다. 강점은 $($strengths)에서 드러나고, $($cautions)은 일찍 조절할수록 생활이 편안해집니다."
    }
    return Compress-Spaces "기본 성향은 $($Destiny.title)에 가깝습니다. 초반에는 $($Early.title), 중반에는 $($Middle.title), 후반에는 $($Late.title)이 중요한 과제로 나타납니다. 강점은 $($strengths)에서 드러나고, $($cautions)은 일찍 조절할수록 생활이 편안해집니다."
}

function Build-SummaryText {
    param(
        [hashtable]$Destiny,
        [hashtable]$Early,
        [hashtable]$Middle,
        [hashtable]$Late
    )

    $strengths = Join-Unique -Values @($Destiny.core, $Early.core, $Middle.core, $Late.core) -Count 2
    $cautions = Join-Unique -Values @($Destiny.caution, $Early.caution, $Middle.caution, $Late.caution) -Count 2
    if ($Early.title -eq $Middle.title -and $Middle.title -eq $Late.title) {
        return Compress-Spaces "$($Destiny.title)을 바탕으로 $($Early.title)의 과제가 길게 이어집니다. 강점은 $($strengths)이고, 주의할 부분은 $($cautions)입니다."
    }
    return Compress-Spaces "$($Destiny.title)을 바탕으로 $($Early.title), $($Middle.title), $($Late.title)의 과제가 이어집니다. 강점은 $($strengths)이고, 주의할 부분은 $($cautions)입니다."
}

function Build-LifeAdvice {
    param(
        [hashtable]$Destiny,
        [hashtable]$Early,
        [hashtable]$Middle,
        [hashtable]$Late
    )

    if (@($Destiny.caution + $Early.caution + $Middle.caution + $Late.caution) -contains "건강 주의") {
        return "강점은 살리되, 피로와 건강 신호는 가장 먼저 챙기세요."
    }
    $cautionValues = @(Take-Unique -Values @($Destiny.caution, $Early.caution, $Middle.caution, $Late.caution) -Count 1)
    $caution = $cautionValues[0]
    return "$($Destiny.title)의 장점은 살리되, $($caution)은 초기에 조절하는 편이 좋습니다."
}

function To-JsonLine {
    param([object]$Value)
    return ($Value | ConvertTo-Json -Compress -Depth 12)
}

$profileMaps = @{}
foreach ($profileFile in $profileFiles) {
    $profiles = @()
    for ($i = 0; $i -le 9; $i++) {
        $profiles += (Build-ProfileObject $i $profileFile.Variant)
    }
    $profileMaps[$profileFile.Variant] = @{}
    foreach ($profile in $profiles) {
        $profileMaps[$profileFile.Variant][[int]$profile.destiny] = $profile
    }
    $path = Join-Path $assetRoot $profileFile.Name
    [System.IO.File]::WriteAllText($path, ($profiles | ConvertTo-Json -Depth 12), $utf8)
}

foreach ($file in $recordFiles) {
    $variant = if ($file.Name -like "life_records_male_*") {
        "male"
    } elseif ($file.Name -like "life_records_female_*") {
        "female"
    } else {
        "default"
    }
    $lines = [System.IO.File]::ReadAllLines($file.FullName, $utf8)
    $out = New-Object System.Collections.Generic.List[string]

    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $record = $line | ConvertFrom-Json

        $destiny = [int]$record.destiny
        $early = [int]$record.early
        $middle = [int]$record.middle
        $late = [int]$record.late
        $destinySpec = Get-VariantSpec $destiny $variant
        $earlySpec = Get-VariantSpec $early $variant
        $middleSpec = Get-VariantSpec $middle $variant
        $lateSpec = Get-VariantSpec $late $variant

        $newRecord = [ordered]@{
            code = [string]$record.code
            destiny = $destiny
            early = $early
            middle = $middle
            late = $late
            destinyProfileKey = $destiny
            lifeTitle = Build-LifeTitle $destinySpec $earlySpec $middleSpec $lateSpec
            earlyText = Compress-Spaces $earlySpec.early
            middleText = Compress-Spaces $middleSpec.middle
            lateText = Compress-Spaces $lateSpec.late
            lifeText = Build-LifeText $destinySpec $earlySpec $middleSpec $lateSpec
            summaryText = Build-SummaryText $destinySpec $earlySpec $middleSpec $lateSpec
            keywords = @(Take-Unique -Values @($destinySpec.core, $earlySpec.core, $middleSpec.core, $lateSpec.core) -Count 6)
            cautionKeywords = @(Take-Unique -Values @($destinySpec.caution, $earlySpec.caution, $middleSpec.caution, $lateSpec.caution) -Count 6)
            oneLineAdvice = Build-LifeAdvice $destinySpec $earlySpec $middleSpec $lateSpec
        }

        $out.Add((To-JsonLine $newRecord))
    }

    [System.IO.File]::WriteAllLines($file.FullName, $out, $utf8)
}

Write-Output "Rebuilt free dataset templates in $assetRoot"
