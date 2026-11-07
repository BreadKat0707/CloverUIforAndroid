# CloverUI 变更记录

## 2026-07-09 00:18
- 修复 `CloverWindowBottomSheet` / `CloverBottomSheet` 事件穿透问题：
  - 在 `BoxWithConstraints`（Dialog/Popup 内容最外层）加 `PointerEventPass.Final` 拦截器。
  - 子节点（LazyColumn、按钮等）优先消费事件；未被消费的事件由外层兜底消费，避免滑动/点击穿透到 Dialog 后面的 Activity 内容。
  - 文件：`ui/overlay/CloverBottomSheet.kt`。
- 重新发布 `clover-ui 0.1.1-SNAPSHOT` 到 mavenLocal；`FloatHearingAndroid` 编译通过。

## 2026-07-09 00:17
- 优化 `CloverBottomSheet` / `CloverWindowBottomSheet` 的内容区拖拽响应：
  - `NestedScrollConnection` 中改用 `runBlocking { dragOffset.snapTo(...) }` 同步更新 sheet 位置，减少 `scope.launch` 带来的帧延迟。
  - 限制 `dragOffset` 范围在 `[0, screenHeightPx]`，防止 overdrag。
  - 内容区滚动到顶后继续下拉时，若 sheet 已完全滑出屏幕则立即调用 `onDismiss()`。
  - 文件：`ui/overlay/CloverBottomSheet.kt`。
- 重新发布 `clover-ui 0.1.1-SNAPSHOT` 到 mavenLocal；`FloatHearingAndroid` 编译通过。

## 2026-07-08 22:56
- `CloverWindowBottomSheet` 改回 `Dialog` 实现：
  - `Popup` 虽无窗口动画，但关闭后会导致页面被“硬控”无法立即交互，且丧失状态栏/导航栏沉浸。
  - 改回 `Dialog(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)`，恢复焦点和 edge-to-edge。
  - 同时优化动画：拖拽关闭动画从 250ms 缩短到 180ms；预测返回手势完成后直接调用 `onDismiss()`，不再额外播放滑出动画。
  - 文件：`ui/overlay/CloverBottomSheet.kt`。
- 更新 `components.md` 中 `CloverWindowBottomSheet` 的描述。
- 重新发布 `clover-ui 0.1.1-SNAPSHOT` 到 mavenLocal；`FloatHearingAndroid` 编译通过。

## 2026-07-08 22:37
- 修复 `CloverWindowBottomSheet` 手滑收回后遮罩延迟消失的问题：
  - 将窗口实现从 `androidx.compose.ui.window.Dialog` 改为 `Popup`。
  - `Popup` 没有额外的窗口入场/出场动画，sheet 滑出与遮罩淡出完全同步。
  - 保留返回键关闭、点击外部关闭、拖拽手柄下拉收起等交互。
  - 文件：`ui/overlay/CloverBottomSheet.kt`。
- 更新 `components.md` 中 `CloverWindowBottomSheet` 的描述，说明其基于 `Popup` 实现。
- 重新发布 `clover-ui 0.1.1-SNAPSHOT` 到 mavenLocal；`FloatHearingAndroid` 编译通过。

## 2026-07-08 21:27
- 升级 CloverUI 版本号到 `0.1.1-SNAPSHOT`，解决 FloatHearingAndroid 因 Gradle SNAPSHOT 缓存无法解析新 API 的问题：
  - `CloverUIforAndroid/gradle.properties`：`cloverUiVersion` 从 `0.1.0-SNAPSHOT` 改为 `0.1.1-SNAPSHOT`。
  - `CloverDemo/gradle/libs.versions.toml`：`cloverUi` 同步改为 `0.1.1-SNAPSHOT`。
  - `FloatHearingAndroid/app/build.gradle.kts`：CloverUI 依赖版本同步改为 `0.1.1-SNAPSHOT`。
  - 为 `FloatHearingAndroid` 的 `SettingsScreen.kt` 补全缺失的 import：`CloverWindowBottomSheet`、`CloverWindowDialog`、`CloverDialogPosition`。
- 重新发布 `clover-ui 0.1.1-SNAPSHOT` 到 mavenLocal；`CloverDemo` 与 `FloatHearingAndroid` 均编译通过。

## 2026-07-08 20:04
- 新增 Window 层 `CloverWindowDialog` 和 `CloverWindowBottomSheet`：
  - `CloverDialog` / `CloverBottomSheet` 保持同层覆盖层绘制，可与祖先 `HazeState` 共享模糊源。
  - 新增 `CloverWindowDialog`：使用 `androidx.compose.ui.window.Dialog` 系统独立窗口，参数与同层版一致但不支持 `hazeState`。
  - 新增 `CloverWindowBottomSheet`：使用 `Dialog` 系统独立窗口，参数与同层版一致但不支持 `hazeState` / `hazeTints`。
  - 将 `CloverDialog` 与 `CloverBottomSheet` 的面板内容抽为私有函数 `CloverDialogPanel` / `CloverBottomSheetPanel`，供同层与窗口版共用。
  - 文件：`ui/overlay/CloverDialog.kt`、`ui/overlay/CloverBottomSheet.kt`。
- 更新 `components.md`：新增两个组件说明，Overlay 统计从 7 修正为 9，公开 API 总数从 60 修正为 62。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-08 20:01
- 修复 `CloverAdaptiveShellScaffold` 单手模式系统导航栏区域没有背景的问题：
  - 在底部栏下方额外补一块高度为 `navBarBottom` 的同色背景，覆盖系统导航栏区域。
  - 标题栏和底部导航栏保持正常排列，坐在系统导航栏上方。
  - 文件：`ui/layout/CloverAdaptiveShellScaffold.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-08 19:55
- 修复 `CloverAdaptiveShellScaffold` 单手模式底部标题栏被系统导航栏盖住的问题：
  - 底部栏（Bottom TitleBar + BottomNavbar）整体向上抬高 `navBarBottom`，不再延伸到系统导航栏下方。
  - `contentPadding.bottom` 同步改为 `bottomBarHeight + navBarBottom`，避免内容被底部手势条/导航键盖住。
  - 文件：`ui/layout/CloverAdaptiveShellScaffold.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-08 19:48
- 维护 `components.md` 统计与最后更新时间：
  - Material 章节从 6 个 API 修正为 8 个（新增 `Modifier.cloverSolid`、`CloverSolidSurface`）。
  - 公开 API 总数从 58 修正为 60。
  - 更新最后更新时间为 2026-07-08 19:48。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-06 01:00
- 新增 Clover Solid（纯色）材质，并把所有面板/栏位从 Acrylic 切换为 Solid：
  - 新增 `CloverMaterial.Solid.backgroundColor()` 与 `Modifier.cloverSolid` / `CloverSolidSurface`。
  - 恢复 `cloverAcrylic` 为 Haze 2.0 模糊实现（保留但不再默认使用）。
  - 把 `CloverAdaptiveShellScaffold` 的顶部标题栏、侧边导航栏、底部栏背景从 `cloverAcrylic` 改为 `cloverSolid`。
  - 把 `CloverBottomSheet`、`CloverDialog`、`CloverDropdownMenu`、`CloverFlyout` 的面板背景从 `cloverAcrylic` 改为 `cloverSolid`。
  - Demo 的 `DemoDetailScaffold` 标题栏也从 Acrylic 改为 Solid。
  - 文件：`material/CloverMaterial.kt`、`ui/layout/CloverAdaptiveShellScaffold.kt`、`ui/overlay/CloverBottomSheet.kt`、`ui/overlay/CloverDialog.kt`、`ui/overlay/CloverFlyout.kt`、`ui/control/CloverDropdownMenu.kt`、`CloverDemo/app/.../DemoDetailScaffold.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-06 00:35
- 临时将 `cloverAcrylic` 改为不透明纯色背景：
  - 由于 Haze 模糊在 API 35 上仍无法正常工作，先把 `cloverAcrylic` 内部实现改为直接绘制 `backgroundColor` 的纯色背景，不带模糊效果。
  - 保留 `CloverAcrylicSurface`、API 签名和 Haze 依赖，后续可恢复模糊逻辑。
  - 文件：`clover-ui/src/main/java/cn/lemondrop/clover/material/CloverMaterial.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-06 00:27
- 升级 Haze 到 2.0.0-alpha03 以修复亚克力透明问题：
  - 版本号：`libs.versions.toml` 中 `haze` 从 `1.7.2` 升级到 `2.0.0-alpha03`。
  - 新增 `haze-blur` 模块依赖，并将 `haze` / `haze-blur` 从 `implementation` 改为 `api`，使调用方也能访问 `HazeColorEffect` 等公开 API。
  - `cloverAcrylic` 改用 Haze 2.0 API：
    - `HazeStyle` → `HazeBlurStyle`
    - `HazeTint` → `HazeColorEffect`
    - `hazeEffect(state, style = ...)` → `hazeEffect(state) { blurEffect { this.style = style } }`
  - 同步更新 `CloverHazeDefaults.tints()` 和 `CloverBottomSheet` 的 `hazeTints` 类型为 `List<HazeColorEffect>`。
  - 文件：`clover-ui/build.gradle.kts`、`clover-ui/gradle/libs.versions.toml`、`material/CloverMaterial.kt`、`effects/CloverHaze.kt`、`ui/overlay/CloverBottomSheet.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-06 00:07
- 修复 `CloverAdaptiveShellScaffold` 单手模式底部栏背景不统一的问题：
  - `CloverBottomNavbar` 默认使用实色 `surface` 背景，把外层统一 Acrylic 背景盖住，导致导航栏看起来是实色而标题栏透明。
  - 修正：在 `BottomCombined` / `TopBarWithBottomNav` 的底部栏中调用 `CloverBottomNavbar` 时传入 `color = Color.Transparent`，让它与 `CloverTitleBar` 共享同一 Acrylic 背景。
  - 文件：`clover-ui/src/main/java/cn/lemondrop/clover/ui/layout/CloverAdaptiveShellScaffold.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-06 00:01
- 修复 `CloverAdaptiveShellScaffold` 单手模式底部栏顺序：
  - 单手操作（`BottomCombined`）下，底部栏垂直顺序改为：上方是底部导航栏，下方是标题栏。
  - 文件：`clover-ui/src/main/java/cn/lemondrop/clover/ui/layout/CloverAdaptiveShellScaffold.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 23:49
- 移除 `CloverTopAppBar` 及相关 API：
  - 删除 `clover-ui/src/main/java/cn/lemondrop/clover/ui/layout/CloverTopAppBar.kt`。
  - 删除 `CloverSizes.topAppBarExpandedHeight`。
- `CloverTitleBar` 改进深色模式适配：
  - 新增 `contentColor` 参数，默认 `LocalCloverColorScheme.current.onSurface`。
  - 使用 `CompositionLocalProvider(LocalContentColor)` 为 leading/trailing 内容提供统一内容色。
  - 标题文字样式强制使用 `contentColor`，避免深色模式下出现黑底黑字。
- `CloverAdaptiveShellScaffold` 默认标题栏改为 `CloverTitleBar`：
  - 顶部标题栏从 `CloverTopAppBar` 切换为 `CloverTitleBar`。
  - 高度从 `topAppBarExpandedHeight` 改为 `titleBarHeight`。
  - 内容区 `contentPadding.top` 同步改为 `titleBarHeight`。
  - 移除 `subtitle` 参数。
- Demo 同步移除 `CloverTopAppBar` 演示，标题栏示例统一使用 `CloverTitleBar`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 23:45
- 修复 `CloverTopAppBar` 在狭小宽度下崩溃的问题：
  - 当标题栏可用宽度小于“导航图标 + 操作按钮 + 间距”时，`maxTitleWidth` 计算为负数，导致 `Constraints.copy(maxWidth = negative)` 抛出 `IllegalArgumentException`。
  - 修正：将 `maxTitleWidth` 用 `coerceAtLeast(0)` 限制为最小 0。
  - 文件：`clover-ui/src/main/java/cn/lemondrop/clover/ui/layout/CloverTopAppBar.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 23:41
- 修复 `CloverTopAppBar` 在无限高度约束下崩溃的问题：
  - 当 TopAppBar 被置于 `LazyColumn` 等提供无限高度约束的容器中时，`constraints.maxHeight - collapsedHeight` 会产生无法被 `Constraints` 编码的极大值。
  - 修正：当 `constraints.maxHeight == Constraints.Infinity` 时直接保持 `Infinity`，否则才减去 `collapsedHeight`。
  - 文件：`clover-ui/src/main/java/cn/lemondrop/clover/ui/layout/CloverTopAppBar.kt`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 23:37
- 修复标题栏与导航栏空白区域点击穿透问题：
  - `CloverTitleBar`、`CloverTopAppBar`、`CloverNavigationRail`、`CloverNavigationView`、`CloverBottomNavbar` 的外层容器统一添加 `consumeTouches`。
  - 仅消费子项按钮/导航项之间空白区域的事件，内部可点击子项仍可正常响应。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 22:04
- 修复亚克力全部失效的问题：
  - 移除 `cloverAcrylic` 中位于 `hazeEffect` 之前的实色 fallback 背景层，该层会盖住模糊效果。
  - 删除 `fallbackColor` 参数及 `CloverAcrylicSurface` 中的对应参数。
- `CloverTopAppBar` 改为 MD3 Large TopAppBar 风格，新增 `collapsed` 参数：
  - `collapsed = false` 时显示大标题（`headlineMedium`，128dp 高）。
  - `collapsed = true` 时收折为普通 TopAppBar（64dp 高）。
  - 标题/图标/操作按钮随状态平滑过渡。
- `CloverTitleBar` 明确为底部普通标题栏，固定使用 `topAppBarCollapsed` 字号，不收折。
- 重写 `CloverAdaptiveShellScaffold`：
  - 内容区全屏绘制到状态栏、系统导航栏、标题栏和导航栏后方，作为 Haze 模糊源。
  - 顶部标题栏背景延伸到状态栏下方，侧边导航栏背景同步延伸。
  - 底部栏背景延伸到系统导航栏下方。
  - 标题栏/导航栏与内容交界处改用 1dp 分隔线，移除 `CloverOutsideStroke` 外描边。
  - 新增 `externalHazeState` 参数，允许与外部（如 Demo 全局背景）共享 HazeState。
  - `background` 默认改为透明，让 App 级背景（如壁纸）透出。
  - 单手操作模式：底部 TitleBar 放在 BottomNavbar 上方。
- 侧边导航形态精简为两种：
  - `Rail`：固定 collapsed，图标在上、文字在下。
  - `View`：可展开/收折，展开左图标右文字，收折仅图标。
  - 移除 `RailExpanded` 及水平图标+文字的导航形态。
- `CloverFlyout` / `CloverDropdownMenu`：`reveal` 默认改为 `false`，面板整体不再启用 CloverReveal 边缘辉光和水波纹；菜单项仍保留基本按压高亮。
- `CloverDialog`（底部位置）增加系统导航栏安全边距，避免按钮被导航栏盖住。
- 新增 `CloverTypography.headlineMedium` / `topAppBarCollapsed`。
- 新增 `CloverSizes.topAppBarExpandedHeight = 128.dp`。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 21:16
- 回退 `CloverNavigationRail` 的过度修改，恢复为原始实现：
  - 保留原始 API：`backgroundColor`、`header`、`footer`。
  - 固定为图标在上、文字在下的 collapsed 布局（本身不支持展开）。
  - 文件位置恢复为 `ui/layout/CloverNavigationRail.kt`。
- 修复 `CloverAdaptiveShellScaffold` 对 `CloverNavigationRail` 的调用：使用 `backgroundColor = Color.Transparent`。
- 修复 `CloverNavigationView` 中误引用已删除的 `CloverNavigationRailDefaults.MinWidth`，改为 `CloverSizes.navigationRailWidth`。
- 重新编译并发布 `clover-ui` 到 mavenLocal。

## 2026-07-05 20:56
- 精简侧边导航形态：
  - 从 `CloverShellNavigationStyle` 移除 `RailExpanded`。
  - `Rail` 固定为图标在上、文字在下的垂直布局。
  - `View` 支持 `expanded` 参数：展开时左图标右文字，收折时仅显示图标。
  - `CloverNavigationRail` 移除展开/折叠状态，简化 API。
  - `CloverNavigationView` 新增 `expanded` / `onToggleExpanded` 参数。
  - `CloverAdaptiveShellScaffold` 内部管理 View 的展开状态，默认展开。
  - Demo 的 `AdaptiveScaffoldDemoScreen` 移除 `RailExpanded` 选项。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 20:20
- 修复 `CloverAdaptiveShellScaffold` 顶部标题栏背景未延伸到状态栏的问题：
  - `ShellAcrylicLayer` 中顶部条带的 `cloverAcrylic` 将 `backgroundColor` 从 `Color.Transparent` 改为 `CloverMaterial.Acrylic.backgroundColor()`，让状态栏区域也能显示半透/亚克力底色。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 20:18
- `CloverAdaptiveShellScaffold` 默认策略改为常规手机布局：
  - `CloverShellStrategy` 主构造函数默认值从 `navigationPosition = Start` 改为 `navigationPosition = Bottom`。
  - `CloverAdaptiveShellScaffold` 的 `strategy` 参数默认值从 `CloverShellStrategy.Classic` 改为 `CloverShellStrategy.TopBarWithBottomNav`。
  - 文档注释同步更新。
- 重新编译并发布 `clover-ui` 到 mavenLocal；`CloverDemo` 编译通过。

## 2026-07-05 19:43
- 修复亚克力面板（Flyout / BottomSheet / Dialog / DropdownMenu）完全透明的问题：
  - 问题：移除了面板自身的不透明 `surface` 背景后，`hazeEffect` 在 Haze 不可用时只依赖 `backgroundColor` 作为 fallback，但这个 fallback 在半透 `Color` 下无法撑起实色面板。
  - 修正：在 `cloverAcrylic` 内部**先绘制一层不透明 fallback 底色**（默认主题 `surface`），再在其上应用 `hazeEffect`。这样 Haze 生效时底层被模糊并叠上半透 tint，Haze 不可用时也能看到正常的实色面板。
  - 新增 `fallbackColor` 参数到 `cloverAcrylic` 与 `CloverAcrylicSurface`，用于控制底层实色；默认使用 `LocalCloverColorScheme.current.surface`。
  - 四个浮层面板改回使用底层不透明 surface + 亚克力模糊效果，不再透明。
- 重新编译并发布 `clover-ui` 到 mavenLocal。

## 2026-07-05 19:22
- 修复 Flyout / BottomSheet / Dialog / DropdownMenu 亚克力模糊不生效的问题：
  - 根本原因：面板在调用 `cloverAcrylic` 之前先画了不透明的 `surface` 背景，把模糊层完全盖住。
  - 移除四个浮层亚克力路径中的 `.background(surface)`，改为由 `cloverAcrylic` 自身提供半透 fallback 底色与 tint。
  - 将 `shape` 直接传给 `cloverAcrylic`，避免重复 clip。
- `CloverDialog` 从系统独立 `Dialog` 改为同层覆盖层绘制：
  - 不再创建新窗口，可与祖先 `HazeState` 共享模糊源。
  - 调用方需将其置于 root 级容器（如 `Box(Modifier.fillMaxSize())`）中以保证遮罩全屏覆盖。
  - 保留返回键/点击遮罩关闭、居中/底部位置与对应动画。
- `CloverDropdownMenu` 从 `Popup` 改为同层覆盖层绘制：
  - 移除 `Popup`，避免跨窗口导致无法使用页面级 `HazeState`。
  - 调用方需将菜单放在 root 级容器中，避免被父布局裁剪。
- 重新编译并发布 `clover-ui` 到 mavenLocal。

## 2026-07-05 16:22
- 重写 `CloverDialog` 为 Miuix 风格：
  - 使用 `androidx.compose.ui.window.Dialog` 独立窗口显示，支持返回键/点击外部关闭。
  - 面板圆角改为 28.dp，标题/内容默认居中对齐。
  - 新增 `position` 参数，支持 `Center` / `Bottom` 两种位置；底部位置使用与 `CloverBottomSheet` 一致的从下滑入/下滑收起动画。
  - 新增 `confirmButton` / `dismissButton` 便捷参数；居中默认“左侧取消、右侧确认”，底部默认“上方取消、下方确认”（竖向排列）。
  - 新增 `hazeState` 参数，支持亚克力背景。
  - 新增 `CloverDialogConfirmButton` / `CloverDialogDismissButton` 两个便捷按钮组件。
- 重新编译并发布 `clover-ui` 到 mavenLocal。

## 2026-07-05 16:12
- `CloverDropdownMenu` 全面改为 Flyout 风格交互：
  - 使用 `MutableTransitionState` 控制进入/退出动画，退出动画完整播放后才会真正关闭 `Popup`。
  - 在 `Popup` 内部绘制一层很淡的遮罩（scrim），点击遮罩/空白处会先播退出动画再关闭。
  - 关闭系统 `dismissOnClickOutside`，避免与自定义遮罩冲突；返回键和遮罩点击统一处理。
  - 恢复面板内部空 `clickable {}`，防止点击菜单内容时误触发遮罩。
- 重新编译并发布 `clover-ui` 到 mavenLocal。

## 2026-07-05 15:53
- 修复 `CloverDropdownMenu` 在折叠状态下仍然创建 `Popup` 并拦截全屏触摸事件的问题：
  - 将 `Popup` 包进 `if (expanded)`，只在展开时创建。
  - 移除 Popup 内部全屏透明 `clickable` Box，避免消费所有事件导致页面无法点击/返回。
  - 保留 `AnimatedVisibility` 缩放进入动画。
- 重新编译并发布 `clover-ui` 到 mavenLocal。
