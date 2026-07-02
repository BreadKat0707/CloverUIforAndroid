# Changelog

本项目所有对 CloverUIforAndroid 的重要修改都会记录在此文件。
记录格式：`- 完成时间：YYYY-MM-DD HH:mm，修改内容`

## [Unreleased]

### Added

- 完成时间：2026-06-25 23:27，新增 `CloverUIforAndroid/VERSIONING.md` 版本号管理规则文档。
- 完成时间：2026-06-25 23:27，新增 `CloverUIforAndroid/gradle.properties`，将 Library 版本号 `cloverUiVersion` 提取为单一数据源。

### Changed

- 完成时间：2026-06-25 23:27，`clover-ui/build.gradle.kts` 的 `maven-publish` 改为从 `gradle.properties` 读取 `cloverUiVersion`，不再硬编码 `0.1.0-SNAPSHOT`。
- 完成时间：2026-06-25 23:27，在 `clover-ui/build.gradle.kts` 中设置 `version = cloverUiVersion`，确保发布时版本号一致。

### Fixed

- 完成时间：2026-06-25 23:35，修复 `CloverTheme` 的 `colorScheme` 参数被忽略的问题：将参数改为 `CloverColorScheme? = null`，传入时优先使用，未传入时才根据 `dynamicColor`/`darkTheme` 生成。
- 完成时间：2026-06-25 23:35，修复 `CloverBasicItem` Modifier 顺序错误：改为 `.clip(shape).background(backgroundColor, shape).then(clickableModifier)`，使圆角背景生效。
- 完成时间：2026-06-25 23:35，修复 `CloverIndicationNode` 每次 `onAttach` 都启动新协程的问题：新增 `interactionJob` 并在 `onDetach` 中取消，防止重复收集 Interaction。
- 完成时间：2026-06-25 23:35，修复 `CloverDialog` 点击穿透问题：将无效的 `.clickable(enabled = false)` 替换为 `.pointerInput(Unit) { detectTapGestures { } }`；同时将 `LocalContentColor` 上移到包裹整个弹窗面板（含按钮行）。
- 完成时间：2026-06-25 23:35，修复 `CloverFlyout` 首帧定位跳变问题：新增 `isPanelMeasured` 状态，首次测量完成前通过 `alpha = 0f` 隐藏面板，测量完成后再正常显示。
- 完成时间：2026-06-25 23:35，修复 `CloverBottomSheet` 滑动时每帧重组的问题：将 `progress`、`scrimAlpha`、`DragHandle` 的 `alpha` 改用 `derivedStateOf` 读取 `dragOffset.value`。

### Changed

- 完成时间：2026-07-02 20:00，重写 `CloverReveal` 光效为边缘辉光效果：移除跨元素共享聚光灯，改为每个元素独立绘制边缘辉光；浅色模式使用主题主色辉光，深色模式使用主色/白色辉光，提升可见性；按压结束后 spotlight 与水波纹会立即消退，避免残留。
- 完成时间：2026-07-02 20:00，`CloverRevealHost` 仅共享指针状态，不再绘制跨元素聚光灯；`cloverRevealItem` 与 `cloverReveal` 使用同一套边缘辉光逻辑。
- 完成时间：2026-07-02 20:00，移除 `CloverRevealColors`，简化颜色 API 为 `CloverRevealDefaults.glowColor()` 与 `CloverRevealDefaults.rippleColor()`。

## [0.1.0-SNAPSHOT] - 未发布

### Added

- 完成时间：项目初始化，发布 Clover Design UI Library for Android 第一个 SNAPSHOT 版本。
- 包含核心主题系统、按钮、控件、列表、布局导航、浮层、特效、材质、设置项等组件。

