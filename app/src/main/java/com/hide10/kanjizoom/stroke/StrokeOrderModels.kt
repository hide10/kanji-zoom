package com.hide10.kanjizoom.stroke

data class StrokeCharacterData(
    val character: String,
    val viewBoxWidth: Float,
    val viewBoxHeight: Float,
    val strokes: List<StrokePathData>,
)

data class StrokePathData(
    val order: Int,
    val pathData: String,
)

enum class DisplayMode {
    Normal,
    StrokeOrder,
}

enum class StrokeFallbackReason {
    EmptyInput,
    UnsupportedCharacterType,
    MissingStrokeData,
    ParseFailed,
}

fun firstCodePointOrNull(text: String): String? {
    if (text.isEmpty()) {
        return null
    }

    val endIndex = text.offsetByCodePoints(0, 1)
    return text.substring(0, endIndex)
}

fun String.toCodePointStrings(): List<String> {
    if (isEmpty()) {
        return emptyList()
    }

    val characters = mutableListOf<String>()
    var index = 0
    while (index < length) {
        val nextIndex = offsetByCodePoints(index, 1)
        characters += substring(index, nextIndex)
        index = nextIndex
    }
    return characters
}

fun String.toCodePointHex(): String {
    val codePoint = codePointAt(0)
    return codePoint.toString(16).padStart(5, '0')
}

fun String.isHanScriptCharacter(): Boolean {
    val codePoint = codePointAt(0)
    return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN
}
