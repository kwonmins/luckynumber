package com.example.unum.data.model

fun CompatibilityRelationshipStatus.compatibilityCoverTheme(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> BookSpecs.forStatus(this).themeId.key
    CompatibilityRelationshipStatus.CRUSH -> BookSpecs.forStatus(this).themeId.key
    CompatibilityRelationshipStatus.REUNION -> BookSpecs.forStatus(this).themeId.key
}

fun CompatibilityRelationshipStatus.compatibilityCoverTitle(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> BookSpecs.forStatus(this).coverTitle
    CompatibilityRelationshipStatus.CRUSH -> BookSpecs.forStatus(this).coverTitle
    CompatibilityRelationshipStatus.REUNION -> BookSpecs.forStatus(this).coverTitle
}

fun CompatibilityRelationshipStatus.compatibilityKicker(): String = when (this) {
    CompatibilityRelationshipStatus.COUPLE -> BookSpecs.forStatus(this).coverKicker
    CompatibilityRelationshipStatus.CRUSH -> BookSpecs.forStatus(this).coverKicker
    CompatibilityRelationshipStatus.REUNION -> BookSpecs.forStatus(this).coverKicker
}

fun compatibilityStatusFromThemeOrText(theme: String, text: String): CompatibilityRelationshipStatus? {
    return when (BookThemeId.fromThemeOrText(theme, text, FortuneBookType.COMPATIBILITY)) {
        BookThemeId.COMPATIBILITY_COUPLE -> CompatibilityRelationshipStatus.COUPLE
        BookThemeId.COMPATIBILITY_CRUSH -> CompatibilityRelationshipStatus.CRUSH
        BookThemeId.COMPATIBILITY_REUNION -> CompatibilityRelationshipStatus.REUNION
        else -> null
    }
}
