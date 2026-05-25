package com.example.onpostscroll

import kotlin.math.ceil
import kotlin.math.floor

class PostScrollStats {
    var lastDeltaY: Float = 0f
        private set

    var calls: Int = 0
        private set

    val formattedLastDeltaY: String
        get() = roundToTenth(lastDeltaY.toDouble()).toString()

    fun record(deltaY: Float) {
        lastDeltaY = deltaY
        calls += 1
    }

    private fun roundToTenth(value: Double): Double {
        return if (value >= 0.0) {
            floor(value * 10.0 + 0.5) / 10.0
        } else {
            ceil(value * 10.0 - 0.5) / 10.0
        }
    }
}
