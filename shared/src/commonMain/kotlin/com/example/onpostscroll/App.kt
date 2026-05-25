package com.example.onpostscroll

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        OnPostScrollBugSample()
    }
}

@Composable
private fun OnPostScrollBugSample() {
    val scrollState = rememberScrollState()
    val stats = remember { PostScrollStats() }
    var lastDeltaText by remember { mutableStateOf(stats.formattedLastDeltaY) }
    var calls by remember { mutableIntStateOf(stats.calls) }
    var sourceText by remember { mutableStateOf("none") }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                stats.record(available.y)
                lastDeltaText = stats.formattedLastDeltaY
                calls = stats.calls
                sourceText = source.toString()
                return available
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState),
    ) {
        ReproContent(
            scrollState = scrollState,
            lastDeltaText = lastDeltaText,
            calls = calls,
            sourceText = sourceText,
        )
    }
}

@Composable
private fun ReproContent(
    scrollState: ScrollState,
    lastDeltaText: String,
    calls: Int,
    sourceText: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        EdgeMarker("Top edge")
        Spacer(modifier = Modifier.height(320.dp))
        CenteredStatusSurface(
            lastDeltaText = lastDeltaText,
            calls = calls,
            sourceText = sourceText,
            scrollValue = scrollState.value,
            scrollMaxValue = scrollState.maxValue,
        )
        Spacer(modifier = Modifier.height(320.dp))
        EdgeMarker("Bottom edge")
    }
}

@Composable
private fun CenteredStatusSurface(
    lastDeltaText: String,
    calls: Int,
    sourceText: String,
    scrollValue: Int,
    scrollMaxValue: Int,
) {
    Box(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "onPostScroll available.y = $lastDeltaText",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "calls = $calls\nsource = $sourceText\nscroll = $scrollValue / $scrollMaxValue",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EdgeMarker(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
