package com.example.unum.data.model

enum class CoverSymbol { HEART, COMPASS, COINS, BOOK, LEAF, CASE, MOON, MIRROR, PEOPLE, RINGS, LETTER, BLOSSOM }

data class MoonlightCoverStyle(
    val id: String,
    val label: String,
    val accent: Int,
    val tint: Int,
    val symbol: CoverSymbol,
    val subtitle: String,
    val compatibility: Boolean = false
)

/** Resolve the category before legacy themes: several categories share a saved theme ID. */
object MoonlightCoverStyles {
    private fun style(id: String, label: String, accent: Long, tint: Long, symbol: CoverSymbol, subtitle: String, compatibility: Boolean = false) =
        MoonlightCoverStyle(id, label, accent.toInt(), tint.toInt(), symbol, subtitle, compatibility)

    val all = listOf(
        style("romance", "연애", 0xFF936379, 0xFFF6E6EC, CoverSymbol.HEART, "마음이 가까워지는 이야기"),
        style("career", "일과 진로", 0xFF567993, 0xFFE6EEF6, CoverSymbol.COMPASS, "내가 향하고 싶은 방향"),
        style("money", "재물", 0xFF5C7D6A, 0xFFE6F0E8, CoverSymbol.COINS, "차곡차곡 채우는 나의 일상"),
        style("study", "학업", 0xFF7976A5, 0xFFECEAFA, CoverSymbol.BOOK, "배움이 쌓이는 시간"),
        style("health", "건강", 0xFF78845C, 0xFFEEF0E3, CoverSymbol.LEAF, "몸과 마음을 돌보는 시간"),
        style("business", "사업", 0xFF977947, 0xFFF6EDDA, CoverSymbol.CASE, "가능성을 현실로 만드는 과정"),
        style("general", "종합운", 0xFF89729F, 0xFFEEE7F5, CoverSymbol.MOON, "일상 속 나의 가능성을 읽다"),
        style("self", "나 자신", 0xFFA17C61, 0xFFF5EADD, CoverSymbol.MIRROR, "나를 조금 더 알아가는 이야기"),
        style("relationship", "인간관계", 0xFF5D8187, 0xFFE5EFF0, CoverSymbol.PEOPLE, "서로를 이해하는 작은 실마리"),
        style("compatibility_couple", "커플", 0xFF967082, 0xFFF4E7EE, CoverSymbol.RINGS, "함께 쌓아가는 두 사람의 이야기", true),
        style("compatibility_crush", "짝사랑", 0xFFA57E7B, 0xFFF8EAE7, CoverSymbol.LETTER, "조심스럽게 전하고 싶은 마음", true),
        style("compatibility_reunion", "재회", 0xFF8F779C, 0xFFF0E9F5, CoverSymbol.BLOSSOM, "다시 마주할 때 필요한 이해", true)
    )

    fun forTopic(topic: PremiumTopic) = all.first { it.id == BookSpecs.forTopic(topic).id }

    fun forBook(book: FortuneBook?): MoonlightCoverStyle {
        if (book == null) return forTopic(PremiumTopic.GENERAL)
        val topic = book.concernTopic.trim()
        val specs = if (book.bookType == FortuneBookType.COMPATIBILITY) BookSpecs.compatibilitySpecs else BookSpecs.personalSpecs
        val exact = specs.firstOrNull { topic == it.label || topic == it.bookLabel || topic == it.coverTitle }
        // Prefer specific labels to generic words such as "일" or "궁합" in old records.
        val specific = specs.sortedByDescending { it.label.length }.firstOrNull { topic.contains(it.label) }
        val id = (exact ?: specific ?: BookSpecs.forBook(book)).id
        return all.firstOrNull { it.id == id } ?: forTopic(PremiumTopic.GENERAL)
    }
}
