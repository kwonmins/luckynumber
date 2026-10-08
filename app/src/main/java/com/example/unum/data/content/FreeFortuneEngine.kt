package com.example.unum.data.content

import com.example.unum.data.model.*
import com.example.unum.domain.NumerologyCalculator
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.absoluteValue

/** All editable reading text comes from FreeFortuneContent.json. */
class FreeFortuneEngine(rawContent: String) {
    private val root = JSONObject(rawContent.trimStart('\uFEFF'))
    private val daily = root.getJSONObject("daily")
    private val profiles = root.getJSONArray("profiles").let { array ->
        (0 until array.length()).associate { i ->
            val p = array.getJSONObject(i)
            val profile = DestinyProfile(
                destiny=p.getInt("destiny"), title=p.getString("title"), polarity=p.getString("polarity"),
                coreKeywords=p.getJSONArray("coreKeywords").strings(), cautionKeywords=p.getJSONArray("cautionKeywords").strings(),
                resultTitle=p.getString("resultTitle"), summary=p.getString("summary"), strength=p.getString("strength"),
                caution=p.getString("caution"), actionGuide=p.getString("actionGuide"), earlyScene=p.optString("earlyScene"),
                middleScene=p.optString("middleScene"), lateScene=p.optString("lateScene"), relationshipScene=p.optString("relationshipScene"),
                workScene=p.optString("workScene"), moneyScene=p.optString("moneyScene")
            )
            profile.destiny to profile
        }
    }
    init {
        require(root.getInt("version") == 1) { "지원하지 않는 무료 콘텐츠 버전입니다." }
        require(profiles.keys == (0..9).toSet()) { "0~9 숫자 프로필을 모두 제공해야 합니다." }
        (0..9).forEach { require(root.getJSONObject("interactions").getJSONArray(it.toString()).length()==10) }
        DailyFortuneTopic.entries.forEach { require(daily.getJSONObject("messages").getJSONArray(it.name).length()>0) }
    }

    fun content(code: String, gender: GenderOption): NumerologyContent {
        require(code.length==4 && code.all(Char::isDigit))
        val p=code.map { profiles.getValue(it.digitToInt()) }
        return NumerologyContent(p[0], build(code,gender,p[0],p[1],p[2],p[3]))
    }

    fun bundle(input: BirthInput): NumerologyResultBundle {
        val lunar=if(input.calendarType==CalendarType.SOLAR) NumerologyCalculator.toLunarBirthInput(input) else input
        val solar=if(input.calendarType==CalendarType.LUNAR) NumerologyCalculator.toSolarBirthInput(input) else input
        val numbers=NumerologyCalculator.calculate(lunar)
        val content=content(numbers.code,input.gender)
        return NumerologyResultBundle(lunar,numbers,content,solar,reading(content))
    }

    fun describe(destiny: Int, companion: Int): String {
        require(destiny in 0..9 && companion in 0..9)
        return root.getJSONObject("interactions").getJSONArray(destiny.toString()).getString(companion)
    }

    fun build(code: String, gender: GenderOption, profile: DestinyProfile, earlyProfile: DestinyProfile, middleProfile: DestinyProfile, lateProfile: DestinyProfile): LifeRecord {
        val interaction="처음 반응에서는 ${describe(profile.destiny,earlyProfile.destiny)} 시간이 지나 역할이 커지면 ${describe(profile.destiny,middleProfile.destiny)}"
        val even=profile.destiny%2==0
        val genderKey=when(gender) {GenderOption.MALE -> if(even) "maleEven" else "maleOdd"; GenderOption.FEMALE -> if(even) "femaleEven" else "femaleOdd"; else -> null}
        return LifeRecord(code=code,destiny=profile.destiny,destinyProfileKey=profile.destiny,
            lifeTitle="운세 성향 ${profile.destiny}번 리포트",destinyText=profile.summary,
            lifeText=listOf(profile.summary,earlyProfile.earlyScene,middleProfile.middleScene,lateProfile.lateScene,profile.caution).filter(String::isNotBlank).joinToString("\n\n"),
            summaryText=interaction,keywords=profile.coreKeywords,cautionKeywords=profile.cautionKeywords,
            earlyText=earlyProfile.earlyScene,middleText=middleProfile.middleScene,lateText=lateProfile.lateScene,
            interactionText=interaction,genderText=genderKey?.let {root.getJSONObject("genderContext").getString(it)}.orEmpty(),
            relationshipText=profile.relationshipScene,workText=profile.workScene,moneyText=profile.moneyScene,actionText=profile.actionGuide)
    }

    fun reading(content: NumerologyContent): FreeReadingResult {
        val p=content.destinyProfile;val r=content.lifeRecord
        return FreeReadingResult(p.summary.ifBlank {p.resultTitle},listOf(r.interactionText,r.genderText).filter(String::isNotBlank).joinToString(" ").ifBlank {p.summary},p.strength,p.caution,r.actionText.ifBlank {p.actionGuide},r.earlyText,r.middleText,r.lateText,r.relationshipText,r.workText,r.moneyText)
    }

    fun daily(numbers: NumerologyNumbers, date: LocalDate=LocalDate.now()): DailyFortuneResult {
        val core=calculateCoreNumber(numbers,date)
        val topics=listOf(DailyFortuneTopic.LOVE,DailyFortuneTopic.MONEY,DailyFortuneTopic.WORK,DailyFortuneTopic.HEALTH,DailyFortuneTopic.LUCK).map { topic ->
            val pool=daily.getJSONObject("messages").getJSONArray(topic.name).strings()
            val seed=date.dayOfYear+core*3+topic.messageSeed*5+numbers.early*7+numbers.middle*11+numbers.late*13
            DailyTopicFortune(topic,pool[Math.floorMod(seed,pool.size)],65+Math.floorMod(seed,31),daily.getJSONObject("keywords").getString(topic.name))
        }
        val seed=date.toEpochDay()+numbers.code.toInt()
        val colors=daily.getJSONArray("colors").strings();val times=daily.getJSONArray("times").strings()
        return DailyFortuneResult(date,core,daily.getJSONObject("titles").getString(core.toString()),daily.getJSONObject("summaries").getString(core.toString()),topics,
            score=65+Math.floorMod(seed,31L).toInt(),luckyNumber=core,luckyColor=colors[Math.floorMod(seed,colors.size.toLong()).toInt()],luckyTime=times[Math.floorMod(seed,times.size.toLong()).toInt()])
    }

    internal fun calculateCoreNumber(numbers: NumerologyNumbers,date: LocalDate): Int {
        var value=date.year.absoluteValue.toString().sumOf {it.digitToInt()}+date.monthValue+date.dayOfMonth+numbers.destiny+numbers.early+numbers.middle+numbers.late
        while(value>9) value=value.toString().sumOf {it.digitToInt()}
        return value.coerceAtLeast(1)
    }
    private fun JSONArray.strings()=(0 until length()).map(::getString)
}
