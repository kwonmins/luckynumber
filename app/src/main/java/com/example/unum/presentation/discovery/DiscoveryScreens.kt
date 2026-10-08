package com.example.unum.presentation.discovery

import android.app.DatePickerDialog
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ads.*
import com.example.unum.data.content.*
import com.example.unum.data.model.AuthState
import com.example.unum.presentation.AppViewModel
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth

data class DiscoveryMenu(val emoji: String, val title: String, val route: String)
private const val TarotIconToken = "tarot-cards"
val discoveryMenus = listOf(
    DiscoveryMenu("☀️","오늘 운세","today"), DiscoveryMenu("🌙","내일 운세","tomorrow"),
    DiscoveryMenu("🗓️","지정일 운세","date"), DiscoveryMenu(TarotIconToken,"오늘의 타로","tarot"),
    DiscoveryMenu("🔢","수비학","numbers"), DiscoveryMenu("💞","연애·궁합","compatibility"),
    DiscoveryMenu("🌿","행운 아이템","luck"), DiscoveryMenu("📒","운세 기록","history")
)

@Composable
fun DiscoveryMenuGrid(onOpen: (String) -> Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
        discoveryMenus.chunked(4).forEach { row ->
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                row.forEach { menu ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(role=Role.Button) { onOpen(menu.route) }.padding(vertical=6.dp), horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(54.dp).background(Surface,RoundedCornerShape(17.dp)).border(1.dp,Border,RoundedCornerShape(17.dp)),contentAlignment=Alignment.Center) { DiscoveryIcon(menu.emoji) }
                        Text(menu.title,color=DeepNavy,style=MaterialTheme.typography.labelSmall,maxLines=1)
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveryScreen(viewModel: AppViewModel,onOpen: (String) -> Unit) {
    var group by rememberSaveable { mutableStateOf("운세") }
    ScreenColumn {
        item { AppTopBar("운세 모아보기",subtitle="수비학 · 타로 · 하루의 작은 힌트") }
        item { SectionTitle("자주 찾는 운세") }
        item { DiscoveryMenuGrid(onOpen) }
        item { NativeAdPlacement() }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("운세","타로·기록","프리미엄").forEach { label -> FilterChip(selected=group==label,onClick={group=label},label={Text(label)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Surface2,selectedLabelColor=DeepNavy)) }
        } }
        when(group) {
            "운세" -> {
                item { FeatureRow("☀️","오늘의 운세","총운과 분야별 해석, 시간대별 흐름") {onOpen("today")} }
                item { FeatureRow("🌙","내일의 운세","내일의 흐름 미리 살펴보기") {onOpen("tomorrow")} }
                item { FeatureRow("📆","월간 흐름","이번 달의 점수와 좋은 날짜") {onOpen("monthly")} }
                item { FeatureRow("🔢","나의 수비학","숫자로 읽는 기질과 삶의 방향") {onOpen("numbers")} }
            }
            "타로·기록" -> {
                item { FeatureRow(TarotIconToken,"오늘의 타로","하루 한 장, 나에게 필요한 메시지") {onOpen("tarot")} }
                item { FeatureRow("📒","타로 기록","지나온 카드와 해석 다시 읽기") {onOpen("tarotHistory")} }
                item { FeatureRow("🗓️","운세 기록","다른 날짜의 운세 돌아보기") {onOpen("history")} }
            }
            else -> {
                item { FeatureRow("📖","프리미엄운세","나 자신·인간관계·진로 등 맞춤 질문") {viewModel.resetPremiumFlow();onOpen("premium")} }
                item { FeatureRow("💞","연애·궁합","짝사랑·커플·재회의 고민") {onOpen("compatibility")} }
                item { FeatureRow("📚","나의 보관함","저장한 프리미엄 운세노트") {onOpen("library")} }
            }
        }
        item { BannerAdPlacement() }
    }
}

@Composable
fun TarotScreen(viewModel: AppViewModel,onBack: () -> Unit,onLogin: () -> Unit) {
    val state by viewModel.discovery.collectAsStateWithLifecycle()
    val appState by viewModel.uiState.collectAsStateWithLifecycle()
    var themeName by rememberSaveable { mutableStateOf(TarotTheme.GENERAL.name) }
    val theme=TarotTheme.valueOf(themeName)
    val activity=findActivity(LocalContext.current)
    var adBusy by remember { mutableStateOf(false) }
    var adMessage by remember { mutableStateOf<String?>(null) }
    var revealing by remember { mutableStateOf(false) }
    val manager=remember(activity) { activity?.let(::RewardedAdManager) }
    LaunchedEffect(appState.authState) { viewModel.refreshDiscovery() }
    ScreenColumn {
        item { AppTopBar("오늘의 타로",subtitle="${state.today.monthValue}월 ${state.today.dayOfMonth}일 · 하루 한 장",onBack=onBack) }
        item { Text("한국시간 자정에 새 카드가 열려요. 오늘의 결과는 다시 읽을 수 있어요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
        if(appState.authState !is AuthState.SignedIn) item { FeatureRow("🍀","기기에서 하루 한 번","로그인한 계정의 결과는 다른 기기에서도 이어서 읽어요.",onLogin) }
        if(state.draw == null) {
            item { Text("어떤 이야기가 궁금한가요?",color=DeepNavy,style=MaterialTheme.typography.titleMedium) }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    TarotTheme.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            row.forEach { t -> FilterChip(selected=t==theme,onClick={themeName=t.name},enabled=!state.busy,label={Text(t.label)}) }
                        }
                    }
                }
            }
            item { Text("질문을 떠올리고 펼쳐진 카드에서 한 장을 골라보세요.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium) }
            item {
                key(state.today, appState.authState) {
                    TarotSpreadPicker(enabled=!state.busy) {
                        revealing=true
                        viewModel.drawTarot(theme)
                    }
                }
            }
            item { Text("카드를 선택하면 오늘의 결과가 저장돼요. 분야를 바꿔도 추가로 뽑을 수 없어요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
        } else {
            val draw=requireNotNull(state.draw)
            item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
                TarotIllustratedFace(draw,animate=revealing,onRevealed={revealing=false})
            } }
            if(!revealing) {
            item { SurfaceCard { Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("${draw.theme.label} · ${draw.card.keyword}",color=DeepNavy,style=MaterialTheme.typography.titleMedium);Text(draw.card.meaning,color=TextSecondary,style=MaterialTheme.typography.bodyLarge) } } }
            if(draw.detailUnlocked || !AdMobConfig.ADS_ENABLED) {
                item { SurfaceCard { Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text("오늘의 실천과 상세 해석",color=DeepNavy,style=MaterialTheme.typography.titleMedium);Text(TarotCatalog.detail(draw),color=TextSecondary,style=MaterialTheme.typography.bodyLarge)} } }
            } else item { GradientButton(if(adBusy) "광고를 준비하고 있어요" else "광고 보고 상세 해석 열기",{
                if(manager==null) adMessage="화면을 다시 열어주세요." else {
                    adBusy=true
                    manager.showOrContinue(onRewarded={adBusy=false;viewModel.unlockTarot()},onUnavailable={adBusy=false;adMessage="광고를 준비하지 못했어요. 기존 카드는 유지됩니다."},onDismissed={adBusy=false})
                }
            },Modifier.fillMaxWidth(),enabled=!state.busy && !adBusy) }
            item { Text("오늘의 카드가 저장됐어요. 내일 다시 새로운 카드를 만나보세요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
            }
        }
        if(state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth(),color=Accent,trackColor=Surface2) }
        state.message?.let { message -> item { Text(message,color=AccentDark,style=MaterialTheme.typography.bodySmall);TextButton(onClick=viewModel::refreshDiscovery) {Text("다시 확인하기")} } }
        adMessage?.let { message -> item { Text(message,color=TextSecondary) } }
        item { NativeAdPlacement() }
    }
}

@Composable
fun BenefitsScreen(viewModel: AppViewModel,onTarot: () -> Unit,onHistory: () -> Unit) {
    val state by viewModel.discovery.collectAsStateWithLifecycle()
    val auth by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(auth.authState) {viewModel.refreshDiscovery()}
    ScreenColumn {
        item {AppTopBar("매일 모으는 행운",subtitle="출석하고 하루를 가볍게 시작해요")}
        item {SurfaceCard {Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {Text("나의 행운 포인트",color=TextSecondary);Text("🍀 ${state.points} P",fontSize=36.sp,color=DeepNavy);Text("연속 출석 ${state.streak}일",color=AccentDark)}}}
        item {GradientButton(if(state.attendanceDate==state.today.toString()) "오늘 출석 완료" else "오늘 출석하고 +5P 받기",viewModel::claimAttendance,Modifier.fillMaxWidth(),enabled=!state.busy && state.attendanceDate!=state.today.toString())}
        state.message?.let { message -> item {Text(message,color=AccentDark)} }
        item {SurfaceCard {Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {Text("광고 보고 포인트 받기",color=DeepNavy,style=MaterialTheme.typography.titleMedium);Text("광고 적립은 추후 오픈돼요.",color=TextSecondary);GradientButton("준비 중",{},Modifier.fillMaxWidth(),enabled=false)}}}
        item {FeatureRow(TarotIconToken,"오늘의 타로","포인트 차감 없이 하루 한 장",onTarot)}
        item {FeatureRow("📒","타로 기록","지난 카드를 다시 읽어보세요",onHistory)}
        item {Text("포인트 사용처와 추가 혜택은 추후 안내해드려요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
        item {NativeAdPlacement()}
    }
}

@Composable
fun TarotHistoryScreen(viewModel: AppViewModel,onBack: () -> Unit,onTarot: () -> Unit) {
    val state by viewModel.discovery.collectAsStateWithLifecycle()
    var selectedDate by rememberSaveable {mutableStateOf<String?>(null)}
    val chosen=state.history.firstOrNull {it.date.toString()==selectedDate}
    chosen?.let { draw -> AlertDialog(onDismissRequest={selectedDate=null},containerColor=Surface,title={Text("${draw.date} · ${draw.card.name}")},text={Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {TarotIllustratedFace(draw)};Text(draw.card.keyword);Text(if(draw.detailUnlocked || !AdMobConfig.ADS_ENABLED) TarotCatalog.detail(draw) else draw.card.meaning)}},confirmButton={TextButton(onClick={selectedDate=null}) {Text("닫기")}}) }
    ScreenColumn {
        item {AppTopBar("타로 기록",subtitle="최근 90일의 카드와 이야기",onBack=onBack)}
        if(state.history.isEmpty()) item {EmptyStateView("아직 카드가 없어요","오늘 나에게 필요한 메시지를 만나보세요.","오늘의 타로 보기",onTarot)}
        state.history.forEach { draw -> item(key=draw.date.toString()) {FeatureRow(TarotIconToken,"${draw.date} · ${draw.card.name}","${draw.theme.label} · ${draw.card.keyword}") {selectedDate=draw.date.toString()}} }
    }
}

@Composable
fun MonthlyScreen(viewModel: AppViewModel,onBack: () -> Unit,onInput: () -> Unit,onDate: (LocalDate) -> Unit) {
    val app by viewModel.uiState.collectAsStateWithLifecycle()
    var monthText by rememberSaveable {mutableStateOf(YearMonth.from(TarotCatalog.today()).toString())}
    val month=YearMonth.parse(monthText)
    val days=(1..month.lengthOfMonth()).mapNotNull { viewModel.dailyFortune(month.atDay(it)) }
    ScreenColumn {
        item {AppTopBar("월간 흐름",onBack=onBack)}
        item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {TextButton(onClick={monthText=month.minusMonths(1).toString()}) {Text("← 이전")};Text("${month.year}년 ${month.monthValue}월",color=DeepNavy);TextButton(onClick={monthText=month.plusMonths(1).toString()}) {Text("다음 →")}}}
        if(app.latestBundle==null) item {EmptyStateView("나의 정보를 먼저 알려주세요","생년월일의 수리 숫자로 월간 흐름을 살펴봐요.","생년월일 입력",onInput)}
        else {
            item {SurfaceCard {Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text("이달의 평균 흐름",color=DeepNavy);ScoreLabel(days.map {it.score}.average().toInt(),large=true);Text("날짜별 수리 운세의 평균 참고 지표예요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)}}}
            item {SectionTitle("흐름이 좋은 날짜")}
            days.sortedWith(compareByDescending<com.example.unum.data.model.DailyFortuneResult> {it.score}.thenBy {it.date}).take(5).forEach { d -> item {FeatureRow("📅","${d.date.dayOfMonth}일 · ${d.score}점",d.coreSummary) {onDate(d.date)}} }
            item {NativeAdPlacement()}
        }
    }
}

@Composable
fun NumerologyScreen(viewModel: AppViewModel,onBack: () -> Unit,onInput: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bundle=state.latestBundle
    ScreenColumn {
        item {AppTopBar("나의 수비학",subtitle="숫자로 살펴보는 기질과 삶의 방향",onBack=onBack)}
        if(bundle==null) item {EmptyStateView("나의 숫자를 만나보세요","생년월일을 알려주시면 수리 숫자를 읽어드려요.","생년월일 입력",onInput)}
        else {
            item {SurfaceCard {Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {Text("나의 수리 코드",color=TextSecondary);Text(bundle.numbers.code,fontSize=40.sp,color=DeepNavy);Text(bundle.content.destinyProfile.title,color=DeepNavy,style=MaterialTheme.typography.titleMedium)}}}
            bundle.freeReading?.let { reading -> listOf("나의 성향" to reading.opening,"숫자의 흐름" to reading.core,"나의 장점" to reading.strength,"돌아볼 점" to reading.caution,"실천 방향" to reading.action,"관계" to reading.relationship,"일" to reading.work,"돈" to reading.money).forEach { (title,body) -> item {Text(title,color=DeepNavy,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(10.dp));Text(body,color=TextSecondary,style=MaterialTheme.typography.bodyLarge)} } }
            item {NativeAdPlacement()}
        }
    }
}

@Composable
fun HistoryScreen(viewModel: AppViewModel,onBack: () -> Unit,onDate: (LocalDate) -> Unit,onTarotHistory: () -> Unit,onLibrary: () -> Unit) {
    val app by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenColumn {
        item {AppTopBar("운세 기록",onBack=onBack)}
        item {FeatureRow(TarotIconToken,"나의 타로 기록","뽑았던 카드와 메시지",onTarotHistory)}
        item {FeatureRow("📚","프리미엄 보관함","저장한 운세노트",onLibrary)}
        item {SectionTitle("지난 7일의 흐름")}
        if(app.latestBundle!=null) (0..6).forEach { offset -> val date=TarotCatalog.today().minusDays(offset.toLong()); val d=viewModel.dailyFortune(date)!!; item {FeatureRow("📅","${date.monthValue}월 ${date.dayOfMonth}일 · ${d.score}점",d.coreSummary) {onDate(date)}} }
        else item {Text("생년월일을 입력하면 날짜별 운세를 살펴볼 수 있어요.",color=TextSecondary)}
    }
}

@Composable
fun FeatureRow(emoji: String,title: String,description: String,onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(18.dp)).clickable(role=Role.Button,onClick=onClick).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        DiscoveryIcon(emoji)
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {Text(title,color=DeepNavy,style=MaterialTheme.typography.titleSmall);Text(description,color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
        Text("→",color=Accent)
    }
}

@Composable
private fun DiscoveryIcon(token: String) {
    if (token == TarotIconToken) {
        TarotMenuIcon()
    } else {
        val art = when (token) {
            "☀️" -> FortuneArt.SUN
            "🌙" -> FortuneArt.MOON
            "🗓️", "📆", "📅" -> FortuneArt.CALENDAR
            "🔢" -> FortuneArt.NUMBERS
            "💞" -> FortuneArt.RINGS
            "🌿" -> FortuneArt.LEAF
            "📒", "📖", "📚" -> FortuneArt.BOOK
            "🍀" -> FortuneArt.CLOVER
            "🎁" -> FortuneArt.GIFT
            else -> FortuneArt.MOON_STARS
        }
        FortuneIllustration(art, Modifier.size(36.dp))
    }
}

@Composable
private fun TarotMenuIcon() {
    TarotArtwork(23,Modifier.width(34.dp).height(42.dp))
}

@Composable
private fun ScreenColumn(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) { LazyColumn(contentPadding=PaddingValues(20.dp,16.dp,20.dp,28.dp),verticalArrangement=Arrangement.spacedBy(18.dp),content=content) }
}

fun chooseFortuneDate(context: Context,onChosen: (LocalDate) -> Unit) {
    val today=TarotCatalog.today()
    DatePickerDialog(context,{_,y,m,d->onChosen(LocalDate.of(y,m+1,d))},today.year,today.monthValue-1,today.dayOfMonth).show()
}

private tailrec fun findActivity(context: Context): Activity? = when(context) {is Activity->context;is ContextWrapper->findActivity(context.baseContext);else->null}
