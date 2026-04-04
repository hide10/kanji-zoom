package com.hide10.kanjizoom

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hide10.kanjizoom.stroke.DisplayMode
import com.hide10.kanjizoom.stroke.StrokeOrderCanvas
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
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MainViewModel = viewModel(
        factory = remember(application) { MainViewModel.factory(application) },
    )
    val uiState by viewModel.uiState.collectAsState()
    val isStrokeMode = uiState.displayMode == DisplayMode.StrokeOrder
    val hasStrokeData = uiState.strokeCharacterData != null
    val enableTransform = !isStrokeMode
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

    LaunchedEffect(uiState.inputText, uiState.displayMode, hasStrokeData) {
        scale = 1f
        offset = Offset.Zero
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .then(
                    if (enableTransform) {
                        Modifier.transformable(transformableState)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val displayText = uiState.displayText
            val charCount = displayText.codePointCount(0, displayText.length).coerceAtLeast(1)
            val sizeByWidth = maxWidth.value * 0.85f / charCount
            val sizeByHeight = maxHeight.value * 0.85f
            val autoSizeDpValue = min(sizeByWidth, sizeByHeight)
            val density = androidx.compose.ui.platform.LocalDensity.current
            val autoSizeSp = with(density) { (autoSizeDpValue / fontScale).sp }
            val textColor = MaterialTheme.colorScheme.onSurface
            val helperColor = MaterialTheme.colorScheme.onSurfaceVariant

            if (isStrokeMode) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StrokeModeTargetCard(
                        inputText = uiState.inputText,
                        inputCharacters = uiState.inputCharacters,
                        activeCharacterIndex = uiState.activeCharacterIndex,
                        activeCharacter = uiState.activeCharacter,
                        canReplay = hasStrokeData,
                        onValueChange = viewModel::onInputChanged,
                        onCharacterSelected = viewModel::onActiveCharacterSelected,
                        onReplayRequested = viewModel::onReplayRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            hasStrokeData -> {
                                StrokeOrderCanvas(
                                    strokeData = requireNotNull(uiState.strokeCharacterData),
                                    playbackVersion = uiState.strokePlaybackVersion,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 8.dp),
                                )
                            }

                            displayText.isEmpty() -> {
                                Text(
                                    text = "上の入力欄に漢字を入力",
                                    fontSize = 24.sp,
                                    color = helperColor.copy(alpha = 0.5f),
                                )
                            }

                            else -> {
                                Text(
                                    text = uiState.activeCharacter.orEmpty(),
                                    fontSize = autoSizeSp,
                                    fontFamily = FontFamily.Serif,
                                    color = helperColor.copy(alpha = 0.28f),
                                )
                            }
                        }
                    }

                    if (!uiState.strokeFallbackMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.strokeFallbackMessage.orEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center,
                            color = helperColor,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                BasicTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputChanged,
                    textStyle = TextStyle(
                        fontSize = autoSizeSp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        color = textColor,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (displayText.isEmpty()) {
                                Text(
                                    text = "タップして入力",
                                    fontSize = 24.sp,
                                    color = helperColor.copy(alpha = 0.4f),
                                )
                            }

                            innerTextField()
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeButton(
                label = "通常",
                selected = !isStrokeMode,
                onClick = { viewModel.onDisplayModeChanged(DisplayMode.Normal) },
            )
            ModeButton(
                label = "書き順",
                selected = isStrokeMode,
                onClick = { viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder) },
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { showInfoDialog = true }) {
                Text("i", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (isStrokeMode) {
            Text(
                text = "入力欄の文字をタップして、書き順を見たい文字に切り替えます。",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showInfoDialog) {
        val versionName = remember(context) {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "-"
        }
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("漢字ズーム") },
            text = {
                Column {
                    Text("バージョン: $versionName")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("漢字を大きく表示し、書き順も確認できるアプリです。")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("操作方法:", style = MaterialTheme.typography.labelLarge)
                    Text("・タップして文字を入力")
                    Text("・通常モードではピンチで拡大・縮小")
                    Text("・拡大中にドラッグで移動")
                    Text("・書き順ボタンで1文字のアニメ再生に切替")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("書き順データ: KanjiVG (CC BY-SA 3.0)")
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

@Composable
private fun ModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label)
        }
    }
}

@Composable
private fun StrokeModeTargetCard(
    inputText: String,
    inputCharacters: List<String>,
    activeCharacterIndex: Int,
    activeCharacter: String?,
    canReplay: Boolean,
    onValueChange: (String) -> Unit,
    onCharacterSelected: (Int) -> Unit,
    onReplayRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "書き順モード",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (inputText.isEmpty()) {
                    "文字を入力して、再生したい漢字を選びます"
                } else {
                    "入力中の文字列"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                ),
                placeholder = {
                    Text("ここに文字を入力")
                },
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "書き順を見たい文字をタップ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (inputCharacters.isEmpty()) {
                    StrokeTargetChip(
                        character = "未",
                        selected = false,
                        enabled = false,
                        onClick = {},
                    )
                } else {
                    inputCharacters.forEachIndexed { index, character ->
                        StrokeTargetChip(
                            character = character,
                            selected = index == activeCharacterIndex,
                            onClick = { onCharacterSelected(index) },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (activeCharacter == null) {
                    "入力した文字から書き順を見る文字を選べます"
                } else {
                    "「$activeCharacter」を選択中です"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReplayRequested,
                enabled = canReplay,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (activeCharacter == null) {
                        "書き順を見る文字を選択"
                    } else {
                        "「$activeCharacter」の書き順を見る"
                    },
                )
            }
        }
    }
}

@Composable
private fun StrokeTargetChip(
    character: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = Modifier.then(
            if (enabled) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
    ) {
        Text(
            text = character,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}
