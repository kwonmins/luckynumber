package com.example.unum.domain

import android.icu.util.Calendar
import android.icu.util.ChineseCalendar
import android.icu.util.TimeZone as IcuTimeZone
import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.DestinyPolarity
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.GenderResonance
import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.NumerologyNumbers
import java.util.GregorianCalendar
import java.util.TimeZone as JavaTimeZone

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
        return toBirthInput(
            calendarType = form.calendarType,
            yearText = form.year,
            monthText = form.month,
            dayText = form.day,
            gender = form.gender
        )
    }

    fun toBirthInput(
        calendarType: CalendarType,
        yearText: String,
        monthText: String,
        dayText: String,
        gender: GenderOption
    ): BirthInput? {
        val year = yearText.toIntOrNull() ?: return null
        val month = monthText.toIntOrNull() ?: return null
        val day = dayText.toIntOrNull() ?: return null
        return when (calendarType) {
            CalendarType.SOLAR -> {
                if (!isValidSolarDate(year, month, day)) return null
                BirthInput(CalendarType.SOLAR, year, month, day, gender)
            }
            CalendarType.LUNAR -> {
                if (!isValidLunarDate(year, month, day, gender)) return null
                BirthInput(CalendarType.LUNAR, year, month, day, gender)
            }
        }
    }

    fun toLunarBirthInput(solarInput: BirthInput): BirthInput {
        require(solarInput.calendarType == CalendarType.SOLAR) { "양력 입력만 음력으로 변환할 수 있습니다." }
        val gregorian = GregorianCalendar(JavaTimeZone.getTimeZone("Asia/Seoul")).apply {
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

    fun toSolarBirthInput(lunarInput: BirthInput): BirthInput {
        require(lunarInput.calendarType == CalendarType.LUNAR) { "음력 입력만 양력으로 변환할 수 있습니다." }

        val lunar = ChineseCalendar(IcuTimeZone.getTimeZone("Asia/Seoul")).apply {
            isLenient = false
            clear()
            set(Calendar.EXTENDED_YEAR, lunarInput.year + CHINESE_YEAR_OFFSET)
            set(Calendar.MONTH, lunarInput.month - 1)
            set(Calendar.IS_LEAP_MONTH, 0)
            set(Calendar.DAY_OF_MONTH, lunarInput.day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val solar = GregorianCalendar(JavaTimeZone.getTimeZone("Asia/Seoul")).apply {
            isLenient = false
            time = lunar.time
        }

        return BirthInput(
            calendarType = CalendarType.SOLAR,
            year = solar.get(GregorianCalendar.YEAR),
            month = solar.get(GregorianCalendar.MONTH) + 1,
            day = solar.get(GregorianCalendar.DAY_OF_MONTH),
            gender = lunarInput.gender
        )
    }

    private fun isValidSolarDate(year: Int, month: Int, day: Int): Boolean {
        return runCatching {
            GregorianCalendar(JavaTimeZone.getTimeZone("Asia/Seoul")).apply {
                isLenient = false
                set(year, month - 1, day, 12, 0, 0)
                set(GregorianCalendar.MILLISECOND, 0)
                time
            }
        }.isSuccess
    }

    private fun isValidLunarDate(year: Int, month: Int, day: Int, gender: GenderOption): Boolean {
        return runCatching {
            toSolarBirthInput(
                BirthInput(
                    calendarType = CalendarType.LUNAR,
                    year = year,
                    month = month,
                    day = day,
                    gender = gender
                )
            )
        }.isSuccess
    }

    fun formatDate(year: Int, month: Int, day: Int): String = "%04d.%02d.%02d".format(year, month, day)
    fun calendarLabel(calendarType: CalendarType): String = if (calendarType == CalendarType.LUNAR) "음력" else "양력"

    fun destinyPolarity(destiny: Int): DestinyPolarity = when (destiny) {
        1, 3, 5, 7, 9 -> DestinyPolarity.YANG
        2, 4, 6, 8 -> DestinyPolarity.YIN
        else -> DestinyPolarity.NEUTRAL
    }

    fun genderResonance(gender: GenderOption, destiny: Int): GenderResonance {
        val polarity = destinyPolarity(destiny)
        return when {
            polarity == DestinyPolarity.NEUTRAL || gender == GenderOption.NONE -> GenderResonance.NEUTRAL
            gender == GenderOption.MALE && polarity == DestinyPolarity.YANG -> GenderResonance.AMPLIFIED
            gender == GenderOption.FEMALE && polarity == DestinyPolarity.YIN -> GenderResonance.AMPLIFIED
            else -> GenderResonance.BALANCED
        }
    }

    fun genderResonanceDescription(gender: GenderOption, destiny: Int): String {
        val polarity = destinyPolarity(destiny)
        return when (genderResonance(gender, destiny)) {
            GenderResonance.AMPLIFIED -> when (gender) {
                GenderOption.MALE -> "남성의 흐름과 운명수 ${destiny}의 ${polarity.label} 기운이 맞물려 추진력과 결단력이 더 강하게 드러납니다."
                GenderOption.FEMALE -> "여성의 흐름과 운명수 ${destiny}의 ${polarity.label} 기운이 맞물려 섬세함과 감정의 리듬이 더 선명하게 드러납니다."
                GenderOption.NONE -> "운명수의 기운이 비교적 또렷하게 드러나는 편입니다."
            }
            GenderResonance.BALANCED -> when (gender) {
                GenderOption.MALE -> "남성의 흐름 안에서 운명수 ${destiny}의 ${polarity.label} 기운이 부드럽게 조율되며 드러납니다."
                GenderOption.FEMALE -> "여성의 흐름 안에서 운명수 ${destiny}의 ${polarity.label} 기운이 균형을 잡으며 드러납니다."
                GenderOption.NONE -> "운명수의 기운이 한쪽으로 치우치지 않고 드러나는 편입니다."
            }
            GenderResonance.NEUTRAL -> "운명수 0은 비워두는 힘과 전환의 감각이 커서 성별보다 현재의 선택 방식이 더 크게 작용합니다."
        }
    }

    private const val CHINESE_YEAR_OFFSET = 2637
}
