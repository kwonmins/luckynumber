package com.example.unum.data.model

enum class BookThemeId(val key: String) {
    ROMANCE("romance"),
    CAREER("career"),
    MONEY("money"),
    SELF_ESTEEM("self_esteem"),
    RELATIONSHIP("relationship"),
    COMPATIBILITY("compatibility"),
    COMPATIBILITY_COUPLE("compatibility_couple"),
    COMPATIBILITY_CRUSH("compatibility_crush"),
    COMPATIBILITY_REUNION("compatibility_reunion"),
    CALM("calm");

    val isCompatibility: Boolean
        get() = this == COMPATIBILITY ||
            this == COMPATIBILITY_COUPLE ||
            this == COMPATIBILITY_CRUSH ||
            this == COMPATIBILITY_REUNION

    companion object {
        fun fromKey(key: String): BookThemeId? {
            val normalized = key.trim().lowercase()
            return entries.firstOrNull { it.key == normalized } ?: when (normalized) {
                "soft" -> CALM
                "self", "self-esteem" -> SELF_ESTEEM
                "couple" -> COMPATIBILITY_COUPLE
                "crush" -> COMPATIBILITY_CRUSH
                "reunion" -> COMPATIBILITY_REUNION
                else -> null
            }
        }

        fun fromThemeOrText(
            theme: String,
            text: String,
            bookType: FortuneBookType = FortuneBookType.PERSONAL
        ): BookThemeId {
            val directTheme = fromKey(theme)
            if (directTheme != null && directTheme != COMPATIBILITY && directTheme != CALM) {
                return directTheme
            }

            val searchable = "$theme $text".lowercase()
            return when {
                "compatibility_couple" in searchable || "커플" in searchable || "couple" in searchable ->
                    COMPATIBILITY_COUPLE
                "compatibility_crush" in searchable || "짝사랑" in searchable || "crush" in searchable ->
                    COMPATIBILITY_CRUSH
                "compatibility_reunion" in searchable || "재회" in searchable || "reunion" in searchable ->
                    COMPATIBILITY_REUNION
                "compatibility" in searchable || "궁합" in searchable || bookType == FortuneBookType.COMPATIBILITY ->
                    COMPATIBILITY
                "career" in searchable || "진로" in searchable || "일" in searchable ->
                    CAREER
                "money" in searchable || "돈" in searchable || "금전" in searchable || "경제" in searchable ->
                    MONEY
                "self_esteem" in searchable || "나 자신" in searchable || "자아" in searchable || "자기" in searchable ->
                    SELF_ESTEEM
                "relationship" in searchable || "인간관계" in searchable ->
                    RELATIONSHIP
                "romance" in searchable || "연애" in searchable || "사랑" in searchable ->
                    ROMANCE
                directTheme != null -> directTheme
                else -> if (bookType == FortuneBookType.COMPATIBILITY) COMPATIBILITY else ROMANCE
            }
        }
    }
}

data class BookThemeSpec(
    val id: BookThemeId,
    val displayName: String,
    val moodLabel: String,
    val symbol: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val archiveCoverStartColor: Long,
    val archiveCoverEndColor: Long,
    val archiveGlowColor: Long,
    val readerAccentColor: Long,
    val readerAccentDeepColor: Long,
    val readerNoteAccentColor: Long,
    val readerRibbonColor: Long,
    val readerFoilColor: Long,
    val readerStitchColor: Long,
    val readerLeatherTopColor: Long,
    val readerLeatherMidColor: Long,
    val readerLeatherBottomColor: Long,
    val readerCoverTopColor: Long,
    val readerCoverMidColor: Long,
    val readerCoverBottomColor: Long,
    val readerTintColor: Long,
    val readerPageColor: Long,
    val readerPageTopColor: Long,
    val readerEdgeColor: Long,
    val readerOverviewPaperColor: Long,
    val pdfAccentColor: Int = readerAccentColor.toInt(),
    val pdfAccentDarkColor: Int = readerAccentDeepColor.toInt(),
    val pdfRibbonColor: Int = readerRibbonColor.toInt(),
    val pdfFoilColor: Int = readerFoilColor.toInt(),
    val pdfStitchColor: Int = readerStitchColor.toInt(),
    val pdfCoverTopColor: Int = readerCoverTopColor.toInt(),
    val pdfCoverMidColor: Int = readerCoverMidColor.toInt(),
    val pdfCoverBottomColor: Int = readerCoverBottomColor.toInt(),
    val pdfTintColor: Int = readerTintColor.toInt(),
    val pdfPageColor: Int = readerPageColor.toInt(),
    val pdfPageTopColor: Int = readerPageTopColor.toInt(),
    val pdfEdgeColor: Int = readerEdgeColor.toInt()
)

object BookThemeSpecs {
    private val specs: Map<BookThemeId, BookThemeSpec> = listOf(
        BookThemeSpec(
            id = BookThemeId.ROMANCE,
            displayName = "연애 운세",
            moodLabel = "따뜻한 연애 리딩",
            symbol = "♡",
            primaryColor = 0xFFDC2626,
            secondaryColor = 0xFF991B1B,
            archiveCoverStartColor = 0xFF1C1018,
            archiveCoverEndColor = 0xFF2A1520,
            archiveGlowColor = 0x478B3A5A,
            readerAccentColor = 0xFFDC2626,
            readerAccentDeepColor = 0xFF991B1B,
            readerNoteAccentColor = 0xFFBB6680,
            readerRibbonColor = 0xFF8B1A2E,
            readerFoilColor = 0xFFF2C4A0,
            readerStitchColor = 0xFFFDE68A,
            readerLeatherTopColor = 0xFF4A1528,
            readerLeatherMidColor = 0xFF280B17,
            readerLeatherBottomColor = 0xFF0F0308,
            readerCoverTopColor = 0xFF222633,
            readerCoverMidColor = 0xFF10131B,
            readerCoverBottomColor = 0xFF05070C,
            readerTintColor = 0xFFFFF1F2,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA,
            pdfRibbonColor = 0xFFB91C1C.toInt(),
            pdfFoilColor = 0xFFF7D56A.toInt()
        ),
        BookThemeSpec(
            id = BookThemeId.CAREER,
            displayName = "일과 방향",
            moodLabel = "차분한 방향 정리",
            symbol = "▦",
            primaryColor = 0xFF2563EB,
            secondaryColor = 0xFF1E3A8A,
            archiveCoverStartColor = 0xFF0F131C,
            archiveCoverEndColor = 0xFF172030,
            archiveGlowColor = 0x473A6EBB,
            readerAccentColor = 0xFF2563EB,
            readerAccentDeepColor = 0xFF1E3A8A,
            readerNoteAccentColor = 0xFF5A8EC4,
            readerRibbonColor = 0xFF1A3A6B,
            readerFoilColor = 0xFFB8D4F5,
            readerStitchColor = 0xFFFDE68A,
            readerLeatherTopColor = 0xFF0D1E3A,
            readerLeatherMidColor = 0xFF060F1E,
            readerLeatherBottomColor = 0xFF020508,
            readerCoverTopColor = 0xFF222633,
            readerCoverMidColor = 0xFF10131B,
            readerCoverBottomColor = 0xFF05070C,
            readerTintColor = 0xFFEFF6FF,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA,
            pdfRibbonColor = 0xFF2563EB.toInt(),
            pdfFoilColor = 0xFFF7D56A.toInt()
        ),
        BookThemeSpec(
            id = BookThemeId.MONEY,
            displayName = "돈의 흐름",
            moodLabel = "단정한 자산 리딩",
            symbol = "₩",
            primaryColor = 0xFF059669,
            secondaryColor = 0xFF065F46,
            archiveCoverStartColor = 0xFF0D1910,
            archiveCoverEndColor = 0xFF112318,
            archiveGlowColor = 0x472A7A52,
            readerAccentColor = 0xFF059669,
            readerAccentDeepColor = 0xFF065F46,
            readerNoteAccentColor = 0xFF4AAA7A,
            readerRibbonColor = 0xFF0D4028,
            readerFoilColor = 0xFFA8E6C4,
            readerStitchColor = 0xFFFDE68A,
            readerLeatherTopColor = 0xFF0E3D2C,
            readerLeatherMidColor = 0xFF072418,
            readerLeatherBottomColor = 0xFF020F09,
            readerCoverTopColor = 0xFF1F5B4C,
            readerCoverMidColor = 0xFF0E332C,
            readerCoverBottomColor = 0xFF061C18,
            readerTintColor = 0xFFECFDF5,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA,
            pdfRibbonColor = 0xFF10B981.toInt(),
            pdfFoilColor = 0xFFF7D56A.toInt()
        ),
        BookThemeSpec(
            id = BookThemeId.SELF_ESTEEM,
            displayName = "자기 기준",
            moodLabel = "깊은 자기 리딩",
            symbol = "✦",
            primaryColor = 0xFF7C3AED,
            secondaryColor = 0xFF4C1D95,
            archiveCoverStartColor = 0xFF191208,
            archiveCoverEndColor = 0xFF241A0A,
            archiveGlowColor = 0x38D4A84B,
            readerAccentColor = 0xFF7C3AED,
            readerAccentDeepColor = 0xFF4C1D95,
            readerNoteAccentColor = 0xFFD4A84B,
            readerRibbonColor = 0xFF3A1565,
            readerFoilColor = 0xFFF7D56A,
            readerStitchColor = 0xFFFDE68A,
            readerLeatherTopColor = 0xFF1A1625,
            readerLeatherMidColor = 0xFF0D0B17,
            readerLeatherBottomColor = 0xFF04030A,
            readerCoverTopColor = 0xFF171A2A,
            readerCoverMidColor = 0xFF101225,
            readerCoverBottomColor = 0xFF070813,
            readerTintColor = 0xFFF5F3FF,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA,
            pdfRibbonColor = 0xFF7C3AED.toInt()
        ),
        BookThemeSpec(
            id = BookThemeId.RELATIONSHIP,
            displayName = "관계 패턴",
            moodLabel = "차분한 관계 리딩",
            symbol = "◇",
            primaryColor = 0xFFA16207,
            secondaryColor = 0xFF713F12,
            archiveCoverStartColor = 0xFF1C1018,
            archiveCoverEndColor = 0xFF2A1520,
            archiveGlowColor = 0x429B5C3A,
            readerAccentColor = 0xFFA16207,
            readerAccentDeepColor = 0xFF713F12,
            readerNoteAccentColor = 0xFFBB7755,
            readerRibbonColor = 0xFF7A5010,
            readerFoilColor = 0xFFF0C080,
            readerStitchColor = 0xFFF8E3B0,
            readerLeatherTopColor = 0xFF6B3519,
            readerLeatherMidColor = 0xFF3E1B08,
            readerLeatherBottomColor = 0xFF190A02,
            readerCoverTopColor = 0xFF9C5D32,
            readerCoverMidColor = 0xFF673719,
            readerCoverBottomColor = 0xFF30170C,
            readerTintColor = 0xFFFFF7ED,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA,
            pdfRibbonColor = 0xFFF59E0B.toInt(),
            pdfFoilColor = 0xFFF8E3A3.toInt()
        ),
        BookThemeSpec(
            id = BookThemeId.COMPATIBILITY,
            displayName = "궁합노트",
            moodLabel = "핑크빛 궁합 리딩",
            symbol = "∞",
            primaryColor = 0xFFB85AC7,
            secondaryColor = 0xFF7E2E84,
            archiveCoverStartColor = 0xFF2A1232,
            archiveCoverEndColor = 0xFF442044,
            archiveGlowColor = 0x3DF0ABFC,
            readerAccentColor = 0xFFB85AC7,
            readerAccentDeepColor = 0xFF7E2E84,
            readerNoteAccentColor = 0xFFB85AC7,
            readerRibbonColor = 0xFF5EEAD4,
            readerFoilColor = 0xFFF0ABFC,
            readerStitchColor = 0xFFF5D0FE,
            readerLeatherTopColor = 0xFF4A1B4E,
            readerLeatherMidColor = 0xFF2B0F35,
            readerLeatherBottomColor = 0xFF100517,
            readerCoverTopColor = 0xFF4A1B4E,
            readerCoverMidColor = 0xFF2B0F35,
            readerCoverBottomColor = 0xFF100517,
            readerTintColor = 0xFFFDF2F8,
            readerPageColor = 0xFFFFF7FB,
            readerPageTopColor = 0xFFFFECF5,
            readerEdgeColor = 0xFFE8CAD8,
            readerOverviewPaperColor = 0xFFFFF1F8
        ),
        BookThemeSpec(
            id = BookThemeId.COMPATIBILITY_COUPLE,
            displayName = "커플 운세노트",
            moodLabel = "안정적인 커플 리딩",
            symbol = "♡",
            primaryColor = 0xFF0F8A8A,
            secondaryColor = 0xFF075E5F,
            archiveCoverStartColor = 0xFF062F35,
            archiveCoverEndColor = 0xFF083F43,
            archiveGlowColor = 0x422DD4BF,
            readerAccentColor = 0xFF0F8A8A,
            readerAccentDeepColor = 0xFF075E5F,
            readerNoteAccentColor = 0xFF0F8A8A,
            readerRibbonColor = 0xFFF0B94F,
            readerFoilColor = 0xFF8EE7D6,
            readerStitchColor = 0xFFB6F4E8,
            readerLeatherTopColor = 0xFF075E5F,
            readerLeatherMidColor = 0xFF044043,
            readerLeatherBottomColor = 0xFF011E22,
            readerCoverTopColor = 0xFF075E5F,
            readerCoverMidColor = 0xFF044043,
            readerCoverBottomColor = 0xFF011E22,
            readerTintColor = 0xFFF0FDFA,
            readerPageColor = 0xFFFFF7FB,
            readerPageTopColor = 0xFFFFECF5,
            readerEdgeColor = 0xFFE8CAD8,
            readerOverviewPaperColor = 0xFFF0FDFA
        ),
        BookThemeSpec(
            id = BookThemeId.COMPATIBILITY_CRUSH,
            displayName = "짝사랑 운세노트",
            moodLabel = "설렘과 거리의 리딩",
            symbol = "✦",
            primaryColor = 0xFF7C6DE8,
            secondaryColor = 0xFF4338CA,
            archiveCoverStartColor = 0xFF11183A,
            archiveCoverEndColor = 0xFF1D2756,
            archiveGlowColor = 0x42A78BFA,
            readerAccentColor = 0xFF7C6DE8,
            readerAccentDeepColor = 0xFF4338CA,
            readerNoteAccentColor = 0xFF7C6DE8,
            readerRibbonColor = 0xFFA78BFA,
            readerFoilColor = 0xFFC4B5FD,
            readerStitchColor = 0xFFE9D5FF,
            readerLeatherTopColor = 0xFF26305F,
            readerLeatherMidColor = 0xFF151B3C,
            readerLeatherBottomColor = 0xFF080B1E,
            readerCoverTopColor = 0xFF26305F,
            readerCoverMidColor = 0xFF151B3C,
            readerCoverBottomColor = 0xFF080B1E,
            readerTintColor = 0xFFF5F3FF,
            readerPageColor = 0xFFFFF7FB,
            readerPageTopColor = 0xFFFFECF5,
            readerEdgeColor = 0xFFE8CAD8,
            readerOverviewPaperColor = 0xFFF5F3FF
        ),
        BookThemeSpec(
            id = BookThemeId.COMPATIBILITY_REUNION,
            displayName = "재회 운세노트",
            moodLabel = "여운이 남는 재회 리딩",
            symbol = "⌂",
            primaryColor = 0xFFC46A2A,
            secondaryColor = 0xFF7C2D12,
            archiveCoverStartColor = 0xFF3A1609,
            archiveCoverEndColor = 0xFF6B2D14,
            archiveGlowColor = 0x3DF59E0B,
            readerAccentColor = 0xFFC46A2A,
            readerAccentDeepColor = 0xFF7C2D12,
            readerNoteAccentColor = 0xFFC46A2A,
            readerRibbonColor = 0xFFF59E0B,
            readerFoilColor = 0xFFFDBA74,
            readerStitchColor = 0xFFFED7AA,
            readerLeatherTopColor = 0xFF7A321A,
            readerLeatherMidColor = 0xFF431508,
            readerLeatherBottomColor = 0xFF190602,
            readerCoverTopColor = 0xFF7A321A,
            readerCoverMidColor = 0xFF431508,
            readerCoverBottomColor = 0xFF190602,
            readerTintColor = 0xFFFFF4E8,
            readerPageColor = 0xFFFFF7FB,
            readerPageTopColor = 0xFFFFECF5,
            readerEdgeColor = 0xFFE8CAD8,
            readerOverviewPaperColor = 0xFFFFF4E8
        ),
        BookThemeSpec(
            id = BookThemeId.CALM,
            displayName = "프리미엄 노트",
            moodLabel = "차분한 기본 리딩",
            symbol = "∞",
            primaryColor = 0xFFD4A84B,
            secondaryColor = 0xFFA67C35,
            archiveCoverStartColor = 0xFF0C0B0F,
            archiveCoverEndColor = 0xFF13100F,
            archiveGlowColor = 0x1AFFFFFF,
            readerAccentColor = 0xFFD4A84B,
            readerAccentDeepColor = 0xFFA67C35,
            readerNoteAccentColor = 0xFFD4A84B,
            readerRibbonColor = 0xFF5A3A10,
            readerFoilColor = 0xFFD4A84B,
            readerStitchColor = 0xFFFDE68A,
            readerLeatherTopColor = 0xFF151017,
            readerLeatherMidColor = 0xFF0A0810,
            readerLeatherBottomColor = 0xFF030205,
            readerCoverTopColor = 0xFF151017,
            readerCoverMidColor = 0xFF0A0810,
            readerCoverBottomColor = 0xFF030205,
            readerTintColor = 0xFFFFF1F2,
            readerPageColor = 0xFFFFFDF8,
            readerPageTopColor = 0xFFFFFAF1,
            readerEdgeColor = 0xFFE6DAC9,
            readerOverviewPaperColor = 0xFFFBF6EA
        )
    ).associateBy { it.id }

    fun get(id: BookThemeId): BookThemeSpec =
        specs[id] ?: specs.getValue(BookThemeId.CALM)
}
