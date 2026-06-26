package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.HomeFormState

class UserPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    fun loadBirthFormState(): HomeFormState? {
        if (!prefs.contains(KEY_YEAR)) return null
        return HomeFormState(
            calendarType = runCatching {
                CalendarType.valueOf(prefs.getString(KEY_CALENDAR_TYPE, CalendarType.SOLAR.name).orEmpty())
            }.getOrDefault(CalendarType.SOLAR),
            year = prefs.getString(KEY_YEAR, "").orEmpty(),
            month = prefs.getString(KEY_MONTH, "").orEmpty(),
            day = prefs.getString(KEY_DAY, "").orEmpty(),
            gender = runCatching {
                GenderOption.valueOf(prefs.getString(KEY_GENDER, GenderOption.NONE.name).orEmpty())
            }.getOrDefault(GenderOption.NONE)
        )
    }

    fun saveBirthFormState(formState: HomeFormState) {
        prefs.edit()
            .putString(KEY_CALENDAR_TYPE, formState.calendarType.name)
            .putString(KEY_YEAR, formState.year)
            .putString(KEY_MONTH, formState.month)
            .putString(KEY_DAY, formState.day)
            .putString(KEY_GENDER, formState.gender.name)
            .apply()
    }

    fun clearBirthFormState() {
        prefs.edit()
            .remove(KEY_CALENDAR_TYPE)
            .remove(KEY_YEAR)
            .remove(KEY_MONTH)
            .remove(KEY_DAY)
            .remove(KEY_GENDER)
            .apply()
    }

    fun loadNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun loadNotificationOnboardingSeen(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_ONBOARDING_SEEN, false)

    fun saveNotificationOnboardingSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ONBOARDING_SEEN, seen).apply()
    }

    private companion object {
        const val KEY_CALENDAR_TYPE = "calendar_type"
        const val KEY_YEAR = "year"
        const val KEY_MONTH = "month"
        const val KEY_DAY = "day"
        const val KEY_GENDER = "gender"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_ONBOARDING_SEEN = "notification_onboarding_seen"
    }
}
