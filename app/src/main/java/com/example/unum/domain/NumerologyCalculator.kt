package com.example.unum.domain

import android.icu.util.Calendar
import android.icu.util.ChineseCalendar
import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.NumerologyNumbers
import java.util.GregorianCalendar
import java.util.TimeZone

object NumerologyCalculator {
    fun calculate(input: BirthInput): NumerologyNumbers {
        val yearSum = input.year.toString().map { it.digitToInt() }.sum()
        val monthSum = input.month.toString().padStart(2, '0').map { it.digitToInt() }.sum()
        val daySum = input.day.toString().padStart(2, '0').map { it.digitToInt() }.sum()

        val early = daySum % 10
        val middle = monthSum % 10
        val late = yearSum % 10
        val destiny = (yearSum + monthSum + daySum) % 10

        return NumerologyNumbers(destiny, early, middle, late, "$destiny$early$middle$late")
    }

    fun toBirthInput(form: HomeFormState): BirthInput? {
        val year = form.year.toIntOrNull() ?: return null
        val month = form.month.toIntOrNull() ?: return null
        val day = form.day.toIntOrNull() ?: return null
        if (!isValidSolarDate(year, month, day)) return null
        return BirthInput(CalendarType.SOLAR, year, month, day, form.gender)
    }

    fun toLunarBirthInput(solarInput: BirthInput): BirthInput {
        val gregorian = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).apply {
            isLenient = false
            set(solarInput.year, solarInput.month - 1, solarInput.day, 12, 0, 0)
            set(GregorianCalendar.MILLISECOND, 0)
        }
        val lunar = ChineseCalendar(gregorian.time)
        val lunarYear = lunar.get(Calendar.EXTENDED_YEAR) - CHINESE_YEAR_OFFSET
        val lunarMonth = lunar.get(Calendar.MONTH) + 1
        val lunarDay = lunar.get(Calendar.DAY_OF_MONTH)

        return BirthInput(
            calendarType = CalendarType.LUNAR,
            year = lunarYear,
            month = lunarMonth,
            day = lunarDay,
            gender = solarInput.gender
        )
    }

    private fun isValidSolarDate(year: Int, month: Int, day: Int): Boolean {
        return runCatching {
            GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).apply {
                isLenient = false
                set(year, month - 1, day, 12, 0, 0)
                set(GregorianCalendar.MILLISECOND, 0)
                time
            }
        }.isSuccess
    }

    fun formatDate(year: Int, month: Int, day: Int): String = "%04d.%02d.%02d".format(year, month, day)
    fun calendarLabel(calendarType: CalendarType): String = if (calendarType == CalendarType.LUNAR) "음력" else "양력"

    private const val CHINESE_YEAR_OFFSET = 2637
}
