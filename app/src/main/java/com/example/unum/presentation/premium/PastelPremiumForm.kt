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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            AppTopBar(if(enteringQuestion) "질문하기" else "프리미엄 운세",onBack=if(enteringQuestion) {{enteringQuestion=false}} else null)
            if(!enteringQuestion) {
                Row(Modifier.fillMaxWidth().background(Surface2,RoundedCornerShape(24.dp)).padding(4.dp)) {
                    listOf("일반" to PremiumMode.PERSONAL,"연애" to PremiumMode.COMPATIBILITY).forEach { (label,mode) ->
                        val selected=state.premiumMode==mode
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(if(selected) Surface else Surface2).clickable { viewModel.setPremiumMode(mode) }.padding(13.dp),contentAlignment=Alignment.Center) { Text(label,color=if(selected) DeepNavy else TextMuted,style=MaterialTheme.typography.labelLarge) }
                    }
                }
                Text("어떤 이야기가 궁금하세요?",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
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
                        PastelCategoryRow(topic.label,"수리에게 당신의 고민을 들려주세요.",when(topic) { PremiumTopic.ROMANCE -> FortuneArt.HEART; PremiumTopic.HEALTH -> FortuneArt.LEAF; PremiumTopic.BUSINESS -> FortuneArt.CASE; else -> FortuneArt.CLOVER }) {selectedTitle=topic.label;viewModel.selectPremiumTopic(topic);enteringQuestion=true}
                    }
                }
                Row(verticalAlignment=Alignment.CenterVertically) { PastelSuri(2,Modifier.size(94.dp)); Text("마음에 담아둔 질문 하나,\n수리가 함께 읽어드릴게요.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium) }
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
                OutlinedTextField(value=concern,onValueChange={ if(romance) viewModel.updateCompatibilityConcern(it.take(500)) else viewModel.updatePremiumConcern(it.take(500)) },modifier=Modifier.fillMaxWidth().heightIn(min=180.dp),shape=RoundedCornerShape(20.dp),placeholder={Text("궁금한 것을 자유롭게 물어보세요.\n예: 이번 시험에 합격할 수 있을까요?",style=MaterialTheme.typography.bodyMedium)},supportingText={Text("${concern.length} / 500",modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.End)},colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Accent,unfocusedBorderColor=Border,focusedContainerColor=Surface,unfocusedContainerColor=Surface,focusedTextColor=TextPrimary,unfocusedTextColor=TextPrimary))
                Text("추천 질문",color=DeepNavy,style=MaterialTheme.typography.titleSmall)
                val suggestions=if(romance) listOf("그 사람에게 먼저 연락해도 될까요?","그 사람의 마음을 알고 싶어요.","우리 관계가 좋아질 수 있을까요?") else listOf("이번 달에 좋은 기회가 있을까요?","지금 이 선택, 계속해도 될까요?","앞으로 어떤 준비를 하면 좋을까요?")
                suggestions.forEach { question ->
                    Text(question,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(24.dp)).clickable {if(romance) viewModel.updateCompatibilityConcern(question) else viewModel.updatePremiumConcern(question)}.padding(14.dp),color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                }
                val partner=state.compatibilityForm.partner
                val ready=state.latestBundle!=null && concern.trim().length>=6 && (!romance || (partner.year.length==4 && partner.month.isNotBlank() && partner.day.isNotBlank()))
                GradientButton("질문하기",onStart,Modifier.fillMaxWidth(),enabled=ready)
                Text("질문을 확인한 뒤 이용 방법을 안내해드려요.",color=TextMuted,style=MaterialTheme.typography.bodySmall)
                state.inputError?.let { Text(it,color=Rose,style=MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PastelCategoryRow(title: String, description: String, art: FortuneArt, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(20.dp)).clickable(onClick=onClick).padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        FortuneIllustration(art,Modifier.size(52.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(title,color=DeepNavy,style=MaterialTheme.typography.titleSmall); Text(description,color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward,null,tint=Accent,modifier=Modifier.size(18.dp))
    }
}
