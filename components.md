# CloverUI 组件清单

本文件记录 CloverUIforAndroid 中所有公开组件、它们接收的参数、可用变体以及默认行为。
**每次新增、修改或删除组件时，必须同步更新本文档。**

> 文档结构：
> - 组件名（文件路径）
> - 作用一句话描述
> - 接收参数 / 属性
> - 变体（如果有）
> - 注意事项 / 默认值

---

## 目录

1. [设计系统 / Core](#设计系统--core)
2. [效果 / Effects](#效果--effects)
3. [材质 / Material](#材质--material)
4. [偏好设置 / Preference](#偏好设置--preference)
5. [按钮 / Button](#按钮--button)
6. [控件 / Control](#控件--control)
7. [布局与导航 / Layout](#布局与导航--layout)
8. [列表 / List](#列表--list)
9. [浮层 / Overlay](#浮层--overlay)

---

## 设计系统 / Core

### `CloverTheme` (`core/CloverTheme.kt`)

**作用**：Clover Design 主题包裹器，提供局部深色/亮色模式覆盖以及颜色方案。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `colorScheme` | `CloverColorScheme?` | `null` | 自定义颜色方案；传入时优先使用，否则按 `dynamicColor`/`darkTheme` 生成 |
| `dynamicColor` | `Boolean` | `false` | 是否使用系统 Material You 动态取色（Android 12+） |
| `darkTheme` | `Boolean` | `isSystemInDarkTheme()` | 是否使用深色模式 |
| `content` | `@Composable () -> Unit` | 必填 | 主题作用域内容 |

**变体**：

- `CloverTheme(colorScheme, dynamicColor, darkTheme, content)` — 完整版
- `CloverTheme(darkTheme, content)` — 只切换深浅，使用默认静态配色

### `isCloverDark` (`core/CloverTheme.kt`)

**作用**：读取当前 Clover 深色模式，优先使用 `LocalCloverDarkMode` 局部覆盖值，否则跟随系统。

**参数**：无

### `CloverBasicItem` (`core/CloverBasicItem.kt`)

**作用**：通用列表/设置项基座，统一标题、副标题、前后操作区样式与交互。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 主标题文字 |
| `subtitle` | `String?` | `null` | 副标题文字，null 时不显示 |
| `indicator` | `@Composable (() -> Unit)?` | `null` | 左侧选中/播放指示条 |
| `leading` | `@Composable (() -> Unit)?` | `null` | 左侧图标/封面/占位内容 |
| `trailing` | `@Composable (RowScope.() -> Unit)?` | `null` | 右侧控件 |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `onClick` | `(() -> Unit)?` | `null` | 点击回调，null 表示不可点击 |
| `onLongClick` | `(() -> Unit)?` | `null` | 长按回调 |
| `backgroundColor` | `Color` | `Color.Transparent` | 列表项背景色 |
| `shape` | `Shape` | `CloverBasicItemDefaults.shape` | 背景形状/圆角 |
| `colors` | `CloverBasicItemColors` | `CloverBasicItemDefaults.colors()` | 文字颜色配置 |
| `contentPadding` | `PaddingValues` | `CloverBasicItemDefaults.contentPadding` | 内部 padding |
| `indicationCornerRadius` | `Dp` | `CloverBasicItemDefaults.indicationCornerRadius` | 按下高亮圆角 |

### `cloverClickable` (`core/CloverIndication.kt`)

**作用**：Clover 风格点击修饰符，支持单击、长按、圆角高亮按下效果。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `onLongClick` | `() -> Unit` | `{}` | 长按回调 |
| `cornerRadius` | `Dp` | `CloverSizes.listItemCornerRadius` | 高亮圆角 |
| `color` | `Color?` | `null` | 自定义高亮色，null 时使用当前主题 onSurface |

---

## 效果 / Effects

### `CloverHazeDefaults` (`effects/CloverHaze.kt`)

**作用**：Haze 效果默认参数与降级策略管理。

**属性 / 函数**：

| 名称 | 类型 | 说明 |
|------|------|------|
| `isSupported` | `Boolean` | 当前设备是否支持 Haze（Android 12+） |
| `tints(baseColor, useLuminosity, useExclusion)` | `List<HazeTint>` | 生成 Clover 风格 HazeTint 组合 |
| `surfaceTint(alpha)` | `Color` | 带默认透明度的表面叠色 |

### `rememberCloverHazeState` (`effects/CloverHaze.kt`)

**作用**：创建 Clover 风格的 `HazeState`。

**参数**：无

### `CloverRevealState` / `rememberCloverRevealState` (`effects/CloverReveal.kt`)

**作用**：共享 Reveal 光照层状态。

**参数**：无

### `CloverRevealHost` (`effects/CloverReveal.kt`)

**作用**：共享 Reveal 指针状态容器，内部子项可通过 `cloverRevealItem` 绘制边缘辉光；Host 本身不再绘制跨元素聚光灯。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `state` | `CloverRevealState` | `rememberCloverRevealState()` | Reveal 状态 |
| `enabled` | `Boolean` | `true` | 是否启用 |
| `content` | `@Composable () -> Unit` | 必填 | 子项内容 |

### `cloverRevealItem` (`effects/CloverReveal.kt`)

**作用**：给 `CloverRevealHost` 内部子项添加边缘辉光；指针靠近/按压时元素边框发出主题色辉光，辉光严格限制在元素自身边界。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `shape` | `Shape` | `CloverRevealDefaults.shape` | 项的形状 |
| `enabled` | `Boolean` | `true` | 是否启用 |

### `cloverReveal` (`effects/CloverReveal.kt`)

**作用**：给单个组件添加独立 Reveal 边缘辉光 + 按压水波纹；不在 Host 内部时使用。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `shape` | `Shape` | `CloverRevealDefaults.shape` | 裁剪形状 |
| `enabled` | `Boolean` | `true` | 是否启用 |

### `CloverRevealDefaults` (`effects/CloverReveal.kt`)

**作用**：Reveal 默认参数与颜色。

**属性 / 函数**：

| 名称 | 类型 | 说明 |
|------|------|------|
| `shape` | `Shape` | 默认圆角 8.dp |
| `revealDistance` | `Dp` | 触发边缘辉光的距离 |
| `borderWidth` | `Dp` | 辉光边框宽度 |
| `glowWidth` | `Dp` | 辉光向外扩散宽度 |
| `backgroundColor()` | `Color` | 默认背景色 |
| `glowColor()` | `Color` | 当前主题辉光色（浅色主色、深色主色/白色） |
| `rippleColor()` | `Color` | 当前主题水波纹色 |

### `CloverRevealSurface` (`effects/CloverReveal.kt`)

**作用**：带 Reveal 按压光效的按钮/卡片表面。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `shape` | `Shape` | `CloverRevealDefaults.shape` | 形状 |
| `backgroundColor` | `Color` | `CloverRevealDefaults.backgroundColor()` | 背景色 |
| `contentColor` | `Color` | 自动黑/白 | 内容色 |
| `contentPadding` | `PaddingValues` | `CloverRevealDefaults.contentPadding` | 内边距 |
| `content` | `@Composable RowScope.() -> Unit` | 必填 | 内容 |

---

## 材质 / Material

### `CloverMaterial` (`material/CloverMaterial.kt`)

**作用**：Acrylic / Mica 材质参数集中管理。

**子对象**：

| 名称 | 说明 |
|------|------|
| `CloverMaterial.Acrylic` | 亚克力材质参数：blurRadius、noiseFactor、tintAlpha、backgroundColor()、tints() |
| `CloverMaterial.Mica` | 云母材质参数：blurRadius、noiseTextureSize、tintAlpha/Alt、noiseAlpha/Alt、tintColor(isAlt)、fallbackColor() |

### `cloverAcrylic` (`material/CloverMaterial.kt`)

**作用**：将 Modifier 应用 Clover Acrylic 效果。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `state` | `HazeState` | 必填 | Haze 状态 |
| `shape` | `Shape?` | `null` | 可选裁剪形状 |
| `backgroundColor` | `Color?` | `null` | Haze 失效时的 fallback 背景色 |
| `tints` | `List<HazeTint>?` | `null` | 叠色层 |
| `blurRadius` | `Dp` | `CloverMaterial.Acrylic.blurRadius` | 模糊半径 |
| `noiseFactor` | `Float` | `CloverMaterial.Acrylic.noiseFactor` | 噪声强度 |

### `CloverAcrylicSurface` (`material/CloverMaterial.kt`)

**作用**：Acrylic 表面容器，等价于对 Box 应用 `cloverAcrylic`。

**参数**：与 `cloverAcrylic` 基本一致，外加 `content`。

### `cloverMica` (`material/CloverMica.kt`)

**作用**：将 Modifier 应用 Clover Mica（云母）材质背景。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `isAlt` | `Boolean` | `false` | Mica Alt 变体 |
| `strategy` | `WallpaperLoadStrategy` | `Drawable` | 壁纸加载策略 |
| `shape` | `Shape?` | `null` | 可选裁剪形状 |
| `tintColor` | `Color?` | `null` | 叠色 |
| `tintAlpha` | `Float` | 按 isAlt | 叠色透明度 |
| `noiseAlpha` | `Float` | 按 isAlt | 噪点透明度 |
| `blurRadius` | `Dp` | `CloverMaterial.Mica.blurRadius` | 壁纸模糊强度 |

### `CloverMicaSurface` (`material/CloverMica.kt`)

**作用**：Mica 表面容器。

**参数**：与 `cloverMica` 基本一致，外加 `content`。

### `rememberWallpaperBitmap` (`material/CloverMica.kt`)

**作用**：按指定策略获取当前桌面壁纸（未模糊），返回 null 表示无法获取。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `strategy` | `WallpaperLoadStrategy` | `Drawable` | 壁纸加载策略 |

### `CloverWallpaperMica` (`material/CloverMica.kt`)

**作用**：系统壁纸实时 Mica，基于 windowShowWallpaper + Android 12 跨窗口模糊。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `isAlt` | `Boolean` | `false` | Mica Alt 变体 |
| `enabled` | `Boolean` | `true` | 是否启用窗口模糊 |
| `blurRadius` | `Dp` | `CloverMaterial.Mica.blurRadius` | 模糊半径 |
| `tintColor` | `Color?` | `null` | 叠色 |
| `tintAlpha` | `Float` | 按 isAlt | 叠色透明度 |
| `noiseAlpha` | `Float` | 按 isAlt | 噪点透明度 |
| `scrimColor` | `Color` | `Color.Transparent` | 额外遮罩色 |
| `content` | `@Composable BoxScope.() -> Unit` | 必填 | 内容 |

### `CloverOutsideStroke` (`material/CloverOutsideStroke.kt`)

**作用**：在内容外侧绘制描边，不会压缩内容本身。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `width` | `Dp` | 必填 | 描边宽度 |
| `color` | `Color` | 必填 | 描边颜色 |
| `sides` | `Set<CloverStrokeSide>` | `ALL` | 描边方向 |
| `shape` | `Shape` | `RectangleShape` | 裁剪形状 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `content` | `@Composable () -> Unit` | 必填 | 内容 |

**变体**：

- `CloverOutsideStroke(width, color, side, shape, modifier, content)` — 只在单侧描边

---

## 偏好设置 / Preference

### `CloverSettingItem` (`preference/CloverSettingItem.kt`)

**作用**：设置页面行，基于 `CloverBasicItem`。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 标题 |
| `subtitle` | `String?` | `null` | 副标题/说明 |
| `icon` | `ImageVector?` | `null` | 左侧图标 |
| `trailing` | `@Composable (RowScope.() -> Unit)?` | `null` | 右侧控件 |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `onClick` | `(() -> Unit)?` | `null` | 点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |

---

## 按钮 / Button

### `CloverButton` (`ui/button/CloverButton.kt`)

**作用**：Clover 通用按钮，通过 colors 组合出填充、描边、文字等样式。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `size` | `CloverButtonSize` | `Medium` | 尺寸：Small / Medium / Large |
| `colors` | `CloverButtonColors` | `CloverButtonDefaults.filledColors()` | 颜色配置 |
| `shape` | `Shape` | `CloverButtonDefaults.shape` | 形状 |
| `reveal` | `Boolean` | `true` | 是否启用 Reveal 按压光效 |
| `contentPadding` | `PaddingValues?` | `null` | 自定义内容内边距 |
| `content` | `@Composable RowScope.() -> Unit` | 必填 | 按钮内容 |

**变体**：

- `CloverButton(text, onClick, ...)` — 实心文字按钮
- `CloverOutlinedButton(text, onClick, ...)` — 描边文字按钮
- `CloverTextButton(text, onClick, ...)` — 纯文字按钮
- `CloverIconButton(icon, contentDescription, onClick, ...)` — 图标按钮

**颜色预设**：

- `CloverButtonDefaults.filledColors(...)` — 实心按钮颜色
- `CloverButtonDefaults.outlinedColors(...)` — 描边按钮颜色
- `CloverButtonDefaults.textColors(...)` — 文字按钮颜色

---

## 控件 / Control

### `CloverSwitch` (`ui/control/CloverSwitch.kt`)

**作用**：开关。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `checked` | `Boolean` | 必填 | 当前是否打开 |
| `onCheckedChange` | `(Boolean) -> Unit` | 必填 | 状态变化回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |

### `CloverSwitchItem` (`ui/control/CloverSwitch.kt`)

**作用**：带文字的 Switch 行。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 标题 |
| `checked` | `Boolean` | 必填 | 当前是否打开 |
| `onCheckedChange` | `(Boolean) -> Unit` | 必填 | 状态变化回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `subtitle` | `String?` | `null` | 副标题 |

### `CloverCheckbox` (`ui/control/CloverCheckbox.kt`)

**作用**：复选框。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `checked` | `Boolean` | 必填 | 是否选中 |
| `onCheckedChange` | `(Boolean) -> Unit` | 必填 | 状态变化回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |

### `CloverRadioButton` (`ui/control/CloverRadioButton.kt`)

**作用**：单选按钮。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `selected` | `Boolean` | 必填 | 是否选中 |
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |

### `CloverSlider` (`ui/control/CloverSlider.kt`)

**作用**：滑块，基于 Material3 Slider 映射 Clover 颜色。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `Float` | 必填 | 当前值 |
| `onValueChange` | `(Float) -> Unit` | 必填 | 值变化回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `enabled` | `Boolean` | `true` | 是否可用 |
| `valueRange` | `ClosedFloatingPointRange<Float>` | `0f..1f` | 取值范围 |

### `CloverLinearProgressIndicator` (`ui/control/CloverProgress.kt`)

**作用**：线性进度条。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `progress` | `Float?` | `null` | 当前进度 0..1，null 表示不确定进度 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `color` | `Color` | `primary` | 进度颜色 |
| `trackColor` | `Color` | `surfaceVariant` | 轨道颜色 |

### `CloverCircularProgressIndicator` (`ui/control/CloverProgress.kt`)

**作用**：圆形进度条。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `progress` | `Float?` | `null` | 当前进度 0..1，null 表示不确定进度 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `color` | `Color` | `primary` | 进度颜色 |
| `trackColor` | `Color` | `surfaceVariant` | 轨道颜色 |
| `size` | `Dp` | `48.dp` | 直径 |
| `strokeWidth` | `Dp` | `4.dp` | 线宽 |

### `CloverDropdownMenu` (`ui/control/CloverDropdownMenu.kt`)

**作用**：下拉菜单容器。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `expanded` | `Boolean` | 必填 | 是否展开 |
| `onDismissRequest` | `() -> Unit` | 必填 | 关闭回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `offset` | `DpOffset` | `DpOffset(0.dp, 6.dp)` | 相对锚点的偏移 |
| `content` | `@Composable ColumnScope.() -> Unit` | 必填 | 菜单内容 |

### `CloverDropdownMenuItem` (`ui/control/CloverDropdownMenu.kt`)

**作用**：下拉菜单项。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `text` | `String` | 必填 | 菜单文字 |
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `leadingIcon` | `@Composable (() -> Unit)?` | `null` | 左侧图标 |
| `trailingIcon` | `@Composable (() -> Unit)?` | `null` | 右侧图标 |
| `enabled` | `Boolean` | `true` | 是否可用 |

---

## 布局与导航 / Layout

### `CloverTopAppBar` (`ui/layout/CloverTopAppBar.kt`)

**作用**：顶部标题栏（MD3 Small TopAppBar 风格），用于大屏幕设备。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `@Composable () -> Unit` | 必填 | 标题内容 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `backgroundColor` | `Color` | `surface` | 背景色 |
| `contentColor` | `Color` | `onSurface` | 内容/图标颜色 |
| `navigationIcon` | `@Composable (() -> Unit)?` | `null` | 左侧导航图标 |
| `actions` | `@Composable RowScope.() -> Unit` | `{}` | 右侧操作区 |

### `CloverTitleBar` (`ui/layout/CloverTitleBar.kt`)

**作用**：底部标题栏，位于屏幕最底部（单手可达区域）。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `@Composable () -> Unit` | 必填 | 标题内容 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `backgroundColor` | `Color?` | `null` | 背景色，null 表示透明 |
| `leading` | `@Composable (() -> Unit)?` | `null` | 左侧操作区 |
| `trailing` | `@Composable RowScope.() -> Unit` | `{}` | 右侧操作区 |

### `CloverBottomNavbar` (`ui/layout/CloverBottomNavbar.kt`)

**作用**：底部导航栏。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `items` | `List<CloverNavItem>` | 必填 | 导航项列表 |
| `selectedIndex` | `Int` | 必填 | 当前选中项索引 |
| `onItemSelected` | `(Int) -> Unit` | 必填 | 选中回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |

### `CloverNavigationRail` (`ui/layout/CloverNavigationRail.kt`)

**作用**：侧边导航栏（紧凑版），用于平板/折叠屏/桌面。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `items` | `List<CloverNavItem>` | 必填 | 导航项列表 |
| `selectedIndex` | `Int` | 必填 | 当前选中项索引 |
| `onItemSelected` | `(Int) -> Unit` | 必填 | 选中回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `backgroundColor` | `Color?` | `null` | 背景色 |
| `header` | `@Composable (() -> Unit)?` | `null` | 顶部额外内容 |
| `footer` | `@Composable (() -> Unit)?` | `null` | 底部额外内容 |

### `CloverNavigationView` (`ui/layout/CloverNavigationView.kt`)

**作用**：左侧导航视图（宽版），Fluent Design 风格。

**参数**：与 `CloverNavigationRail` 一致。

### `CloverAdaptiveScaffold` (`ui/layout/CloverAdaptiveScaffold.kt`)

**作用**：自适应页面骨架，通过 strategy 决定标题栏、导航栏摆放位置。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `strategy` | `CloverAdaptiveStrategy` | 必填 | 布局策略 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `topBar` | `@Composable (() -> Unit)?` | `null` | 顶部栏 slot |
| `bottomBar` | `@Composable (() -> Unit)?` | `null` | 底部栏 slot |
| `navigationBar` | `@Composable (() -> Unit)?` | `null` | 侧边导航栏 slot |
| `floatingActionButton` | `@Composable (() -> Unit)?` | `null` | 浮动按钮 slot |
| `barMaterial` | `CloverBarMaterial` | `Acrylic` | 栏位材质 |
| `content` | `@Composable (PaddingValues) -> Unit` | 必填 | 页面主体内容 |

**`CloverAdaptiveStrategy` 变体**：

- `Classic` — 顶部 TopAppBar + 底部 BottomNavbar
- `BottomCombined` — 底部 TitleBar + 底部 BottomNavbar
- `RailWithTopBar` — 左侧 NavigationRail + 顶部 TopAppBar
- `RailWithBottomBar` — 左侧 NavigationRail + 底部 TitleBar
- `ViewWithTopBar` — 左侧 NavigationView + 顶部 TopAppBar
- `ViewWithBottomBar` — 左侧 NavigationView + 底部 TitleBar

**`CloverBarMaterial` 变体**：

- `None` — 无特殊材质
- `Acrylic` — 亚克力材质
- `Solid(color)` — 纯色材质

### `CloverAdaptiveNavigableScaffold` (`ui/layout/CloverAdaptiveScaffold.kt`)

**作用**：带导航功能的自适应页面骨架，内部自动组合 TopAppBar / TitleBar / BottomNavbar / NavigationRail / NavigationView。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `@Composable () -> Unit` | 必填 | 页面标题 |
| `items` | `List<CloverNavItem>` | 必填 | 导航项 |
| `selectedIndex` | `Int` | 必填 | 当前选中索引 |
| `onItemSelected` | `(Int) -> Unit` | 必填 | 选中回调 |
| `strategy` | `CloverAdaptiveStrategy` | `BottomCombined` | 布局策略 |
| `navigationStyle` | `CloverNavigationStyle` | `Rail` | 侧边导航形态：Rail / View |
| `topBarActions` | `@Composable RowScope.() -> Unit` | `{}` | 顶部栏右侧操作 |
| `bottomBarLeading` | `@Composable (() -> Unit)?` | `null` | 底部标题栏左侧 |
| `bottomBarTrailing` | `@Composable RowScope.() -> Unit` | `{}` | 底部标题栏右侧 |
| `floatingActionButton` | `@Composable (() -> Unit)?` | `null` | 浮动按钮 |
| `barMaterial` | `CloverBarMaterial` | `Acrylic` | 栏位材质 |
| `content` | `@Composable (PaddingValues) -> Unit` | 必填 | 页面内容 |

### `CloverAdaptivePageScaffold` (`ui/layout/CloverAdaptivePageScaffold.kt`)

**作用**：响应式二级页面脚手架（设置、关于等没有底部导航的页面）。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `@Composable () -> Unit` | 必填 | 页面标题 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `navigationIcon` | `@Composable (() -> Unit)?` | `null` | 左侧导航/返回图标 |
| `actions` | `@Composable RowScope.() -> Unit` | `{}` | 右侧操作按钮 |
| `content` | `@Composable (PaddingValues) -> Unit` | 必填 | 页面内容 |

### `cloverWindowWidthSizeClass` (`ui/layout/CloverWindowSize.kt`)

**作用**：获取当前窗口宽度尺寸类（Compact / Medium / Expanded）。

**参数**：无

### `cloverIsCompactWidth` (`ui/layout/CloverWindowSize.kt`)

**作用**：判断当前是否为紧凑宽度（手机竖屏）。

**参数**：无

---

## 列表 / List

### `CloverListItem` (`ui/list/CloverListItem.kt`)

**作用**：标准列表项，带左侧选中/播放指示条。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 主标题 |
| `subtitle` | `String?` | `null` | 副标题 |
| `onClick` | `() -> Unit` | `{}` | 点击回调 |
| `onLongClick` | `() -> Unit` | `{}` | 长按回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `leading` | `@Composable (() -> Unit)?` | `null` | 左侧内容 |
| `trailing` | `@Composable (() -> Unit)?` | `null` | 右侧内容 |
| `isSelected` | `Boolean` | `false` | 是否选中（显示指示条） |
| `isPlaying` | `Boolean` | `false` | 是否正在播放（显示更高指示条） |
| `isLarge` | `Boolean` | `false` | 是否使用大高度（64.dp） |
| `backgroundColor` | `Color` | `Color.Transparent` | 背景色 |

### `CloverHorizontalListItem` (`ui/list/CloverHorizontalListItem.kt`)

**作用**：横向列表项，封面在上、文字在下，用于横向滚动列表。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 主标题 |
| `subtitle` | `String?` | `null` | 副标题 |
| `onClick` | `() -> Unit` | `{}` | 点击回调 |
| `onLongClick` | `() -> Unit` | `{}` | 长按回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `cover` | `@Composable () -> Unit` | 必填 | 封面/头像内容 |
| `width` | `Dp` | `140.dp` | 整个 item 宽度 |
| `coverSize` | `Dp` | `140.dp` | 封面尺寸 |
| `shape` | `RoundedCornerShape` | `RoundedCornerShape(CloverSizes.coverCornerRadius)` | 封面圆角 |

### `CloverSectionHeader` (`ui/list/CloverSectionHeader.kt`)

**作用**：列表分组标题。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 标题文字 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |

### `CloverEmptyList` (`ui/list/CloverEmptyList.kt`)

**作用**：列表空状态组件。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | 必填 | 主标题 |
| `message` | `String` | 必填 | 说明文字 |
| `icon` | `ImageVector?` | `null` | 插图图标 |
| `actionLabel` | `String?` | `null` | 操作按钮文字 |
| `onActionClick` | `() -> Unit` | `{}` | 操作按钮点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |

### `CloverListSkeleton` (`ui/list/CloverListSkeleton.kt`)

**作用**：列表骨架屏，数据加载时展示占位列表项。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | `Int` | `6` | 占位行数 |

---

## 浮层 / Overlay

### `CloverDialog` (`ui/overlay/CloverDialog.kt`)

**作用**：通用居中圆角弹窗，包含标题、自定义内容和底部操作按钮。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `onDismissRequest` | `() -> Unit` | 必填 | 点击遮罩或返回键关闭回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `title` | `String?` | `null` | 标题文字 |
| `buttons` | `@Composable RowScope.() -> Unit` | `{}` | 底部按钮区 |
| `content` | `@Composable ColumnScope.() -> Unit` | 必填 | 自定义内容 |

### `CloverFlyout` (`ui/overlay/CloverFlyout.kt`)

**作用**：弹出菜单，带遮罩、缩放动画、系统返回键关闭。

**参数（items 便捷重载）**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `visible` | `Boolean` | 必填 | 是否显示 |
| `onDismiss` | `() -> Unit` | 必填 | 关闭回调 |
| `items` | `List<CloverFlyoutItem>` | 必填 | 菜单项列表 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `anchorBounds` | `Rect?` | `null` | 触发控件边界 |
| `hazeState` | `HazeState?` | `null` | 页面级 Haze 源 |

**参数（content 完整版）**：与 items 版相比把 `items` 换成 `content: @Composable ColumnScope.() -> Unit`。

**数据类 `CloverFlyoutItem`**：

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `label` | `String` | 必填 | 显示文字 |
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `icon` | `ImageVector?` | `null` | 可选左侧图标 |
| `isDestructive` | `Boolean` | `false` | 是否危险操作（红色高亮） |

### `CloverBottomSheet` (`ui/overlay/CloverBottomSheet.kt`)

**作用**：底部 Sheet 容器，带拖拽手柄、下拉收起、预测返回手势。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `onDismiss` | `() -> Unit` | 必填 | Sheet 完全收起后触发 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `title` | `String?` | `null` | 可选标题 |
| `hazeState` | `HazeState?` | `null` | 可选 Haze 状态，提供时使用亚克力背景 |
| `hazeTints` | `List<HazeTint>` | `emptyList()` | Haze 色调 |
| `content` | `@Composable ColumnScope.() -> Unit` | 必填 | Sheet 内容 |

### `CloverMenuItem` (`ui/overlay/CloverBottomSheet.kt`)

**作用**：Sheet / Flyout 内菜单项，图标 + 文字一行，支持危险操作红色高亮和选中指示条。

**参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `label` | `String` | 必填 | 文字 |
| `onClick` | `() -> Unit` | 必填 | 点击回调 |
| `modifier` | `Modifier` | `Modifier` | 外部 modifier |
| `icon` | `ImageVector?` | `null` | 左侧图标 |
| `isSelected` | `Boolean` | `false` | 是否选中 |
| `isDestructive` | `Boolean` | `false` | 是否为危险操作 |

---

## 统计

- **Core**：7 个组件/修饰符
- **Effects**：6 个组件/修饰符
- **Material**：6 个组件/修饰符
- **Preference**：1 个组件
- **Button**：4 个公开函数（1 个基础 + 3 个变体）
- **Control**：8 个组件
- **Layout**：9 个组件
- **List**：5 个组件
- **Overlay**：4 个组件

**当前总计：50 个公开 API（含变体与数据类）**。

---

*最后更新：2026-07-02*
