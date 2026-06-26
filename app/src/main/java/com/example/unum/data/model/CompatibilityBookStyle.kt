package com.example.unum.data.model

fun CompatibilityRelationshipStatus.compatibilityCoverTheme(): String =
    BookSpecs.forStatus(this).themeId.key

fun CompatibilityRelationshipStatus.compatibilityCoverTitle(): String =
    BookSpecs.forStatus(this).coverTitle

fun CompatibilityRelationshipStatus.compatibilityKicker(): String =
    BookSpecs.forStatus(this).coverKicker

fun compatibilityStatusFromThemeOrText(theme: String, text: String): CompatibilityRelationshipStatus? {
    return when (BookThemeId.fromThemeOrText(theme, text, FortuneBookType.COMPATIBILITY)) {
        BookThemeId.COMPATIBILITY_COUPLE -> CompatibilityRelationshipStatus.COUPLE
        BookThemeId.COMPATIBILITY_CRUSH -> CompatibilityRelationshipStatus.CRUSH
        BookThemeId.COMPATIBILITY_REUNION -> CompatibilityRelationshipStatus.REUNION
        else -> null
    }
}
