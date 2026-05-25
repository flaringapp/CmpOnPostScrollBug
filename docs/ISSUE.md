**Bug Description**

On Compose Multiplatform Desktop, mouse-wheel scrolling does not dispatch the unconsumed edge delta to a parent `NestedScrollConnection.onPostScroll` when a `verticalScroll` child is already at the beginning or end of its scroll range.

The same nested-scroll setup works on Android: when the child scrollable cannot consume more scroll, the parent receives `onPostScroll(consumed, available, source)` with the remaining `available.y` delta. On Desktop JVM, the parent receives `onPostScroll` while the scrollable can still move, but stops receiving it once the scrollable reaches either edge and the user continues scrolling with the mouse wheel.

This breaks parent components that intentionally react to leftover scroll at content bounds. A common example is a collapsing top bar: the top bar can collapse while the content scrolls down, but it cannot expand again when the content is at the top and the user scrolls upward, because the parent never receives the unconsumed wheel delta.

The attached sample isolates the issue with a single `Surface` using:

```kotlin
Surface(
    modifier = Modifier
        .fillMaxSize()
        .nestedScroll(nestedScrollConnection)
        .verticalScroll(scrollState),
) {
    // content taller than viewport
}
```

The parent connection records every `onPostScroll` call and displays the last `available.y` value on screen. Android continues updating this value at the scroll edges. Desktop does not.

**Performance Issue Description**

This is not a performance issue.

**Affected Platforms**

Select one or multiple affected platforms below:

* [x] Desktop (Windows, Linux, macOS)
* [ ] iOS
* [ ] Android
* [ ] Web (K/Wasm) - Canvas based API
* [ ] Web (K/JS) - Canvas based API
* [ ] Web (K/JS) - HTML library

**Versions**

* Compose Multiplatform version\*: 1.11.0 (also tested on 1.10.1)
* Kotlin version\*: 2.3.21
* OS (name, version, arch): macOS 26.5, arm64
* Device (model or simulator for iOS issues): N/A
* JDK (for desktop issues): Oracle JDK 17.0.12
* Browser (for Web issues): N/A

**Reproduction Steps**

Reproducible example repository: https://github.com/flaringapp/CmpOnPostScrollBug

Steps and/or the code snippet to reproduce the behavior:

1. Run the Desktop sample:

```bash
./gradlew :desktopApp:run
```

2. Scroll inside the window with a mouse wheel or trackpad.
3. While the content is not at an edge, observe that the on-screen `calls` counter changes and `onPostScroll available.y` updates.
4. Scroll to the top edge.
5. Continue scrolling upward past the top edge.
6. Observe that the `calls` counter no longer changes and `onPostScroll available.y` is not updated.
7. Scroll to the bottom edge.
8. Continue scrolling downward past the bottom edge.
9. Observe the same issue: no additional `onPostScroll` calls are delivered for the unconsumed wheel delta.

For comparison, run the Android sample:

```bash
./gradlew :androidApp:installDebug
```

On Android, continuing to scroll past the top or bottom edge keeps invoking the parent `onPostScroll` callback with the unconsumed `available.y` delta.

**Expected Behavior**

Desktop should dispatch leftover mouse-wheel scroll delta through nested scroll in the same way Android does.

When a child `verticalScroll` cannot consume scroll because it is already at the start or end of its range, the parent `NestedScrollConnection.onPostScroll` should still be invoked with the unconsumed `available` delta. This should happen before, or at least independently from, any overscroll visual handling.

Expected high-level behavior:

* While content can scroll, the child consumes the delta and parent `onPostScroll` receives the post-scroll callback.
* At the top edge, further upward wheel scroll should reach the parent as unconsumed delta.
* At the bottom edge, further downward wheel scroll should reach the parent as unconsumed delta.
* Behavior should be consistent between Desktop JVM and Android for the same Compose nested-scroll setup.

**Screenshots/video**

Video: [issue_recording.mov](issue_recording.mov)

**Sample Code**

Minimal reproduction project: https://github.com/flaringapp/CmpOnPostScrollBug

Relevant files in the reproduction project:

* `shared/src/commonMain/kotlin/com/example/onpostscroll/App.kt`
* `desktopApp/src/main/kotlin/com/example/onpostscroll/desktop/Main.kt`
* `androidApp/src/main/kotlin/com/example/onpostscroll/android/MainActivity.kt`

Core nested-scroll code:

```kotlin
val scrollState = rememberScrollState()
val nestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            // The sample records available.y and call count here.
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
    // Content taller than the viewport.
}
```

**Profiling Data for Performance Issue**

N/A.

**Additional Information**

The issue seems specific to Desktop mouse-wheel input and scroll-bound handling, not to nested scroll in general:

* `onPostScroll` is called on Desktop while the child scrollable can still consume/move.
* `onPostScroll` stops being called only after the child reaches the top or bottom edge and further wheel delta is unconsumed by the child.
* Android delivers this edge delta to the parent with the same sample code.

Likely internal area to inspect:

* Desktop mouse-wheel handling around `MouseWheelScrollable` / `DesktopScrollable`.
* The path where wheel deltas are converted into scroll deltas and passed into `ScrollableState`.
* The interaction between `ScrollableState` bounds, nested scroll dispatch, and overscroll handling.

My current guess is that Desktop wheel input is being dropped or marked handled when the scrollable is already at bounds, before the remaining delta is dispatched to the parent nested-scroll connection as `available` in `onPostScroll`. If so, the fix is likely in Desktop wheel/scrollable dispatch rather than in application-level nested-scroll code.

The sample's `onPostScroll` returns `available`, so the parent consumes the leftover delta. This is intentional for the repro: it proves the parent wants the unconsumed edge delta and avoids depending on overscroll visuals. Desktop overscroll being disabled or visually different should not prevent `onPostScroll` from being invoked with the leftover delta.
