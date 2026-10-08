package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.content.*
import com.example.unum.data.model.AuthUser
import com.example.unum.data.repository.user.SupabaseRestUserDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.time.LocalDate

data class DiscoveryState(
    val today: LocalDate = TarotCatalog.today(),
    val draw: TarotDraw? = null,
    val history: List<TarotDraw> = emptyList(),
    val points: Int = 0,
    val attendanceDate: String = "",
    val streak: Int = 0,
    val busy: Boolean = false,
    val message: String? = null
)

class DiscoveryRepository(context: Context, private val remote: SupabaseRestUserDatabase) {
    private val prefs = context.getSharedPreferences("discovery_v1", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val random = SecureRandom()
    private fun key(user: AuthUser?) = user?.id ?: "guest"
    fun cached(user: AuthUser?): DiscoveryState {
        val data = JSONObject(prefs.getString(key(user), "{}") ?: "{}")
        val history = data.optJSONArray("history")?.let { a -> (0 until a.length()).mapNotNull { i -> runCatching { decode(a.getJSONObject(i)) }.getOrNull() } }.orEmpty()
        val today = TarotCatalog.today()
        return DiscoveryState(today, history.firstOrNull { it.date == today }, history, data.optInt("points"), data.optString("attendance"), data.optInt("streak"))
    }
    suspend fun refresh(user: AuthUser?): DiscoveryState = mutex.withLock {
        if(user == null) return@withLock cached(null)
        val status = remote.featureRpc(user, "daily_tarot_status")
        val history = remote.tarotHistory(user).let { a -> (0 until a.length()).map { decode(a.getJSONObject(it)) } }
        val draw = status.optJSONObject("draw")?.let(::decode)
        val wallet = status.optJSONObject("wallet")
        val state = DiscoveryState(LocalDate.parse(status.getString("today")), draw, history,
            wallet?.optInt("balance") ?: 0, wallet?.optString("attendance_date")?.takeUnless { it == "null" }.orEmpty(), wallet?.optInt("streak") ?: 0)
        save(user, state)
        state
    }
    suspend fun draw(user: AuthUser?, theme: TarotTheme): DiscoveryState = mutex.withLock {
        val old = cached(user)
        val next = if(user != null) decode(remote.featureRpc(user,"draw_daily_tarot",JSONObject().put("p_theme",theme.name))) else {
            old.draw ?: run {
                check(old.history.none { !it.date.isBefore(old.today) }) { "오늘의 카드를 이미 뽑았어요. 날짜 설정을 확인해주세요." }
                TarotDraw(old.today, theme, random.nextInt(TarotCatalog.cards.size))
            }
        }
        val history = (listOf(next) + old.history.filter { it.date != next.date }).sortedByDescending { it.date }.take(90)
        val state = old.copy(today=next.date, draw=next, history=history, message=null)
        save(user,state)
        state
    }
    suspend fun unlock(user: AuthUser?): DiscoveryState = mutex.withLock {
        val old=cached(user)
        val next=if(user!=null) decode(remote.featureRpc(user,"unlock_daily_tarot")) else requireNotNull(old.draw).copy(detailUnlocked=true)
        val state=old.copy(today=next.date,draw=next,history=old.history.map { if(it.date==next.date) next else it })
        save(user,state);state
    }
    suspend fun attendance(user: AuthUser?): DiscoveryState = mutex.withLock {
        val old=cached(user)
        val state=if(user!=null) {
            val wallet=remote.featureRpc(user,"claim_daily_attendance")
            old.copy(attendanceDate=wallet.getString("attendance_date"),points=wallet.getInt("balance"),streak=wallet.getInt("streak"),message="출석을 확인했어요.")
        } else {
            if(old.attendanceDate == old.today.toString()) return@withLock old.copy(message="오늘은 이미 출석했어요.")
            check(old.attendanceDate.isBlank() || LocalDate.parse(old.attendanceDate).isBefore(old.today)) { "날짜 설정을 확인해주세요." }
            old.copy(points=old.points+5,attendanceDate=old.today.toString(),streak=if(old.attendanceDate==old.today.minusDays(1).toString()) old.streak+1 else 1,message="출석 보상 +5P를 받았어요.")
        }
        save(user,state);state
    }
    private fun decode(j: JSONObject)=TarotDraw(LocalDate.parse(j.getString("draw_date")),TarotTheme.valueOf(j.getString("theme")),j.getInt("card_id").also { require(it in TarotCatalog.cards.indices) },j.optBoolean("detail_unlocked"))
    private fun save(user: AuthUser?, state: DiscoveryState) {
        val history=JSONArray()
        state.history.forEach { d -> history.put(JSONObject().put("draw_date",d.date.toString()).put("theme",d.theme.name).put("card_id",d.cardId).put("detail_unlocked",d.detailUnlocked)) }
        val j=JSONObject().put("history",history).put("points",state.points).put("attendance",state.attendanceDate).put("streak",state.streak)
        check(prefs.edit().putString(key(user),j.toString()).commit()) { "결과 저장에 실패했어요. 다시 확인해주세요." }
    }
}
