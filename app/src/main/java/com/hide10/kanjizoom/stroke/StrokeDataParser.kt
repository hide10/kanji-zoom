package com.hide10.kanjizoom.stroke

object StrokeDataParser {
    private val viewBoxRegex = Regex(
        pattern = """<svg\b[^>]*\bviewBox\s*=\s*(['"])(.*?)\1""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val pathRegex = Regex(
        pattern = """<path\b[^>]*\bd\s*=\s*(['"])(.*?)\1[^>]*?/?>""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )

    fun parseSvgOrNull(svgContent: String, character: String): StrokeCharacterData? =
        runCatching { parseSvg(svgContent, character) }.getOrNull()

    fun parseSvg(svgContent: String, character: String): StrokeCharacterData {
        val viewBoxValues = requireNotNull(viewBoxRegex.find(svgContent)?.groupValues?.getOrNull(2)) {
            "Missing viewBox attribute"
        }
            .trim()
            .split(Regex("\\s+"))
            .map(String::toFloat)
        require(viewBoxValues.size == 4) { "Unexpected viewBox format" }

        val strokes = pathRegex.findAll(svgContent)
            .mapNotNull { match ->
                val pathData = decodeXmlAttribute(match.groupValues[2].trim())
                if (pathData.isEmpty()) {
                    null
                } else {
                    StrokePathData(order = 0, pathData = pathData)
                }
            }
            .mapIndexed { index, stroke ->
                stroke.copy(order = index + 1)
            }
            .toList()

        require(strokes.isNotEmpty()) { "No stroke paths found" }

        return StrokeCharacterData(
            character = character,
            viewBoxWidth = viewBoxValues[2],
            viewBoxHeight = viewBoxValues[3],
            strokes = strokes,
        )
    }

    private fun decodeXmlAttribute(value: String): String = value
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
}
