package com.hide10.kanjizoom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class KanjiZoomUiState(
    val inputText: String = "",
) {
    val displayText: String
        get() = inputText
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(KanjiZoomUiState())
    val uiState: StateFlow<KanjiZoomUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
}
