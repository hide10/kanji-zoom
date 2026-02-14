package com.hide10.kanjizoom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hide10.kanjizoom.ui.theme.KanjiZoomTheme
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
    var showInfoDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(uiState.inputText) {
        scale = 1f
        offset = Offset.Zero
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Display + input area (tap to type, pinch to zoom)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .transformable(transformableState),
            contentAlignment = Alignment.Center,
        ) {
            val displayText = uiState.displayText
            val charCount = displayText.length.coerceAtLeast(1)
            val w = maxWidth.value
            val h = maxHeight.value
            val sizeByWidth = w * 0.85f / charCount
            val sizeByHeight = h * 0.85f
            val autoSizeDpValue = min(sizeByWidth, sizeByHeight)
            val density = LocalDensity.current
            val autoSizeSp = with(density) { (autoSizeDpValue / fontScale).sp }

            val textColor = MaterialTheme.colorScheme.onSurface

            BasicTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.onInputChanged(it) },
                textStyle = TextStyle(
                    fontSize = autoSizeSp,
                    fontFamily = uiState.fontType.toFontFamily(),
                    textAlign = TextAlign.Center,
                    color = textColor,
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (displayText.isEmpty()) {
                            Text(
                                text = "タップして入力",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.4f,
                                ),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { showInfoDialog = true }) {
                Text("i", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showInfoDialog) {
        val context = LocalContext.current
        val versionName = remember {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "-"
        }
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("漢字ズーム") },
            text = {
                Column {
                    Text("バージョン: $versionName")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("漢字を大きく表示して細部を確認できるアプリです。")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("操作方法:", style = MaterialTheme.typography.labelLarge)
                    Text("・タップして文字を入力")
                    Text("・ピンチで拡大・縮小")
                    Text("・拡大中にドラッグで移動")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("閉じる")
                }
            },
        )
    }
}

private fun FontType.toFontFamily(): FontFamily = when (this) {
    FontType.SERIF -> FontFamily.Serif
    FontType.SANS_SERIF -> FontFamily.SansSerif
}
