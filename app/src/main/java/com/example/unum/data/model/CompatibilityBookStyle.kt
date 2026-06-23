package com.example.unum.data.model

fun CompatibilityRelationshipStatus.compatibilityCoverTheme(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> "compatibility_couple"
    CompatibilityRelationshipStatus.CRUSH -> "compatibility_crush"
    CompatibilityRelationshipStatus.REUNION -> "compatibility_reunion"
}

fun CompatibilityRelationshipStatus.compatibilityCoverTitle(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> "커플 운세노트"
    CompatibilityRelationshipStatus.CRUSH -> "짝사랑 운세노트"
    CompatibilityRelationshipStatus.REUNION -> "재회 운세노트"
}

fun CompatibilityRelationshipStatus.compatibilityKicker(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> "PREMIUM COUPLE NOTE"
    CompatibilityRelationshipStatus.CRUSH -> "PREMIUM CRUSH NOTE"
    CompatibilityRelationshipStatus.REUNION -> "PREMIUM REUNION NOTE"
}

fun compatibilityStatusFromThemeOrText(theme: String, text: String): CompatibilityRelationshipStatus? {
    val key = "$theme $text".lowercase()
    return when {
        "compatibility_couple" in key || "커플" in key || "couple" in key ->
            CompatibilityRelationshipStatus.COUPLE
        "compatibility_crush" in key || "짝사랑" in key || "crush" in key ->
            CompatibilityRelationshipStatus.CRUSH
        "compatibility_reunion" in key || "재회" in key || "reunion" in key ->
            CompatibilityRelationshipStatus.REUNION
        else -> null
    }
}
