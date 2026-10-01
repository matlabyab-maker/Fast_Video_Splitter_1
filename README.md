
# Fast Video Splitter

Android project for a Scene/Frame based video editor, splitter and merge workflow.

## Current project structure

- **Editor**: multi-row scene grid, scene selection, delete, timeline range selection, drag/long-press reorder.
- **Scene labels**: `(Humen)` is supported in the data model and UI.
- **Splitter**: UI entry point is included for Scene/Time/Frame splitting.
- **Merge/Join**: UI entry point is included for ordered retained segments.
- **Output folder**: Android Storage Access Framework.
- **Media**: Media3/ExoPlayer for source inspection.

## Important

This is the first Android project foundation. The current analyzer creates deterministic 10-second segments so the UI can be tested immediately. The production Scene Detector should replace `analyzeScenes()` with actual scene-change detection.

The final renderer should use FFmpeg stream-copy (`-c copy`) whenever all retained segments can be safely muxed without re-encoding. If cuts fall between keyframes or streams are incompatible, the app should explicitly offer a re-encode mode rather than silently changing quality.

The requested filename marker is exactly `Humen`.

## Build

Open the folder in Android Studio and let Gradle sync. Build the debug APK.
