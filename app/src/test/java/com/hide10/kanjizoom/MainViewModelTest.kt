package com.hide10.kanjizoom

import com.hide10.kanjizoom.stroke.DisplayMode
import com.hide10.kanjizoom.stroke.StrokeCharacterData
import com.hide10.kanjizoom.stroke.StrokeDataLoadResult
import com.hide10.kanjizoom.stroke.StrokeFallbackReason
import com.hide10.kanjizoom.stroke.StrokeOrderRepository
import com.hide10.kanjizoom.stroke.StrokePathData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `stroke mode defaults to the first character`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(
                mapOf(
                    "漢" to sampleStrokeData("漢"),
                ),
            ),
        )

        viewModel.onInputChanged("漢字")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("漢", state.activeCharacter)
        assertEquals("漢字", state.displayText)
        assertEquals("漢", state.strokeCharacterData?.character)
        assertNull(state.strokeFallbackReason)
        assertEquals(0, state.strokePlaybackVersion)
    }

    @Test
    fun `stroke mode can switch to a tapped character`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(
                mapOf(
                    "天" to sampleStrokeData("天"),
                    "気" to sampleStrokeData("気"),
                ),
            ),
        )

        viewModel.onInputChanged("天気")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        viewModel.onActiveCharacterSelected(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.activeCharacterIndex)
        assertEquals("気", state.activeCharacter)
        assertEquals("気", state.strokeCharacterData?.character)
        assertNull(state.strokeFallbackReason)
    }

    @Test
    fun `unsupported character falls back in stroke mode`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(emptyMap()),
        )

        viewModel.onInputChanged("A")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("A", state.activeCharacter)
        assertNull(state.strokeCharacterData)
        assertEquals(StrokeFallbackReason.UnsupportedCharacterType, state.strokeFallbackReason)
    }

    @Test
    fun `missing han data reports missing stroke data`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(emptyMap()),
        )

        viewModel.onInputChanged("漢")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StrokeFallbackReason.MissingStrokeData, state.strokeFallbackReason)
    }

    @Test
    fun `parse failure reports parse failed fallback`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(
                parseFailures = setOf("漢"),
            ),
        )

        viewModel.onInputChanged("漢")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StrokeFallbackReason.ParseFailed, state.strokeFallbackReason)
        assertNull(state.strokeCharacterData)
    }

    @Test
    fun `replay increments playback version only when stroke data exists`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(
                mapOf(
                    "漢" to sampleStrokeData("漢"),
                ),
            ),
        )

        viewModel.onInputChanged("漢")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()

        val initialVersion = viewModel.uiState.value.strokePlaybackVersion
        assertNotNull(viewModel.uiState.value.strokeCharacterData)

        viewModel.onReplayRequested()

        assertEquals(initialVersion + 1, viewModel.uiState.value.strokePlaybackVersion)
    }

    @Test
    fun `changing input resets playback version to manual state`() = runTest {
        val viewModel = MainViewModel(
            strokeOrderRepository = FakeStrokeOrderRepository(
                mapOf(
                    "漢" to sampleStrokeData("漢"),
                    "字" to sampleStrokeData("字"),
                ),
            ),
        )

        viewModel.onInputChanged("漢")
        viewModel.onDisplayModeChanged(DisplayMode.StrokeOrder)
        advanceUntilIdle()
        viewModel.onReplayRequested()
        assertEquals(1, viewModel.uiState.value.strokePlaybackVersion)

        viewModel.onActiveCharacterSelected(1)
        advanceUntilIdle()
        viewModel.onInputChanged("字")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.activeCharacterIndex)
        assertEquals("字", state.activeCharacter)
        assertEquals(0, state.strokePlaybackVersion)
    }

    private class FakeStrokeOrderRepository(
        private val data: Map<String, StrokeCharacterData> = emptyMap(),
        private val parseFailures: Set<String> = emptySet(),
    ) : StrokeOrderRepository {
        override suspend fun getStrokeData(character: String): StrokeDataLoadResult =
            when {
                parseFailures.contains(character) -> {
                    StrokeDataLoadResult.ParseFailure(IllegalArgumentException("parse failed"))
                }
                data.containsKey(character) -> {
                    StrokeDataLoadResult.Loaded(requireNotNull(data[character]))
                }
                else -> StrokeDataLoadResult.MissingAsset
            }
    }

    private fun sampleStrokeData(character: String) = StrokeCharacterData(
        character = character,
        viewBoxWidth = 109f,
        viewBoxHeight = 109f,
        strokes = listOf(
            StrokePathData(order = 1, pathData = "M10,10 L20,20"),
        ),
    )
}
