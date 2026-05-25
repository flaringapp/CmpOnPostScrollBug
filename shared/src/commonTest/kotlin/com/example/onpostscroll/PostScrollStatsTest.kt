package com.example.onpostscroll

import kotlin.test.Test
import kotlin.test.assertEquals

class PostScrollStatsTest {
    @Test
    fun recordsLastDeltaAndCallCount() {
        val stats = PostScrollStats()

        stats.record(12.5f)
        stats.record(-4f)

        assertEquals(-4f, stats.lastDeltaY)
        assertEquals(2, stats.calls)
    }

    @Test
    fun formatsDeltaForDisplay() {
        val stats = PostScrollStats()

        stats.record(1.25f)

        assertEquals("1.3", stats.formattedLastDeltaY)
    }
}
