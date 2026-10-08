package com.example.unum

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import com.example.unum.ads.AdMobConfig
import com.example.unum.data.content.TarotTheme
import com.example.unum.data.repository.DiscoveryRepository
import com.example.unum.data.repository.user.SupabaseRestUserDatabase
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DiscoveryFlowTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun repeatedAndConcurrentGuestDrawsPersistOneResult() = runBlocking {
        context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).edit().clear().commit()
        val repo=DiscoveryRepository(context,SupabaseRestUserDatabase(context,"",""))
        val first=repo.draw(null,TarotTheme.GENERAL).draw!!
        val results=coroutineScope {(0..19).map { i -> async(Dispatchers.IO) {repo.draw(null,TarotTheme.entries[i%5]).draw!!} }.awaitAll()}
        assertTrue(results.all {it==first})
        assertEquals(first,DiscoveryRepository(context,SupabaseRestUserDatabase(context,"","")).cached(null).draw)
        repo.unlock(null)
        assertEquals(first.cardId,repo.draw(null,TarotTheme.LOVE).draw!!.cardId)
        assertTrue(repo.cached(null).draw!!.detailUnlocked)
        assertEquals(5,repo.attendance(null).points)
        assertEquals(5,repo.attendance(null).points)
    }

    @Test fun appTabsTarotRevisitAndAttendanceWorkWithoutAds() {
        context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("auth_session",Context.MODE_PRIVATE).edit().clear().commit()
        assertFalse(AdMobConfig.ADS_ENABLED)
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.waitUntil(20_000) {compose.onAllNodesWithText("시작하기").fetchSemanticsNodes().isNotEmpty() || compose.onAllNodesWithText("운세").fetchSemanticsNodes().isNotEmpty()}
            if(compose.onAllNodesWithText("시작하기").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("시작하기").performScrollTo().performClick()
            compose.onNodeWithText("운세").performClick()
            compose.onNodeWithText("오늘의 타로").performScrollTo().performClick()
            compose.onNodeWithTag("tarot-confirm").performScrollTo().assertIsNotEnabled()
            compose.onNodeWithText("연애").performScrollTo().performClick()
            compose.onNodeWithTag("tarot-spread").performScrollTo().performTouchInput { swipeLeft() }
            assertFalse(context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).getString("guest",null)?.contains("card_id")==true)
            compose.onNodeWithTag("tarot-confirm").performScrollTo().performClick()
            compose.waitUntil(10_000) {context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).getString("guest",null)?.contains("card_id")==true}
            val first=JSONObject(context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).getString("guest","{}")!!).getJSONArray("history").getJSONObject(0).getInt("card_id")
            compose.onNodeWithText("홈").performClick()
            compose.onNodeWithText("운세").performClick()
            compose.onNodeWithText("오늘의 타로").performScrollTo().performClick()
            compose.onNodeWithTag("tarot-spread").assertDoesNotExist()
            compose.onNodeWithTag("tarot-confirm").assertDoesNotExist()
            assertEquals(first,JSONObject(context.getSharedPreferences("discovery_v1",Context.MODE_PRIVATE).getString("guest","{}")!!).getJSONArray("history").getJSONObject(0).getInt("card_id"))
            compose.onNodeWithText("혜택").performClick()
            compose.onNodeWithText("오늘 출석하고 +5P 받기").performScrollTo().performClick()
            compose.onNodeWithText("오늘 출석 완료").assertIsNotEnabled()
            compose.onNodeWithText("🍀 5 P").assertExists()
            compose.onNodeWithText("사주").assertDoesNotExist()
        }
    }
}
