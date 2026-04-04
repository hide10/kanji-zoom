package com.hide10.kanjizoom.stroke

import org.w3c.dom.Element
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

object StrokeDataParser {
    fun parseSvgOrNull(svgContent: String, character: String): StrokeCharacterData? =
        runCatching { parseSvg(svgContent, character) }.getOrNull()

    fun parseSvg(svgContent: String, character: String): StrokeCharacterData {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isXIncludeAware = false
            setExpandEntityReferences(false)
        }
        configureSecureXmlParsing(factory)
        val document = factory.newDocumentBuilder().apply {
            setEntityResolver(EntityResolver { _, _ -> InputSource(StringReader("")) })
        }.parse(InputSource(StringReader(svgContent)))
        val root = document.documentElement ?: error("Missing svg root element")

        val viewBoxValues = root.getAttribute("viewBox")
            .trim()
            .split(Regex("\\s+"))
            .map(String::toFloat)
        require(viewBoxValues.size == 4) { "Unexpected viewBox format" }

        val pathNodes = root.getElementsByTagName("path")
        val strokes = buildList {
            for (index in 0 until pathNodes.length) {
                val node = pathNodes.item(index) as? Element ?: continue
                val pathData = node.getAttribute("d").trim()
                if (pathData.isNotEmpty()) {
                    add(StrokePathData(order = size + 1, pathData = pathData))
                }
            }
        }

        require(strokes.isNotEmpty()) { "No stroke paths found" }

        return StrokeCharacterData(
            character = character,
            viewBoxWidth = viewBoxValues[2],
            viewBoxHeight = viewBoxValues[3],
            strokes = strokes,
        )
    }

    private fun configureSecureXmlParsing(factory: DocumentBuilderFactory) {
        setFeatureIfSupported(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true)
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false)
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false)
        setFeatureIfSupported(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    }

    private fun setFeatureIfSupported(
        factory: DocumentBuilderFactory,
        feature: String,
        enabled: Boolean,
    ) {
        runCatching {
            factory.setFeature(feature, enabled)
        }
    }
}
