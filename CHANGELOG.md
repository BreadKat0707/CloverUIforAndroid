# Changelog

本项目所有对 CloverUIforAndroid 的重要修改都会记录在此文件。
记录格式：`- 完成时间：YYYY-MM-DD HH:mm，修改内容`

## [Unreleased]

### Added

- 完成时间：2026-07-02 22:15，`CloverDropdownMenu` 重写为 Clover Flyout 风格：
  - 不再依赖 Material3 `DropdownMenu`，改为基于 `Popup` 自定义定位与动画。
  - 新增 `anchorBounds` / `hazeState` 参数，外观与 `CloverFlyout` 一致：圆角、缩放动画、可选亚克力背景。
  - 新增 `CloverDropdownMenuItemData` 便捷数据类，以及 items 便捷重载。
  - 新增 `CloverDropdownMenuDefaults` 默认参数。
- 完成时间：2026-07-02 22:15，`CloverMenuItem` 新增 `enabled` 参数。
- 完成时间：2026-07-02 22:00，`CloverAdaptiveShellScaffold` 新增完整布局策略系统：
  - `CloverShellStrategy`：组合标题栏位置（Top/Bottom）、导航栏位置（Start/End/Bottom/Hidden）、侧边导航形态（Rail/RailExpanded/View）。
  - 新增 `CloverShellTitleBarPosition`、`CloverShellNavigationPosition`、`CloverShellNavigationStyle` 枚举。
  - 提供 `CloverShellStrategy.Classic`（顶部标题栏 + 左侧窄 Rail）、`BottomCombined`（底部标题栏 + 底部导航，单手模式）、`TopBarWithBottomNav`（顶部标题栏 + 底部导航）三种预设。
  - `CloverShellState` 新增 `strategy` 属性，方便 content/overlay 读取当前策略。
  - 废弃旧的 `CloverShellNavigationSide`，保留以兼容旧代码。
- 完成时间：2026-06-25 23:27，新增 `CloverUIforAndroid/gradle.properties`，将 Library 版本号 `cloverUiVersion` 提取为单一数据源。

### Changed

- 完成时间：2026-06-25 23:27，`clover-ui/build.gradle.kts` 的 `maven-publish` 改为从 `gradle.properties` 读取 `cloverUiVersion`，不再硬编码 `0.1.0-SNAPSHOT`。
- 完成时间：2026-06-25 23:27，在 `clover-ui/build.gradle.kts` 中设置 `version = cloverUiVersion`，确保发布时版本号一致。

### Fixed

- 完成时间：2026-07-02 21:20，修复 `CloverButton` 系列组件内容色未正确应用的问题：`Text` 与 `Icon` 改为显式传入 `color`/`tint`，不再依赖 `LocalContentColor`，避免 fluent `Text` 读不到颜色导致黑字。
- 完成时间：2026-07-02 21:20，统一深色模式下按钮/图标按钮内容色为白色：实心、描边、文字按钮及 `CloverIconButton` 在深色模式默认使用白色内容色，浅色模式保持主色或自动黑/白。
- 完成时间：2026-07-02 21:08，修复 `CloverButton` 深色模式下可能出现黑底黑字/低对比度的问题：`filledColors` 默认内容色改为根据容器色亮度自动选择黑/白；`outlinedColors`/`textColors` 的禁用色改为基于 `onSurface` 的标准不透明度，提升可读性。
- 完成时间：2026-07-02 22:15，`CloverNavigationRail` / `CloverBottomNavbar` / `CloverNavigationView` 的每个导航项都加上 `cloverRevealItem()` 按压光效。
- 完成时间：2026-06-25 23:35，修复 `CloverTheme` 的 `colorScheme` 参数被忽略的问题：将参数改为 `CloverColorScheme? = null`，传入时优先使用，未传入时才根据 `dynamicColor`/`darkTheme` 生成。
- 完成时间：2026-06-25 23:35，修复 `CloverBasicItem` Modifier 顺序错误：改为 `.clip(shape).background(backgroundColor, shape).then(clickableModifier)`，使圆角背景生效。
- 完成时间：2026-06-25 23:35，修复 `CloverIndicationNode` 每次 `onAttach` 都启动新协程的问题：新增 `interactionJob` 并在 `onDetach` 中取消，防止重复收集 Interaction。
- 完成时间：2026-06-25 23:35，修复 `CloverDialog` 点击穿透问题：将无效的 `.clickable(enabled = false)` 替换为 `.pointerInput(Unit) { detectTapGestures { } }`；同时将 `LocalContentColor` 上移到包裹整个弹窗面板（含按钮行）。
- 完成时间：2026-06-25 23:35，修复 `CloverFlyout` 首帧定位跳变问题：新增 `isPanelMeasured` 状态，首次测量完成前通过 `alpha = 0f` 隐藏面板，测量完成后再正常显示。
- 完成时间：2026-06-25 23:35，修复 `CloverBottomSheet` 滑动时每帧重组的问题：将 `progress`、`scrimAlpha`、`DragHandle` 的 `alpha` 改用 `derivedStateOf` 读取 `dragOffset.value`。

### Changed

- 完成时间：2026-07-02 22:00，`CloverAdaptiveShellScaffold` 废弃 `navigationSide` 参数，改为通过 `strategy: CloverShellStrategy` 统一控制标题栏位置、导航栏位置与侧边导航形态；同步更新内部布局计算与 Acrylic 层覆盖逻辑。
- 完成时间：2026-07-02 21:45，`CloverAdaptiveShellScaffold` 支持 edge-to-edge：内容层改为全屏绘制并作为 Haze 模糊源，标题栏/导航栏的 acrylic 材质现在能模糊到背后的内容；调用方需使用 `CloverShellState.contentPadding` 让内容避开条带区域。
- 完成时间：2026-07-02 20:00，`CloverRevealHost` 仅共享指针状态，不再绘制跨元素聚光灯；`cloverRevealItem` 与 `cloverReveal` 使用同一套边缘辉光逻辑。
- 完成时间：2026-07-02 20:00，移除 `CloverRevealColors`，简化颜色 API 为 `CloverRevealDefaults.glowColor()` 与 `CloverRevealDefaults.rippleColor()`。
- 完成时间：2026-07-02 21:00，细化 `CloverRevealDefaults` 参数：新增 `hoverRadius`、`pressedRadius`、`releasedRippleRadiusFactor`，移除旧的 `revealDistance`。
- 完成时间：2026-07-02 21:00，修复 `cloverRevealItem` 中按压检测在 composition 阶段写状态的副作用，改为 `LaunchedEffect` 驱动水波纹触发。

## [0.1.0-SNAPSHOT] - 未发布

### Added

- 完成时间：项目初始化，发布 Clover Design UI Library for Android 第一个 SNAPSHOT 版本。
- 包含核心主题系统、按钮、控件、列表、布局导航、浮层、特效、材质、设置项等组件。

