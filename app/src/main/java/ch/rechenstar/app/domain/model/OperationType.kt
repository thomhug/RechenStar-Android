package ch.rechenstar.app.domain.model

enum class OperationType(val rawValue: String, val symbol: String) {
    ADDITION("plus", "+"),
    SUBTRACTION("minus", "-"),
    MULTIPLICATION("mal", "\u00D7");

    companion object {
        fun fromRawValue(raw: String): OperationType =
            entries.first { it.rawValue == raw }
    }
}
