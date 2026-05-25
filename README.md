# CMP onPostScroll Desktop Bug Repro

Small Kotlin Multiplatform + Compose Multiplatform project that demonstrates a nested scroll issue
on Desktop JVM.

## Project Shape

- `shared`: KMP library module with shared Compose UI.
- `androidApp`: Android application launcher that calls `App()` from `shared`.
- `desktopApp`: Desktop JVM application launcher that calls `App()` from `shared`.

## Repro

Run Desktop:

```bash
./gradlew :desktopApp:run
```

Run Android:

```bash
./gradlew :androidApp:installDebug
```

Scroll to the top or bottom edge and keep scrolling past the edge. The centered surface displays:

- `onPostScroll available.y`
- callback call count
- nested scroll source
- current scroll offset

Expected behavior: `onPostScroll` keeps receiving the unconsumed delta at the scroll edge. Android
does this. On Desktop JVM, the callback is not invoked for wheel scroll at the beginning/end, which
is the bug this sample isolates.

This sample's `onPostScroll` handler consumes the incoming `available` offset by returning it. That
means the parent nested-scroll handler takes precedence over overscroll. Desktop overscroll being
disabled is acceptable for this repro; the expected behavior is still that `onPostScroll` is invoked
with the unconsumed edge delta before any overscroll handling would matter.

## Verification

```bash
./gradlew :shared:jvmTest
./gradlew :desktopApp:jar :androidApp:assembleDebug
```
