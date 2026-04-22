$ErrorActionPreference = "Stop"

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$scriptRoot = if ($PSScriptRoot) { $PSScriptRoot } else { Join-Path (Get-Location) "scripts" }

$assetRoot = Join-Path $scriptRoot "..\\app\\src\\main\\assets"
$destinyPath = Join-Path $assetRoot "destiny_profiles.json"

$profiles = @{
    0 = @{
        Title = "여백과 가능성"
        CoreLabel = "여백과 가능성"
        Axis = "여러 가능성 사이에서도 중심을 다시 세우는 힘"
        CoreKeywords = @("여백", "가능성", "전환", "유연성")
        CautionKeywords = @("허무감", "방향 상실", "무기력", "극단성")
        DestinySentence2 = "한 가지 길만 고집하기보다 상황에 맞는 다른 선택지를 함께 열어 두는 편이라, 처음에는 속도가 느려 보여도 결정적인 순간에는 의외의 전환점을 만들어 내곤 합니다."
        RelationSentence = "사람과 관계에서는 상대를 한 모습으로 단정하기보다 분위기와 가능성을 함께 읽으려는 경향이 있습니다."
        WorkSentence = "일과 돈의 흐름에서는 정해진 답을 반복할 때보다 변화 속에서 새 방향을 찾을 때 힘이 더 잘 살아납니다."
        CautionSentence = "다만 기준을 오래 세우지 못하면 허무감이나 무기력으로 기운이 쉽게 가라앉을 수 있습니다."
        FocusSentence = "이 수의 핵심은 비어 있다는 불안이 아니라 아직 열려 있다는 가능성에 있습니다."
        AdviceSentence = "큰 결론을 서두르기보다 하루를 지탱하는 작은 루틴부터 세우면 중심이 훨씬 안정됩니다."
        OneLineAdvice = "결론을 서두르기보다 작은 루틴으로 중심을 먼저 세우세요."
        EarlyStrength = "한 가지로 단정하지 않고 여러 가능성을 남겨 두는 감각"
        MiddleStrength = "익숙한 틀 밖의 선택지도 함께 살피는 유연함"
        LateStrength = "비워 낼 것과 다시 시작할 것을 차분히 고르는 여유"
        CautionState = "방향을 미루며 기운이 꺼지는 모습"
        ActionHint = "결정을 미루기보다 오늘 할 수 있는 작은 선택 하나를 끝내는 것"
    }
    1 = @{
        Title = "시작과 독립"
        CoreLabel = "시작과 독립"
        Axis = "먼저 결심하고 스스로 판을 여는 힘"
        CoreKeywords = @("시작", "독립", "주도성", "실행")
        CautionKeywords = @("조급함", "고립", "완벽주의", "독선")
        DestinySentence2 = "누가 길을 열어 주길 기다리기보다 스스로 출발선을 정하고 먼저 움직일 때 에너지가 가장 선명하게 살아나는 편입니다."
        RelationSentence = "사람과 관계에서는 조용해 보여도 자기 기준이 분명해서, 믿는 사람에게는 오래 책임을 지려는 태도가 드러납니다."
        WorkSentence = "일과 돈의 흐름에서는 처음 판을 열거나 초반 방향을 정해야 하는 자리에서 강점이 크게 드러납니다."
        CautionSentence = "다만 모든 일을 혼자 책임지려 들면 조급함과 고립이 겹치면서 스스로를 더 몰아붙일 수 있습니다."
        FocusSentence = "이 수의 핵심은 남보다 빨리 가는 데 있지 않고, 시작한 일을 내 흐름으로 정착시키는 데 있습니다."
        AdviceSentence = "먼저 움직이는 힘을 살리되, 목표와 진행 상황을 주변과 적절히 나눌수록 결과가 오래 갑니다."
        OneLineAdvice = "먼저 시작하되, 목표와 과정을 혼자만 품지 말고 주변과 나눠 보세요."
        EarlyStrength = "스스로 결정하고 직접 해보려는 독립심"
        MiddleStrength = "결정을 미루지 않고 판을 여는 추진력"
        LateStrength = "후반에도 자기 방식으로 삶을 재정리하려는 의지"
        CautionState = "조급하게 혼자 결론을 내리는 태도"
        ActionHint = "혼자서 다 책임지기보다 시작 단계부터 도움을 나눠 받는 것"
    }
    2 = @{
        Title = "관계와 조화"
        CoreLabel = "관계와 조화"
        Axis = "관계의 온도와 균형을 지키는 힘"
        CoreKeywords = @("관계", "조화", "민감함", "인내")
        CautionKeywords = @("속앓이", "눈치 과다", "우유부단", "정서 소진")
        DestinySentence2 = "말보다 표정과 분위기, 거리감을 먼저 읽는 편이라 사람 사이의 미묘한 흐름을 조율하는 감각이 좋습니다."
        RelationSentence = "사람과 관계에서는 배려와 인내가 강점이지만, 기대와 감정을 오래 안고 가면 스스로 지치기 쉬운 편입니다."
        WorkSentence = "일과 돈의 흐름도 결국 신뢰, 협업, 정서적 안정감에 크게 영향을 받는 경향이 있습니다."
        CautionSentence = "다만 참는 습관만으로 문제를 넘기려 하면 속앓이와 정서 소진이 쌓여 결정이 더 늦어질 수 있습니다."
        FocusSentence = "이 수의 핵심은 약함이 아니라 오래 버티며 균형을 만드는 힘에 있습니다."
        AdviceSentence = "감정을 눌러두기보다 기록하거나 대화로 꺼내는 배출구를 만들면 관계도 훨씬 편안해집니다."
        OneLineAdvice = "참기만 하지 말고, 느낀 점을 말이나 기록으로 풀어 주세요."
        EarlyStrength = "주변의 감정과 분위기를 빠르게 읽는 섬세함"
        MiddleStrength = "협업과 조율에서 강점을 보이는 관계 감각"
        LateStrength = "관계의 밀도와 정서적 편안함을 다시 살피는 태도"
        CautionState = "속마음을 삼킨 채 눈치를 보는 태도"
        ActionHint = "느낀 감정과 원하는 바를 말이나 글로 한 번 밖으로 꺼내는 것"
    }
    3 = @{
        Title = "표현과 해석"
        CoreLabel = "표현과 해석"
        Axis = "생각을 해석하고 전달해 흐름을 움직이는 힘"
        CoreKeywords = @("표현", "언어", "해석", "기획")
        CautionKeywords = @("말의 과속", "산만함", "감정 과열", "비꼼")
        DestinySentence2 = "생각과 감정을 말, 글, 아이디어로 풀어내는 힘이 강해서 설명하고 기획하며 흐름을 엮는 역할에서 재능이 살아납니다."
        RelationSentence = "사람과 관계에서는 분위기를 읽어 말로 풀어 주는 장점이 있지만, 감정이 앞서면 표현의 온도가 급격히 흔들릴 수 있습니다."
        WorkSentence = "일과 돈의 흐름에서는 설득, 기획, 해석, 콘텐츠, 교육처럼 언어 감각이 중요한 장면에서 힘이 크게 드러납니다."
        CautionSentence = "다만 말이 생각보다 빨리 나가면 집중력이 흩어지고, 좋은 의도도 가볍게 보일 수 있습니다."
        FocusSentence = "이 수의 핵심은 많이 말하는 데 있지 않고, 복잡한 것을 이해하기 쉬운 흐름으로 바꾸는 데 있습니다."
        AdviceSentence = "표현력이 강할수록 말의 속도를 한 번만 늦춰도 핵심이 훨씬 또렷해집니다."
        OneLineAdvice = "표현력이 강할수록 말의 속도를 한 번만 늦춰도 흐름이 훨씬 선명해집니다."
        EarlyStrength = "느낀 것을 말과 상상력으로 풀어내는 표현력"
        MiddleStrength = "기획과 설득, 설명에서 두드러지는 언어 감각"
        LateStrength = "삶의 경험을 이야기와 통찰로 정리하는 힘"
        CautionState = "말이 앞서며 집중이 흩어지는 모습"
        ActionHint = "바로 말하기보다 핵심 문장을 먼저 정리한 뒤 표현하는 것"
    }
    4 = @{
        Title = "질서와 기반"
        CoreLabel = "질서와 기반"
        Axis = "기준을 세우고 일을 구조화하는 힘"
        CoreKeywords = @("질서", "기준", "구조", "정리")
        CautionKeywords = @("고집", "경직성", "강박", "융통성 부족")
        DestinySentence2 = "흐름을 정리하고 기준을 세워 혼란을 안정시키는 힘이 좋아, 무너지기 쉬운 판도 차분하게 구조화하는 편입니다."
        RelationSentence = "사람과 관계에서는 신뢰할 수 있는 기준을 중요하게 여기며, 약속과 역할이 분명할수록 편안함을 느끼는 편입니다."
        WorkSentence = "일과 돈의 흐름에서는 시스템을 만들고 유지하는 힘이 커서, 꾸준함과 관리가 필요한 영역에서 존재감이 드러납니다."
        CautionSentence = "다만 기준이 지나치게 단단해지면 경직성과 답답함으로 이어져, 변화가 필요한 순간에도 몸이 늦게 움직일 수 있습니다."
        FocusSentence = "이 수의 핵심은 통제 자체가 아니라 흔들리는 상황에서도 중심을 세우는 데 있습니다."
        AdviceSentence = "기준은 지키되 방법은 조금 더 유연하게 바꾸는 연습을 할수록 강점이 더 오래 갑니다."
        OneLineAdvice = "기준은 지키되 방법은 조금 더 유연하게 바꾸는 연습이 필요합니다."
        EarlyStrength = "규칙과 기준을 빨리 세우려는 안정 지향"
        MiddleStrength = "현실을 구조화하고 실무를 정돈하는 힘"
        LateStrength = "삶의 우선순위를 정리하고 기반을 다시 세우는 힘"
        CautionState = "기준만 붙들다 경직되는 태도"
        ActionHint = "원칙은 지키되 방식 하나쯤은 바꿔 보는 유연함을 익히는 것"
    }
    5 = @{
        Title = "확장과 변화"
        CoreLabel = "확장과 변화"
        Axis = "변화를 기회로 넓히는 힘"
        CoreKeywords = @("확장", "변화", "대담함", "기회")
        CautionKeywords = @("과신", "무리수", "산만한 투자", "지속력 부족")
        DestinySentence2 = "새로운 판과 자극을 두려워하지 않아, 낯선 환경에서도 기회를 먼저 찾아 움직이는 대담함이 살아나는 편입니다."
        RelationSentence = "사람과 관계에서는 활력이 있고 분위기를 바꾸는 힘이 있지만, 재미와 속도만 좇으면 관계의 밀도가 얕아질 수 있습니다."
        WorkSentence = "일과 돈의 흐름에서는 변화와 확장이 필요한 순간에 유리하지만, 속도만 앞서면 선택이 넓어지는 만큼 리스크도 같이 커집니다."
        CautionSentence = "다만 가능성을 너무 크게만 보면 과신과 무리수로 이어져, 시작은 화려해도 유지가 흔들릴 수 있습니다."
        FocusSentence = "이 수의 핵심은 크게 벌이는 데 있지 않고, 넓힌 판을 실제 성과로 연결하는 데 있습니다."
        AdviceSentence = "새 기회가 보여도 속도보다 검증을 먼저 두면 확장의 힘이 훨씬 안정적으로 남습니다."
        OneLineAdvice = "기회가 커 보여도 속도보다 검증을 먼저 두는 편이 안전합니다."
        EarlyStrength = "새로운 자극에 빠르게 반응하는 호기심"
        MiddleStrength = "변화를 기회로 바꾸는 대담함"
        LateStrength = "익숙함에 머물지 않고 삶의 판을 넓히는 시도"
        CautionState = "가능성을 넓히다 무리수를 두는 모습"
        ActionHint = "새로운 기회가 와도 검증과 정리를 먼저 두는 것"
    }
    6 = @{
        Title = "책임과 성실"
        CoreLabel = "책임과 성실"
        Axis = "책임을 통해 신뢰와 결과를 쌓는 힘"
        CoreKeywords = @("책임", "성실", "축적", "관리")
        CautionKeywords = @("걱정 과다", "자기희생", "느린 결단", "부담 축적")
        DestinySentence2 = "맡은 일을 꾸준히 해내며 사람과 결과를 오래 지키는 힘이 강해서, 시간이 지날수록 신뢰가 자산이 되는 경우가 많습니다."
        RelationSentence = "사람과 관계에서는 책임감과 배려가 장점이지만, 필요 이상으로 떠안으면 오히려 지치고 서운함이 커질 수 있습니다."
        WorkSentence = "일과 돈의 흐름에서는 단기 성과보다 꾸준한 축적과 관리에서 힘이 크게 드러납니다."
        CautionSentence = "다만 걱정이 많아지고 책임을 과하게 짊어지면 결단이 늦어지고 몸과 마음의 피로가 쉽게 쌓일 수 있습니다."
        FocusSentence = "이 수의 핵심은 무조건 버티는 데 있지 않고, 오래 갈 구조를 성실하게 쌓아 올리는 데 있습니다."
        AdviceSentence = "도와주는 것과 대신 짊어지는 것을 구분할수록 책임감이 오히려 더 건강하게 작동합니다."
        OneLineAdvice = "도와주는 것과 대신 짊어지는 것을 구분해야 오래 버틸 수 있습니다."
        EarlyStrength = "맡은 일과 사람을 끝까지 챙기려는 책임감"
        MiddleStrength = "꾸준함으로 신뢰와 결과를 쌓는 힘"
        LateStrength = "정리와 돌봄을 통해 삶의 밀도를 높이는 태도"
        CautionState = "책임을 혼자 떠안아 지치는 태도"
        ActionHint = "책임의 범위를 분명히 하고 도움을 요청할 줄 아는 것"
    }
    7 = @{
        Title = "집중과 추진"
        CoreLabel = "집중과 추진"
        Axis = "하나에 몰입해 성과를 만드는 힘"
        CoreKeywords = @("집중", "몰입", "추진", "규율")
        CautionKeywords = @("집착", "번아웃", "예민함", "경직")
        DestinySentence2 = "목표가 분명해지는 순간 힘이 한 방향으로 강하게 모여, 끝까지 파고들며 결과를 만들어 내는 집요함이 큰 자산이 됩니다."
        RelationSentence = "사람과 관계에서는 표현이 적어 차가워 보일 수 있지만, 믿는 대상에게는 오래 충실한 편입니다."
        WorkSentence = "일과 돈의 흐름에서는 넓게 벌리기보다 한 지점을 깊게 파고들 때 훨씬 큰 결과가 나기 쉽습니다."
        CautionSentence = "다만 몰입이 지나치면 예민함과 번아웃이 겹쳐, 잘하던 일조차 갑자기 무겁게 느껴질 수 있습니다."
        FocusSentence = "이 수의 핵심은 속도가 아니라 지속 가능한 몰입에 있습니다."
        AdviceSentence = "성과를 높이는 루틴만큼 멈추고 회복하는 루틴도 일정 안에 넣어야 힘이 오래 갑니다."
        OneLineAdvice = "집중력만큼 회복 루틴도 일정에 넣어야 힘이 오래 갑니다."
        EarlyStrength = "좋아하는 것에 깊게 빠져드는 몰입력"
        MiddleStrength = "핵심 과제에 집중해 결과를 만들어 내는 추진력"
        LateStrength = "불필요한 것을 덜어내고 본질에 집중하는 힘"
        CautionState = "몰입이 지나쳐 예민해지는 모습"
        ActionHint = "성과만큼 휴식과 회복도 계획에 넣어 두는 것"
    }
    8 = @{
        Title = "연결과 확장"
        CoreLabel = "연결과 확장"
        Axis = "사람과 기회를 연결해 흐름을 키우는 힘"
        CoreKeywords = @("연결", "대인관계", "확장", "매력")
        CautionKeywords = @("욕심", "관계 소모", "외부평가 의존", "경계선 약화")
        DestinySentence2 = "사람과 자원, 정보와 기회를 이어 붙이는 감각이 좋아 혼자보다 함께 움직일 때 더 큰 흐름을 만들기 쉽습니다."
        RelationSentence = "사람과 관계에서는 존재감과 매력이 눈에 띄지만, 모두와 잘 지내려는 마음이 커질수록 오히려 중심이 흐려질 수 있습니다."
        WorkSentence = "일과 돈의 흐름도 사람을 통해 열리는 복이 있는 대신, 기준이 약하면 지출과 약속이 동시에 늘어나기 쉽습니다."
        CautionSentence = "다만 관계를 넓히는 속도가 너무 빠르면 경계선이 흐려지고, 외부 평가에 기대어 판단하는 습관이 생길 수 있습니다."
        FocusSentence = "이 수의 핵심은 단순한 사교성이 아니라 관계를 자산으로 바꾸는 연결력에 있습니다."
        AdviceSentence = "사람이 많이 붙을수록 모두를 품으려 하기보다 기준과 경계선을 먼저 세우는 편이 좋습니다."
        OneLineAdvice = "사람이 많이 붙을수록 관계보다 기준을 먼저 세우세요."
        EarlyStrength = "사람과 분위기를 통해 기회를 느끼는 감각"
        MiddleStrength = "네트워크와 협업을 넓히는 연결력"
        LateStrength = "오래 남길 관계와 자원을 선별하는 안목"
        CautionState = "관계를 넓히다 경계가 흐려지는 모습"
        ActionHint = "사람이 늘어날수록 기준과 선을 먼저 분명히 하는 것"
    }
    9 = @{
        Title = "완성과 정리"
        CoreLabel = "완성과 정리"
        Axis = "끝까지 다듬고 정리해 완성하는 힘"
        CoreKeywords = @("완성", "정리", "마감", "확장")
        CautionKeywords = @("과열", "소진", "완벽 강박", "종결 공포")
        DestinySentence2 = "시작보다 마무리의 책임을 더 크게 느끼며, 이미 만들어진 것을 더 높은 수준으로 끌어올리는 과정에서 능력이 선명해집니다."
        RelationSentence = "사람과 관계에서는 기준이 높고 책임감이 강해 신뢰를 주지만, 기대가 커질수록 스스로를 더 소모할 수 있습니다."
        WorkSentence = "일과 돈의 흐름에서는 결과를 정리하고 마감하며 완성도를 높여야 하는 장면에서 존재감이 특히 커집니다."
        CautionSentence = "다만 완벽을 향한 압박이 커지면 소진이 빨라지고, 끝내야 할 순간에도 스스로를 놓아주지 못할 수 있습니다."
        FocusSentence = "이 수의 핵심은 끝을 만드는 힘에 있으며, 제대로 정리할수록 더 큰 결과를 남길 수 있습니다."
        AdviceSentence = "완벽보다 완성을 먼저 선택해야 에너지가 남고, 그 힘으로 다음 단계도 차분히 준비할 수 있습니다."
        OneLineAdvice = "완벽보다 완성을 먼저 선택할 때 성과가 오래 남습니다."
        EarlyStrength = "끝까지 해내고 정리하고 싶은 책임감"
        MiddleStrength = "성과의 완성도를 끌어올리는 힘"
        LateStrength = "지나온 시간을 의미 있게 정리하는 통찰"
        CautionState = "완벽을 붙들다 스스로를 소모하는 태도"
        ActionHint = "완벽한 결과보다 마무리와 회복의 균형을 먼저 챙기는 것"
    }
}

$earlyIntros = @(
    "이 초년은 감정의 뿌리와 첫 습관이 자리 잡는 시기입니다.",
    "이 초년은 어린 시절의 정서와 자기 보호 방식이 굳어지는 시기입니다.",
    "이 초년은 세상을 받아들이는 첫 감각이 몸에 밴다는 점에서 중요합니다.",
    "이 초년은 작고 사소한 경험도 오래 남아 성격의 결이 되는 시기입니다."
)

$middleIntros = @(
    "이 중년은 관계와 일, 돈의 선택이 현실의 결과로 이어지는 시기입니다.",
    "이 중년은 쌓아 온 태도가 성과와 책임의 모습으로 드러나는 시기입니다.",
    "이 중년은 무엇을 넓히고 무엇을 지킬지 결정해야 하는 현실의 중심 구간입니다.",
    "이 중년은 삶의 방향이 막연함을 벗고 구체적인 구조를 갖추기 시작하는 시기입니다."
)

$lateIntros = @(
    "이 말년은 속도보다 의미와 정리가 중요해지는 시기입니다.",
    "이 말년은 무엇을 더 얻을지보다 무엇을 남길지 다시 살피게 되는 시기입니다.",
    "이 말년은 지난 선택을 정리하며 후반의 기준을 새로 세우는 시기입니다.",
    "이 말년은 관계와 일, 재물의 밀도를 다시 고르게 되는 시기입니다."
)

function Get-Profile {
    param([int]$number)
    return $profiles[$number]
}

function Pick-Variant {
    param(
        [string[]]$variants,
        [int]$seed
    )

    return $variants[$seed % $variants.Count]
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

function Build-DestinyText {
    param([int]$number)

    $profile = Get-Profile $number
    return @(
        "이 운명의 바탕에는 $(With-Particle $profile.CoreLabel '이' '가') 있습니다.",
        $profile.DestinySentence2,
        $profile.RelationSentence,
        $profile.WorkSentence,
        $profile.CautionSentence,
        $profile.FocusSentence,
        $profile.AdviceSentence
    ) -join " "
}

function Build-LifeTitle {
    param(
        [string]$code,
        [int]$destiny,
        [int]$early,
        [int]$middle,
        [int]$late
    )

    $variants = @(
        "운명수 {0}, 초년 {1}·중년 {2}·말년 {3}의 흐름",
        "운명수 {0}에 {1}-{2}-{3}의 시간이 이어지는 삶",
        "운명수 {0}, {1}/{2}/{3}이 겹쳐 만드는 인생 흐름",
        "운명수 {0} 위에 초년 {1}, 중년 {2}, 말년 {3}이 놓인 구조"
    )

    $seed = [int]$code
    return [string]::Format((Pick-Variant $variants $seed), $destiny, $early, $middle, $late)
}

function Build-StageText {
    param(
        [string]$stageName,
        [string]$code,
        [int]$destinyNumber,
        [int]$stageNumber
    )

    $seed = [int]$code
    $destiny = Get-Profile $destinyNumber
    $stage = Get-Profile $stageNumber

    switch ($stageName) {
        "early" {
            $intro = Pick-Variant $earlyIntros ($seed + $stageNumber)
            if ($destinyNumber -eq $stageNumber) {
                $strengthLine = "그래서 이 시기에는 $(With-Particle $stage.EarlyStrength '이' '가') 성격의 바탕으로 더 선명하게 자리 잡기 쉽습니다."
            } else {
                $strengthLine = "그래서 이 시기에는 운명수 ${destinyNumber}에 담긴 $($destiny.EarlyStrength) 위에, 초년수 ${stageNumber}의 $(With-Particle $stage.EarlyStrength '이' '가') 함께 자라기 쉽습니다."
            }
            return @(
                $intro,
                "기본 바탕에는 운명수 ${destinyNumber}의 $(With-Particle $destiny.CoreLabel '이' '가') 있고, 그 위에 초년수 ${stageNumber}의 $(With-Particle $stage.CoreLabel '이' '가') 더해집니다.",
                $strengthLine,
                "주변이 안정적이면 장점이 차분히 자리 잡지만, 반대로 흔들림이 크면 $($stage.CautionState) 같은 반응이 나오기 쉽습니다.",
                "초년의 과제는 타고난 기질을 억누르는 데 있지 않고, $($stage.ActionHint)입니다.",
                "이 감각이 자리 잡을수록 이후의 선택도 훨씬 안정된 바닥 위에서 움직입니다."
            ) -join " "
        }
        "middle" {
            $intro = Pick-Variant $middleIntros ($seed + $stageNumber)
            if ($destinyNumber -eq $stageNumber) {
                $strengthLine = "그래서 일, 관계, 재물 문제에서는 $(With-Particle $stage.MiddleStrength '이' '가') 더욱 두드러지게 작동합니다."
            } else {
                $strengthLine = "그래서 일, 관계, 재물 문제에서는 운명수 ${destinyNumber}의 $(With-Particle $destiny.MiddleStrength '과' '와') 중년수 ${stageNumber}의 $(With-Particle $stage.MiddleStrength '이' '가') 함께 작동합니다."
            }
            return @(
                $intro,
                "운명수 ${destinyNumber}의 $(With-Particle $destiny.CoreLabel '이' '가') 기본 축이 되고, 중년수 ${stageNumber}의 $(With-Particle $stage.CoreLabel '이' '가') 현실적인 판단 방식으로 드러납니다.",
                $strengthLine,
                "압박이 커질수록 운명수 ${destinyNumber}의 $(With-Particle $destiny.CautionState '과' '와') 중년수 ${stageNumber}의 $(With-Particle $stage.CautionState '이' '가') 겹치며 판단을 흐릴 수 있습니다.",
                "중년의 과제는 더 많이 벌이는 데 있지 않고, $($stage.ActionHint)입니다.",
                "기준이 선명할수록 이 시기의 성과는 반짝임보다 오래 가는 구조가 됩니다."
            ) -join " "
        }
        "late" {
            $intro = Pick-Variant $lateIntros ($seed + $stageNumber)
            if ($destinyNumber -eq $stageNumber) {
                $strengthLine = "그래서 후반으로 갈수록 $(With-Particle $stage.LateStrength '이' '가') 삶의 정리 방식에 더 또렷하게 드러납니다."
            } else {
                $strengthLine = "그래서 후반으로 갈수록 운명수 ${destinyNumber}의 $($destiny.LateStrength)에 더해, 말년수 ${stageNumber}의 $(With-Particle $stage.LateStrength '이' '가') 삶의 정리 방식에 큰 영향을 줍니다."
            }
            return @(
                $intro,
                "운명수 ${destinyNumber}의 $(With-Particle $destiny.CoreLabel '이' '가') 인생 전체의 바탕을 이루는 가운데, 말년수 ${stageNumber}의 $(With-Particle $stage.CoreLabel '이' '가') 후반의 기준을 더 또렷하게 만듭니다.",
                $strengthLine,
                "정리의 타이밍을 놓치면 $($stage.CautionState) 같은 흐름이 다시 올라와 마음을 무겁게 만들 수 있습니다.",
                "말년의 과제는 지나간 시간을 후회하는 데 있지 않고, $($stage.ActionHint)입니다.",
                "그럴수록 후반의 선택은 조급함보다 의미에 가까워지고, 관계와 일의 밀도도 훨씬 깊어집니다."
            ) -join " "
        }
        default {
            throw "Unknown stage name: $stageName"
        }
    }
}

function Build-LifeText {
    param(
        [int]$destinyNumber,
        [int]$earlyNumber,
        [int]$middleNumber,
        [int]$lateNumber
    )

    $destiny = Get-Profile $destinyNumber
    $early = Get-Profile $earlyNumber
    $middle = Get-Profile $middleNumber
    $late = Get-Profile $lateNumber
    $allSame = ($destinyNumber -eq $earlyNumber) -and ($destinyNumber -eq $middleNumber) -and ($destinyNumber -eq $lateNumber)

    if ($allSame) {
        $cautionLine = "흔들릴 때는 $($destiny.CautionState) 같은 반응이 감정, 현실, 후반의 정리 전반에 동시에 번지기 쉽습니다."
    } else {
        $cautionLine = "흔들릴 때는 운명수 ${destinyNumber}의 $(With-Particle $destiny.CautionState '이' '가') 중심을 흔들고, 초년에는 $($early.CautionState), 중년에는 $($middle.CautionState), 말년에는 $($late.CautionState) 같은 반응이 더 쉽게 올라올 수 있습니다."
    }

    return @(
        "이 흐름의 중심에는 운명수 ${destinyNumber}의 $(With-Particle $destiny.CoreLabel '이' '가') 있습니다.",
        "그래서 삶 전체에서는 $(With-Particle $destiny.Axis '이' '가') 중요한 축으로 작동합니다.",
        "초년수 ${earlyNumber}의 흐름은 $(With-Particle $early.EarlyStrength '을' '를') 더해 감정의 뿌리와 자기 보호 방식을 만들고, 어린 시절의 습관에 오래 남는 영향을 줍니다.",
        "중년수 ${middleNumber}의 흐름은 $(With-Particle $middle.MiddleStrength '을' '를') 통해 관계, 일, 돈을 현실적으로 다루는 태도를 드러내며, 삶의 방향을 구체적인 선택으로 연결합니다.",
        "말년수 ${lateNumber}의 흐름은 $(With-Particle $late.LateStrength '을' '를') 더해 무엇을 남기고 정리할지에 대한 후반의 기준을 세웁니다.",
        "관계에서는 초년의 감정 습관이 애착과 거리감의 방식에 남고, 중년의 선택은 협업과 신뢰의 질을 바꾸며, 말년의 기준은 끝까지 곁에 둘 사람을 가려 내게 합니다.",
        "일과 재물에서는 중년수의 영향이 가장 직접적으로 드러나지만, 초년의 불안과 말년의 정리 방식까지 함께 작동해 전체 흐름을 만듭니다.",
        $cautionLine,
        "이럴수록 문제를 한 번에 해결하려 하기보다 감정, 현실, 후반의 우선순위를 나눠 살피는 편이 훨씬 도움이 됩니다."
    ) -join " "
}

function Build-SummaryText {
    param(
        [int]$destinyNumber,
        [int]$earlyNumber,
        [int]$middleNumber,
        [int]$lateNumber
    )

    $destiny = Get-Profile $destinyNumber
    $early = Get-Profile $earlyNumber
    $middle = Get-Profile $middleNumber
    $late = Get-Profile $lateNumber

    return @(
        "운명수 ${destinyNumber}의 중심에는 $(With-Particle $destiny.CoreLabel '이' '가') 있고, 초년수 ${earlyNumber}의 $($early.CoreLabel), 중년수 ${middleNumber}의 $($middle.CoreLabel), 말년수 ${lateNumber}의 $(With-Particle $late.CoreLabel '이' '가') 차례로 삶의 결을 만듭니다.",
        "감정의 뿌리, 현실의 선택, 후반의 정리를 따로 살필수록 흐름은 훨씬 안정적으로 읽힙니다."
    ) -join " "
}

function Build-OneLineAdvice {
    param(
        [int]$destinyNumber,
        [int]$earlyNumber,
        [int]$middleNumber,
        [int]$lateNumber
    )

    $destiny = Get-Profile $destinyNumber
    $early = Get-Profile $earlyNumber
    $middle = Get-Profile $middleNumber
    $late = Get-Profile $lateNumber

    return "$($destiny.OneLineAdvice) 여기에 초년의 $(With-Particle $early.CoreLabel '을' '를'), 중년의 $(With-Particle $middle.CoreLabel '을' '를'), 말년의 $(With-Particle $late.CoreLabel '을' '를') 서로 다른 과제로 구분해 바라보면 흐름을 훨씬 차분하게 다룰 수 있습니다."
}

function Build-Keywords {
    param([int[]]$numbers)

    $items = New-Object System.Collections.Generic.List[string]
    foreach ($number in $numbers) {
        foreach ($keyword in (Get-Profile $number).CoreKeywords) {
            if (-not $items.Contains($keyword)) {
                $items.Add($keyword)
            }
        }
    }

    return @($items | Select-Object -First 6)
}

function Build-CautionKeywords {
    param([int[]]$numbers)

    $items = New-Object System.Collections.Generic.List[string]
    foreach ($number in $numbers) {
        foreach ($keyword in (Get-Profile $number).CautionKeywords) {
            if (-not $items.Contains($keyword)) {
                $items.Add($keyword)
            }
        }
    }

    return @($items | Select-Object -First 6)
}

$destinySource = Get-Content $destinyPath -Raw -Encoding UTF8 | ConvertFrom-Json
$refinedDestiny = foreach ($item in $destinySource) {
    $profile = Get-Profile ([int]$item.destiny)
    [pscustomobject][ordered]@{
        destiny = [int]$item.destiny
        title = $profile.Title
        polarity = [string]$item.polarity
        coreKeywords = $profile.CoreKeywords
        cautionKeywords = $profile.CautionKeywords
        destinyText = Build-DestinyText ([int]$item.destiny)
        oneLineAdvice = $profile.OneLineAdvice
    }
}

[System.IO.File]::WriteAllText(
    $destinyPath,
    ($refinedDestiny | ConvertTo-Json -Depth 6),
    $utf8NoBom
)

$lifeFiles = Get-ChildItem $assetRoot -Filter "life_records_*.jsonl" | Sort-Object Name
foreach ($file in $lifeFiles) {
    Write-Host "Refining $($file.Name)..."
    $lines = New-Object System.Collections.Generic.List[string]

    foreach ($line in Get-Content $file.FullName -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }

        $item = $line | ConvertFrom-Json
        $destinyNumber = [int]$item.destiny
        $earlyNumber = [int]$item.early
        $middleNumber = [int]$item.middle
        $lateNumber = [int]$item.late
        $numbers = @($destinyNumber, $earlyNumber, $middleNumber, $lateNumber)

        $refined = [pscustomobject][ordered]@{
            code = [string]$item.code
            destiny = $destinyNumber
            early = $earlyNumber
            middle = $middleNumber
            late = $lateNumber
            destinyProfileKey = [int]$item.destinyProfileKey
            lifeTitle = Build-LifeTitle ([string]$item.code) $destinyNumber $earlyNumber $middleNumber $lateNumber
            earlyText = Build-StageText "early" ([string]$item.code) $destinyNumber $earlyNumber
            middleText = Build-StageText "middle" ([string]$item.code) $destinyNumber $middleNumber
            lateText = Build-StageText "late" ([string]$item.code) $destinyNumber $lateNumber
            lifeText = Build-LifeText $destinyNumber $earlyNumber $middleNumber $lateNumber
            summaryText = Build-SummaryText $destinyNumber $earlyNumber $middleNumber $lateNumber
            keywords = Build-Keywords $numbers
            cautionKeywords = Build-CautionKeywords $numbers
            oneLineAdvice = Build-OneLineAdvice $destinyNumber $earlyNumber $middleNumber $lateNumber
        }

        $lines.Add(($refined | ConvertTo-Json -Compress -Depth 6))
    }

    [System.IO.File]::WriteAllLines($file.FullName, $lines, $utf8NoBom)
}

Write-Host "Dataset refinement complete."
