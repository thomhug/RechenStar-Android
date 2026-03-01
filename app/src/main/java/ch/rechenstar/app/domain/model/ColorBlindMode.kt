package ch.rechenstar.app.domain.model

enum class ColorBlindMode(val rawValue: String) {
    NONE("none"),
    PROTANOPIA("protanopia"),
    DEUTERANOPIA("deuteranopia"),
    TRITANOPIA("tritanopia");

    val label: String
        get() = when (this) {
            NONE -> "Keine"
            PROTANOPIA -> "Protanopie (Rot-Blind)"
            DEUTERANOPIA -> "Deuteranopie (Grün-Blind)"
            TRITANOPIA -> "Tritanopie (Blau-Blind)"
        }

    companion object {
        fun fromRawValue(raw: String): ColorBlindMode =
            entries.find { it.rawValue == raw } ?: NONE
    }
}
