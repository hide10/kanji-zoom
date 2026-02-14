package com.hide10.kanjizoom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hide10.kanjizoom.ui.theme.KanjiZoomTheme
import kotlin.math.ceil
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanjiZoomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KanjiZoomScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun KanjiZoomScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Zoom/pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 10f)
        if (scale > 1f) {
            offset += panChange
        } else {
            offset = Offset.Zero
        }
    }

    // Reset zoom when input changes
    LaunchedEffect(uiState.inputText) {
        scale = 1f
        offset = Offset.Zero
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = uiState.inputText,
            onValueChange = { viewModel.onInputChanged(it) },
            label = { Text("漢字を入力") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display area with auto-sizing and pinch zoom/pan
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .transformable(transformableState),
            contentAlignment = Alignment.Center,
        ) {
            val displayText = uiState.displayText
            if (displayText.isNotEmpty()) {
                val (autoSizeDp, cols) = calculateOptimalLayout(
                    charCount = displayText.length,
                    availableWidth = maxWidth,
                    availableHeight = maxHeight,
                )
                val density = LocalDensity.current
                val autoSizeSp = with(density) { (autoSizeDp.value / fontScale).sp }

                val lines = displayText.chunked(cols).joinToString("\n")

                Text(
                    text = lines,
                    fontSize = autoSizeSp,
                    fontFamily = uiState.fontType.toFontFamily(),
                    textAlign = TextAlign.Center,
                    lineHeight = autoSizeSp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FontType.entries.forEach { fontType ->
                FilterChip(
                    selected = uiState.fontType == fontType,
                    onClick = { viewModel.onFontSelected(fontType) },
                    label = { Text(fontType.label) },
                )
            }

            if (scale > 1f) {
                OutlinedButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                ) {
                    Text("リセット")
                }
            }
        }
    }
}

/**
 * Calculate the optimal font size (in Dp) and number of columns
 * to fill the available area with the given number of characters.
 */
private fun calculateOptimalLayout(
    charCount: Int,
    availableWidth: Dp,
    availableHeight: Dp,
): Pair<Dp, Int> {
    if (charCount <= 0) return Pair(0.dp, 1)

    val w = availableWidth.value
    val h = availableHeight.value
    var bestSize = 0f
    var bestCols = 1

    for (cols in 1..charCount) {
        val rows = ceil(charCount.toFloat() / cols).toInt()
        val sizeByWidth = w / cols
        val sizeByHeight = h / rows
        val size = min(sizeByWidth, sizeByHeight)
        if (size > bestSize) {
            bestSize = size
            bestCols = cols
        }
    }

    return Pair((bestSize * 0.9f).dp, bestCols)
}

private fun FontType.toFontFamily(): FontFamily = when (this) {
    FontType.SERIF -> FontFamily.Serif
    FontType.SANS_SERIF -> FontFamily.SansSerif
}
