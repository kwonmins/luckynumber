package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun DeveloperStoryScreen() {
    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(18.dp)) }
            item { SectionTitle("개발자 이야기") }
            item {
                MascotGuideCard(
                    message = "수리는 한 개발자의 막막한 밤에서 태어났어요. 숫자 속에 숨어 있던 작은 길잡이를 함께 들여다볼게요.",
                    imageRes = MascotArt.Story
                )
            }
            item {
                StoryCard(
                    title = "막막했던 공대생의 밤",
                    body = "처음 이 앱을 만든 개발자는 평범한 공대생이었습니다. 코드는 익숙했지만, 삶의 방향은 늘 어렵게 느껴졌습니다. 공부와 일, 관계와 미래가 한꺼번에 밀려오던 어느 날, 그는 답을 찾고 싶다는 마음 하나로 지방의 작은 절에 머물게 되었습니다."
                )
            }
            item {
                StoryCard(
                    title = "절에서 만난 숫자의 스승",
                    body = "그곳에서 그는 도사처럼 보이는 한 사람을 만났습니다. 그분은 거창한 말보다 사람을 오래 바라보는 분이었습니다. 생년월일의 숫자를 시작점으로 삼되, 정해진 공식에만 갇히지 않고 수많은 사람을 관찰하며 쌓은 직관과 경험을 함께 읽었습니다."
                )
            }
            item {
                StoryCard(
                    title = "변형 수리학의 시작",
                    body = "개발자는 그 방식에 매료되었습니다. 숫자는 단순한 계산이 아니라, 사람의 성향과 선택의 리듬을 비춰주는 작은 창처럼 느껴졌습니다. 그래서 전통적인 수리 해석에 실제 상담에서 얻은 관찰, 관계의 흐름, 월별 기운의 변화를 더해 이 앱만의 변형 수리학으로 다듬었습니다."
                )
            }
            item {
                StoryCard(
                    title = "수리가 들고 있는 수정구슬",
                    body = "수리는 그 여정을 상징하는 캐릭터입니다. 숫자를 차갑게 판정하는 존재가 아니라, 막막한 마음 옆에서 조심스럽게 길을 비춰주는 안내자입니다. 이 앱은 정답을 강요하지 않습니다. 대신 지금의 고민을 다른 각도에서 바라볼 수 있는 힌트를 건넵니다."
                )
            }
            item {
                StoryCard(
                    title = "이 앱이 바라는 것",
                    body = "운명은 정해진 문장이 아니라, 내가 오늘 어떤 선택을 하느냐에 따라 조금씩 모양이 달라지는 흐름이라고 믿습니다. 숫자는 그 흐름을 읽는 하나의 언어입니다. 이 앱이 당신에게 작은 용기와 새로운 관점을 건네는 조용한 수정구슬이 되기를 바랍니다.",
                    highlight = true
                )
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun StoryCard(title: String, body: String, highlight: Boolean = false) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                color = if (highlight) Gold else Accent,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                body,
                color = if (highlight) TextPrimary else TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

