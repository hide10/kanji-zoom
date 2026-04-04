package com.hide10.kanjizoom.stroke

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface StrokeDataLoadResult {
    data class Loaded(val strokeData: StrokeCharacterData) : StrokeDataLoadResult
    data object MissingAsset : StrokeDataLoadResult
    data class ParseFailure(val cause: Throwable) : StrokeDataLoadResult
}

interface StrokeOrderRepository {
    suspend fun getStrokeData(character: String): StrokeDataLoadResult
}

class AssetStrokeOrderRepository(
    private val assetManager: AssetManager,
) : StrokeOrderRepository {
    private val cacheLock = Any()
    private val cache = mutableMapOf<String, StrokeDataLoadResult>()

    override suspend fun getStrokeData(character: String): StrokeDataLoadResult = withContext(Dispatchers.IO) {
        synchronized(cacheLock) {
            if (cache.containsKey(character)) {
                return@withContext cache[character]
                    ?: StrokeDataLoadResult.MissingAsset
            }
        }

        val loaded = loadStrokeData(character)

        synchronized(cacheLock) {
            cache[character] = loaded
        }

        loaded
    }

    private fun loadStrokeData(character: String): StrokeDataLoadResult {
        val assetPath = "strokes/${character.toCodePointHex()}.svg"
        val svgContent = runCatching {
            assetManager.open(assetPath).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return StrokeDataLoadResult.MissingAsset

        return runCatching {
            StrokeDataParser.parseSvg(svgContent, character)
        }.fold(
            onSuccess = { StrokeDataLoadResult.Loaded(it) },
            onFailure = { StrokeDataLoadResult.ParseFailure(it) },
        )
    }
}
