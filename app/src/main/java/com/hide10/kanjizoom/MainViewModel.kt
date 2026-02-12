package com.hide10.kanjizoom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class FontType(val label: String) {
    SERIF("明朝"),
    SANS_SERIF("ゴシック"),
}

data class KanjiZoomUiState(
    val inputText: String = "",
    val fontType: FontType = FontType.SERIF,
) {
    val displayChar: String
        get() = inputText.firstOrNull()?.toString() ?: ""
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(KanjiZoomUiState())
    val uiState: StateFlow<KanjiZoomUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onFontSelected(fontType: FontType) {
        _uiState.update { it.copy(fontType = fontType) }
    }
}
