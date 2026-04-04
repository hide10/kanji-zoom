package com.hide10.kanjizoom.stroke

import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.core.graphics.PathParser
import kotlinx.coroutines.delay
import kotlin.math.min

private data class ParsedStrokePath(
    val order: Int,
    val fullAndroidPath: AndroidPath,
    val fullComposePath: Path,
)

@Composable
fun StrokeOrderCanvas(
    strokeData: StrokeCharacterData,
    playbackVersion: Int,
    modifier: Modifier = Modifier,
) {
    val completedColor = MaterialTheme.colorScheme.onSurface
    val currentColor = MaterialTheme.colorScheme.primary
    val upcomingColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    val parsedPaths = remember(strokeData) { parseStrokePaths(strokeData) }
    val strokeProgress = remember { Animatable(0f) }
    var currentStrokeIndex by remember { mutableIntStateOf(-1) }
    var completedStrokeCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(strokeData.character, playbackVersion) {
        currentStrokeIndex = -1
        completedStrokeCount = 0
        strokeProgress.snapTo(0f)

        if (playbackVersion <= 0) {
            return@LaunchedEffect
        }

        for (index in parsedPaths.indices) {
            currentStrokeIndex = index
            strokeProgress.snapTo(0f)
            strokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = LinearEasing),
            )
            completedStrokeCount = index + 1
            delay(140L)
        }

        currentStrokeIndex = -1
        strokeProgress.snapTo(0f)
    }

    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        if (parsedPaths.isEmpty()) {
            return@Canvas
        }

        val scale = min(
            size.width / strokeData.viewBoxWidth,
            size.height / strokeData.viewBoxHeight,
        ) * 0.86f
        val contentWidth = strokeData.viewBoxWidth * scale
        val contentHeight = strokeData.viewBoxHeight * scale
        val translateX = (size.width - contentWidth) / 2f
        val translateY = (size.height - contentHeight) / 2f

        withTransform({
            translate(left = translateX, top = translateY)
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            val strokeStyle = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)

            parsedPaths.forEachIndexed { index, parsedPath ->
                when {
                    index < completedStrokeCount -> {
                        drawPath(
                            path = parsedPath.fullComposePath,
                            color = completedColor,
                            style = strokeStyle,
                        )
                    }

                    index == currentStrokeIndex -> {
                        drawPath(
                            path = parsedPath.fullComposePath,
                            color = upcomingColor,
                            style = strokeStyle,
                        )
                        buildSegmentPath(parsedPath, strokeProgress.value)?.let { segment ->
                            drawPath(
                                path = segment,
                                color = currentColor,
                                style = strokeStyle,
                            )
                        }
                    }

                    else -> {
                        drawPath(
                            path = parsedPath.fullComposePath,
                            color = upcomingColor,
                            style = strokeStyle,
                        )
                    }
                }
            }
        }
    }
}

private fun parseStrokePaths(strokeData: StrokeCharacterData): List<ParsedStrokePath> =
    strokeData.strokes.mapNotNull { stroke ->
        val androidPath = runCatching {
            PathParser.createPathFromPathData(stroke.pathData)
        }.getOrNull() ?: return@mapNotNull null

        ParsedStrokePath(
            order = stroke.order,
            fullAndroidPath = androidPath,
            fullComposePath = AndroidPath(androidPath).asComposePath(),
        )
    }.sortedBy(ParsedStrokePath::order)

private fun buildSegmentPath(parsedStrokePath: ParsedStrokePath, progress: Float): Path? {
    if (progress <= 0f) {
        return null
    }

    if (progress >= 1f) {
        return parsedStrokePath.fullComposePath
    }

    val pathMeasure = PathMeasure(parsedStrokePath.fullAndroidPath, false)
    val pathLength = pathMeasure.length
    if (pathLength <= 0f) {
        return parsedStrokePath.fullComposePath
    }

    val segmentPath = AndroidPath()
    pathMeasure.getSegment(0f, pathLength * progress.coerceIn(0f, 1f), segmentPath, true)
    return segmentPath.asComposePath()
}
