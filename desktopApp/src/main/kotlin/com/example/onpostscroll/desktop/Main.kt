package com.example.onpostscroll.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.onpostscroll.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OnPostScroll Bug",
    ) {
        App()
    }
}
