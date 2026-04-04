package com.hide10.kanjizoom.stroke

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StrokeDataParserTest {
    @Test
    fun `parse svg extracts viewBox and stroke paths`() {
        val parsed = StrokeDataParser.parseSvg(
            svgContent = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 109 109">
                  <g id="kvg:StrokePaths_06f22">
                    <path d="M10,10 L20,20" />
                    <path d="M30,30 L40,40" />
                  </g>
                </svg>
            """.trimIndent(),
            character = "漢",
        )

        assertEquals("漢", parsed.character)
        assertEquals(109f, parsed.viewBoxWidth)
        assertEquals(109f, parsed.viewBoxHeight)
        assertEquals(2, parsed.strokes.size)
        assertEquals("M10,10 L20,20", parsed.strokes[0].pathData)
        assertEquals("M30,30 L40,40", parsed.strokes[1].pathData)
    }

    @Test
    fun `parse svg ignores external doctype references`() {
        val parsed = StrokeDataParser.parseSvg(
            svgContent = """
                <!DOCTYPE svg SYSTEM "file:///missing-svg10.dtd">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 109 109">
                  <g>
                    <path d="M10,10 L20,20" />
                  </g>
                </svg>
            """.trimIndent(),
            character = "漢",
        )

        assertEquals("漢", parsed.character)
        assertEquals(1, parsed.strokes.size)
        assertEquals("M10,10 L20,20", parsed.strokes[0].pathData)
    }

    @Test
    fun `parse svg returns null for malformed input`() {
        val parsed = StrokeDataParser.parseSvgOrNull(
            svgContent = "<svg><path></svg>",
            character = "漢",
        )

        assertNull(parsed)
    }
}
