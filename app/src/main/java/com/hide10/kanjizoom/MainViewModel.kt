package com.hide10.kanjizoom

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hide10.kanjizoom.stroke.AssetStrokeOrderRepository
import com.hide10.kanjizoom.stroke.DisplayMode
import com.hide10.kanjizoom.stroke.StrokeCharacterData
import com.hide10.kanjizoom.stroke.StrokeFallbackReason
import com.hide10.kanjizoom.stroke.StrokeDataLoadResult
import com.hide10.kanjizoom.stroke.StrokeOrderRepository
import com.hide10.kanjizoom.stroke.isHanScriptCharacter
import com.hide10.kanjizoom.stroke.toCodePointStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanjiZoomUiState(
    val inputText: String = "",
    val displayMode: DisplayMode = DisplayMode.Normal,
    val activeCharacterIndex: Int = 0,
    val activeCharacter: String? = null,
    val strokeCharacterData: StrokeCharacterData? = null,
    val strokeFallbackReason: StrokeFallbackReason? = null,
    val strokePlaybackVersion: Int = 0,
) {
    val displayText: String
        get() = inputText

    val inputCharacters: List<String>
        get() = inputText.toCodePointStrings()

    val strokeFallbackMessage: String?
        get() = when (strokeFallbackReason) {
            StrokeFallbackReason.EmptyInput -> "漢字を入力すると書き順を表示します。"
            StrokeFallbackReason.UnsupportedCharacterType -> "書き順表示は漢字のみ対応しています。"
            StrokeFallbackReason.MissingStrokeData -> "この文字の書き順データはまだありません。"
            StrokeFallbackReason.ParseFailed -> "書き順データの読み込みに失敗しました。"
            null -> null
        }
}

class MainViewModel(
    private val strokeOrderRepository: StrokeOrderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(KanjiZoomUiState())
    val uiState: StateFlow<KanjiZoomUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun onInputChanged(text: String) {
        val selection = resolveActiveSelection(text, _uiState.value.activeCharacterIndex)
        _uiState.update {
            it.copy(
                inputText = text,
                activeCharacterIndex = selection.index,
                activeCharacter = selection.character,
            )
        }
        refreshStrokeState()
    }

    fun onActiveCharacterSelected(index: Int) {
        val currentState = _uiState.value
        val selection = resolveActiveSelection(currentState.inputText, index)
        if (selection.character == null || selection.index == currentState.activeCharacterIndex) {
            return
        }

        _uiState.update {
            it.copy(
                activeCharacterIndex = selection.index,
                activeCharacter = selection.character,
            )
        }
        refreshStrokeState()
    }

    fun onDisplayModeChanged(mode: DisplayMode) {
        if (_uiState.value.displayMode == mode) {
            return
        }

        _uiState.update { it.copy(displayMode = mode) }
        refreshStrokeState()
    }

    fun onReplayRequested() {
        _uiState.update { state ->
            if (state.displayMode == DisplayMode.StrokeOrder && state.strokeCharacterData != null) {
                state.copy(strokePlaybackVersion = state.strokePlaybackVersion + 1)
            } else {
                state
            }
        }
    }

    private fun refreshStrokeState() {
        val currentState = _uiState.value
        val selection = resolveActiveSelection(
            inputText = currentState.inputText,
            requestedIndex = currentState.activeCharacterIndex,
        )
        val activeCharacter = selection.character
        val activeCharacterIndex = selection.index

        loadJob?.cancel()

        if (currentState.displayMode != DisplayMode.StrokeOrder) {
            _uiState.update {
                it.copy(
                    activeCharacterIndex = activeCharacterIndex,
                    activeCharacter = activeCharacter,
                    strokeCharacterData = null,
                    strokeFallbackReason = null,
                    strokePlaybackVersion = 0,
                )
            }
            return
        }

        if (activeCharacter == null) {
            _uiState.update {
                it.copy(
                    activeCharacterIndex = 0,
                    activeCharacter = null,
                    strokeCharacterData = null,
                    strokeFallbackReason = StrokeFallbackReason.EmptyInput,
                    strokePlaybackVersion = 0,
                )
            }
            return
        }

        if (!activeCharacter.isHanScriptCharacter()) {
            _uiState.update {
                it.copy(
                    activeCharacterIndex = activeCharacterIndex,
                    activeCharacter = activeCharacter,
                    strokeCharacterData = null,
                    strokeFallbackReason = StrokeFallbackReason.UnsupportedCharacterType,
                    strokePlaybackVersion = 0,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                activeCharacterIndex = activeCharacterIndex,
                activeCharacter = activeCharacter,
                strokeCharacterData = null,
                strokeFallbackReason = null,
                strokePlaybackVersion = 0,
            )
        }

        val inputSnapshot = currentState.inputText
        val activeIndexSnapshot = activeCharacterIndex
        loadJob = viewModelScope.launch {
            val loadResult = strokeOrderRepository.getStrokeData(activeCharacter)
            val strokeCharacterData: StrokeCharacterData?
            val fallbackReason = when (loadResult) {
                is StrokeDataLoadResult.Loaded -> {
                    strokeCharacterData = loadResult.strokeData
                    null
                }
                StrokeDataLoadResult.MissingAsset -> {
                    strokeCharacterData = null
                    StrokeFallbackReason.MissingStrokeData
                }
                is StrokeDataLoadResult.ParseFailure -> {
                    strokeCharacterData = null
                    StrokeFallbackReason.ParseFailed
                }
            }

            _uiState.update { latest ->
                if (
                    latest.displayMode != DisplayMode.StrokeOrder ||
                    latest.inputText != inputSnapshot ||
                    latest.activeCharacterIndex != activeIndexSnapshot
                ) {
                    return@update latest
                }

                latest.copy(
                    activeCharacterIndex = activeCharacterIndex,
                    activeCharacter = activeCharacter,
                    strokeCharacterData = strokeCharacterData,
                    strokeFallbackReason = fallbackReason,
                    strokePlaybackVersion = 0,
                )
            }
        }
    }

    private fun resolveActiveSelection(inputText: String, requestedIndex: Int): ActiveSelection {
        val characters = inputText.toCodePointStrings()
        if (characters.isEmpty()) {
            return ActiveSelection(index = 0, character = null)
        }

        val resolvedIndex = requestedIndex.coerceIn(0, characters.lastIndex)
        return ActiveSelection(
            index = resolvedIndex,
            character = characters[resolvedIndex],
        )
    }

    private data class ActiveSelection(
        val index: Int,
        val character: String?,
    )

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    strokeOrderRepository = AssetStrokeOrderRepository(application.assets),
                )
            }
        }
    }
}
