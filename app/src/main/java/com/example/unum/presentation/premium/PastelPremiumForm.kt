package com.example.unum.presentation.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.data.model.*
import com.example.unum.presentation.AppUiState
import com.example.unum.presentation.AppViewModel
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*

@Composable
internal fun PastelPremiumForm(state: AppUiState, viewModel: AppViewModel, onStart: () -> Unit) {
    var enteringQuestion by rememberSaveable { mutableStateOf(false) }
    var showMore by rememberSaveable { mutableStateOf(false) }
    var selectedTitle by rememberSaveable { mutableStateOf("") }
    val romance=state.premiumMode==PremiumMode.COMPATIBILITY
    MysticBackground(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            QuietLandscape(Modifier.fillMaxWidth().height(160.dp).align(Alignment.BottomCenter),blossoms=romance)
        }
        Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            AppTopBar(if(enteringQuestion) "질문하기" else "프리미엄 운세",onBack=if(enteringQuestion) {{enteringQuestion=false}} else null)
            if(!enteringQuestion) {
                Row(Modifier.fillMaxWidth().background(Surface2,RoundedCornerShape(24.dp)).padding(4.dp)) {
                    listOf("일반" to PremiumMode.PERSONAL,"연애" to PremiumMode.COMPATIBILITY).forEach { (label,mode) ->
                        val selected=state.premiumMode==mode
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(if(selected) Surface else Surface2).clickable { viewModel.setPremiumMode(mode) }.padding(13.dp),contentAlignment=Alignment.Center) { Text(label,color=if(selected) DeepNavy else TextMuted,style=MaterialTheme.typography.labelLarge) }
                    }
                }
                Text("어떤 고민을 들여다볼까요?",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
                if(romance) {
                    val types=listOf(Triple("짝사랑",FortuneArt.HEART,CompatibilityRelationshipStatus.CRUSH),Triple("커플",FortuneArt.RINGS,CompatibilityRelationshipStatus.COUPLE),Triple("재회",FortuneArt.BLOSSOM,CompatibilityRelationshipStatus.REUNION))
                    types.forEach { (label,art,status) ->
                        PastelCategoryRow(label,when(status) { CompatibilityRelationshipStatus.CRUSH -> "짝사랑 상대에 대해 궁금한 점을 물어보세요."; CompatibilityRelationshipStatus.COUPLE -> "연인과의 관계, 마음에 대해 함께 알아보세요."; else -> "헤어진 사람과의 재회 가능성이 궁금한가요?" },art) {
                            viewModel.setCompatibilityRelationshipStatus(status); enteringQuestion=true
                        }
                    }
                } else {
                    listOf(Triple("학업운",FortuneArt.BOOK,PremiumTopic.STUDY),Triple("재물운",FortuneArt.COINS,PremiumTopic.MONEY),Triple("시험운",FortuneArt.SCROLL,PremiumTopic.STUDY)).forEach { (label,art,topic) ->
                        PastelCategoryRow(label,when(label) { "학업운" -> "학업과 관련된 고민을 물어보세요."; "재물운" -> "재물, 금전, 투자 등 궁금한 점을 물어보세요."; else -> "시험, 합격에 관한 고민을 나눠보세요." },art) { selectedTitle=label; viewModel.selectPremiumTopic(topic); enteringQuestion=true }
                    }
                    TextButton(onClick={showMore=!showMore}) { Text(if(showMore) "다른 분야 접기" else "다른 분야도 살펴보기",color=Accent) }
                    if(showMore) PremiumTopic.entries.filter { it !in setOf(PremiumTopic.STUDY,PremiumTopic.MONEY) }.forEach { topic ->
                        val art = topic.fortuneArt()
                        PastelCategoryRow(
                            topic.label,
                            "수리에게 당신의 고민을 들려주세요.",
                            art
                        ) {selectedTitle=topic.label;viewModel.selectPremiumTopic(topic);enteringQuestion=true}
                    }
                }
                Row(verticalAlignment=Alignment.CenterVertically) { PastelSuri(2,Modifier.size(54.dp)); Text("마음에 담아둔 질문 하나,\n수리가 함께 읽어드릴게요.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium) }
            } else {
                val concern=if(romance) state.compatibilityConcern else state.premiumConcern
                Text(if(romance) state.compatibilityForm.relationshipStatus.label else selectedTitle.ifBlank {state.premiumTopic.label},color=Accent,style=MaterialTheme.typography.labelLarge)
                if(state.latestBundle==null) {
                    SurfaceCard {
                        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Text("먼저 나의 생년월일을 알려주세요",color=DeepNavy,style=MaterialTheme.typography.titleSmall)
                            ToggleSegment(state.formState.calendarType,viewModel::setCalendarType)
                            DateInputRow(state.formState.year,state.formState.month,state.formState.day,viewModel::updateYear,viewModel::updateMonth,viewModel::updateDay)
                            GenderSelector(state.formState.gender,viewModel::setGender)
                            GradientButton("내 정보 확인",{viewModel.calculateAndStore()},Modifier.fillMaxWidth(),enabled=!state.isLoading)
                        }
                    }
                }
                if(romance) {
                    val partner=state.compatibilityForm.partner
                    SurfaceCard {
                        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Text("상대방의 생년월일",color=DeepNavy,style=MaterialTheme.typography.titleSmall)
                            ToggleSegment(partner.calendarType,viewModel::setCompatibilityPartnerCalendarType)
                            DateInputRow(partner.year,partner.month,partner.day,viewModel::updateCompatibilityPartnerYear,viewModel::updateCompatibilityPartnerMonth,viewModel::updateCompatibilityPartnerDay)
                            GenderSelector(state.compatibilityForm.partnerGender,viewModel::setCompatibilityPartnerGender)
                        }
                    }
                }
                OutlinedTextField(value=concern,onValueChange={ if(romance) viewModel.updateCompatibilityConcern(it.take(500)) else viewModel.updatePremiumConcern(it.take(500)) },modifier=Modifier.fillMaxWidth().heightIn(min=180.dp),shape=RoundedCornerShape(20.dp),placeholder={Text("지금 가장 궁금한 이야기를\n자유롭게 들려주세요.\n예: 이번 시험에 합격할 수 있을까요?",style=MaterialTheme.typography.bodyMedium)},supportingText={Text("${concern.length} / 500",modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.End)},colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Accent,unfocusedBorderColor=Border,focusedContainerColor=Surface,unfocusedContainerColor=Surface,focusedTextColor=TextPrimary,unfocusedTextColor=TextPrimary))
                Text("추천 질문",color=DeepNavy,style=MaterialTheme.typography.titleSmall)
                val suggestions = premiumQuestionSuggestions(
                    romance = romance,
                    relationshipStatus = state.compatibilityForm.relationshipStatus,
                    topic = state.premiumTopic,
                    selectedTitle = selectedTitle
                )
                suggestions.forEach { question ->
                    Text(question,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(24.dp)).clickable {if(romance) viewModel.updateCompatibilityConcern(question) else viewModel.updatePremiumConcern(question)}.padding(14.dp),color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                }
                state.inputError?.let { Text(it,color=Rose,style=MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(16.dp))
        }
        if(enteringQuestion) {
            val concern=if(romance) state.compatibilityConcern else state.premiumConcern
            val partner=state.compatibilityForm.partner
            val ready=state.latestBundle!=null && concern.trim().length>=6 && (!romance || (partner.year.length==4 && partner.month.isNotBlank() && partner.day.isNotBlank()))
            Column(Modifier.fillMaxWidth().background(Background).imePadding().padding(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                GradientButton("질문하기",onStart,Modifier.fillMaxWidth(),enabled=ready)
                Text("질문을 확인한 뒤 이용 방법을 안내해드려요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            }
        }
        }
    }
}

private fun premiumQuestionSuggestions(
    romance: Boolean,
    relationshipStatus: CompatibilityRelationshipStatus,
    topic: PremiumTopic,
    selectedTitle: String
): List<String> {
    if (romance) {
        return when (relationshipStatus) {
            CompatibilityRelationshipStatus.CRUSH -> listOf(
                "그 사람도 저에게 관심이 있을까요?",
                "어떤 방식으로 다가가면 부담이 적을까요?",
                "지금 고백을 준비해도 괜찮을까요?"
            )
            CompatibilityRelationshipStatus.COUPLE -> listOf(
                "요즘 상대방은 어떤 마음인지 궁금해요.",
                "우리 사이의 갈등을 어떻게 풀면 좋을까요?",
                "이 관계를 오래 이어가려면 무엇이 필요할까요?"
            )
            CompatibilityRelationshipStatus.REUNION -> listOf(
                "지금 다시 연락해도 괜찮은 시기일까요?",
                "헤어진 이유를 어떻게 바라봐야 할까요?",
                "재회를 서두르지 않으려면 무엇을 조심해야 할까요?"
            )
        }
    }
    return when {
        topic == PremiumTopic.MONEY -> listOf(
            "이번 달 지출에서 특히 조심할 점은 무엇인가요?",
            "이 제안을 지금 검토해도 괜찮을까요?",
            "돈을 모으기 위해 먼저 바꿔야 할 습관은 무엇인가요?"
        )
        topic == PremiumTopic.HEALTH -> listOf(
            "요즘 피로를 줄이려면 무엇부터 돌봐야 할까요?",
            "생활 리듬을 어떻게 조정하면 좋을까요?",
            "무리하지 않고 회복하려면 무엇이 필요할까요?"
        )
        topic == PremiumTopic.CAREER || topic == PremiumTopic.BUSINESS -> listOf(
            "지금 이직이나 방향 전환을 고민해도 될까요?",
            "일에서 제가 놓치고 있는 기회는 무엇인가요?",
            "이번 달 중요한 선택에서 무엇을 먼저 확인할까요?"
        )
        topic == PremiumTopic.STUDY && selectedTitle.contains("시험") -> listOf(
            "이번 시험에서 제가 놓치고 있는 점은 무엇인가요?",
            "지금 공부 방향을 계속 이어가도 될까요?",
            "시험 전 집중력을 높이려면 무엇이 필요할까요?"
        )
        topic == PremiumTopic.STUDY -> listOf(
            "지금의 공부 방법이 저와 잘 맞을까요?",
            "집중이 흐트러질 때 어떤 방향을 잡으면 좋을까요?",
            "공부를 꾸준히 이어가기 위해 무엇을 조절할까요?"
        )
        topic == PremiumTopic.SELF_ESTEEM -> listOf(
            "요즘 자신감을 잃게 하는 원인은 무엇일까요?",
            "다른 사람과 비교하는 마음을 어떻게 다루면 좋을까요?",
            "지금 저에게 가장 필요한 회복은 무엇일까요?"
        )
        else -> listOf(
            "이번 달 제가 집중해서 살펴볼 흐름은 무엇인가요?",
            "지금 선택하기 전에 꼭 확인할 점은 무엇인가요?",
            "좋은 흐름을 이어가기 위해 무엇부터 바꾸면 좋을까요?"
        )
    }
}

@Composable
private fun PastelCategoryRow(title: String, description: String, art: FortuneArt, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(20.dp)).clickable(onClick=onClick).padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        FortuneIllustration(art, Modifier.size(42.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(title,color=DeepNavy,style=MaterialTheme.typography.titleSmall); Text(description,color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward,null,tint=Accent,modifier=Modifier.size(18.dp))
    }
}
