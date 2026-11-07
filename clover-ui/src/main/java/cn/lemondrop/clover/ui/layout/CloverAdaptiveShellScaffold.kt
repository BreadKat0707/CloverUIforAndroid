package cn.lemondrop.clover.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.CloverBottomNavbar
import cn.lemondrop.clover.CloverBottomNavbarDefaults
import cn.lemondrop.clover.CloverNavItem
import cn.lemondrop.clover.CloverNavigationRail
import cn.lemondrop.clover.CloverSizes
import cn.lemondrop.clover.CloverTitleBar
import cn.lemondrop.clover.LocalCloverColorScheme
import cn.lemondrop.clover.cloverIsCompactWidth
import cn.lemondrop.clover.material.CloverMaterial
import cn.lemondrop.clover.material.cloverSolid
import cn.lemondrop.clover.rememberCloverHazeState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * 标题栏位置。
 */
enum class CloverShellTitleBarPosition {
    /** 顶部标题栏 */
    Top,
    /** 底部标题栏（单手操作） */
    Bottom
}

/**
 * 导航栏位置。
 */
enum class CloverShellNavigationPosition {
    /** 起始侧（默认左侧，RTL 为右侧） */
    Start,
    /** 结束侧（默认右侧，RTL 为左侧） */
    End,
    /** 底部 */
    Bottom,
    /** 不显示 */
    Hidden
}

/**
 * 侧边导航栏形态。
 */
enum class CloverShellNavigationStyle {
    /** 窄版 NavigationRail（图标 + 文字垂直，80dp） */
    Rail,
    /** Fluent NavigationView（展开：左图标右文字；收折：仅图标） */
    View
}

/**
 * Clover 自适应外壳布局策略。
 *
 * @param titleBarPosition 标题栏位置：顶部或底部
 * @param navigationPosition 导航栏位置：起始侧、结束侧、底部或不显示
 * @param navigationStyle 侧边导航栏形态（仅当导航栏在 Start/End 时生效）
 */
@Stable
data class CloverShellStrategy(
    val titleBarPosition: CloverShellTitleBarPosition = CloverShellTitleBarPosition.Top,
    val navigationPosition: CloverShellNavigationPosition = CloverShellNavigationPosition.Bottom,
    val navigationStyle: CloverShellNavigationStyle = CloverShellNavigationStyle.Rail
) {
    companion object {
        /** 常规桌面/横屏：顶部标题栏 + 左侧窄 Rail */
        val Classic = CloverShellStrategy(
            titleBarPosition = CloverShellTitleBarPosition.Top,
            navigationPosition = CloverShellNavigationPosition.Start,
            navigationStyle = CloverShellNavigationStyle.Rail
        )

        /** 单手操作：底部标题栏 + 底部导航栏（标题栏在导航栏上方） */
        val BottomCombined = CloverShellStrategy(
            titleBarPosition = CloverShellTitleBarPosition.Bottom,
            navigationPosition = CloverShellNavigationPosition.Bottom,
            navigationStyle = CloverShellNavigationStyle.Rail
        )

        /** 常规手机：顶部标题栏 + 底部导航栏 */
        val TopBarWithBottomNav = CloverShellStrategy(
            titleBarPosition = CloverShellTitleBarPosition.Top,
            navigationPosition = CloverShellNavigationPosition.Bottom,
            navigationStyle = CloverShellNavigationStyle.Rail
        )
    }
}

/**
 * 侧边导航栏位置
 *
 * 已废弃：使用 [CloverShellStrategy.navigationPosition] 代替。
 */
@Deprecated("Use CloverShellStrategy.navigationPosition instead")
enum class CloverShellNavigationSide {
    /** 根据布局方向放在起始侧（默认左侧，RTL 为右侧） */
    Start,
    /** 根据布局方向放在结束侧（默认右侧，RTL 为左侧） */
    End
}

/**
 * Clover 自适应外壳状态
 *
 * 提供 [CloverAdaptiveShellScaffold] 内部共享的 HazeState、布局状态以及 content 安全内边距。
 */
@Stable
interface CloverShellState {
    /** 页面级 Haze 状态，供 overlay 中的 sheet/flyout/dialog 共享模糊源 */
    val hazeState: HazeState

    /** 当前是否为紧凑宽度（手机竖屏） */
    val isCompactWidth: Boolean

    /** 当前布局方向 */
    val layoutDirection: LayoutDirection

    /** 当前布局策略 */
    val strategy: CloverShellStrategy

    /** 内容区应避开外壳条带的安全内边距 */
    val contentPadding: PaddingValues
}

/**
 * Clover 自适应页面外壳脚手架
 *
 * 推荐的主力布局组件，支持 edge-to-edge。
 *
 * 设计要点：
 * - 内容区全屏绘制到状态栏、系统导航栏、标题栏和导航栏的后方，作为 Haze 模糊源。
 * - 顶部标题栏 / 侧边导航栏背景延伸到状态栏下方，底部栏背景延伸到系统导航栏下方。
 * - 标题栏与导航栏的交界处使用 1dp 分隔线，不再使用外侧描边。
 * - 调用方应使用 [CloverShellState.contentPadding] 让内容文字/控件避开外壳条带。
 * - overlay 与 content 处于同一 Box 层级，可共享 [HazeState]。
 *
 * @param modifier 外部 modifier
 * @param externalHazeState 外部 Haze 状态；传入时与浮层共享同一个模糊源，不传则内部创建
 * @param background 统一背景材质，默认透明以允许 App 级背景（如壁纸）透出
 * @param title 标题内容
 * @param navigationIcon 左侧导航/返回图标
 * @param actions 右侧操作按钮
 * @param items 导航项列表
 * @param selectedIndex 当前选中导航项索引
 * @param onItemSelected 导航项选中回调
 * @param strategy 布局策略，默认 [CloverShellStrategy.TopBarWithBottomNav]
 * @param railHeader 导航 Rail/View 顶部额外内容
 * @param railFooter 导航 Rail/View 底部额外内容
 * @param overlay 弹窗/浮层/Sheet/Flyout/Dialog 放置区，可拿到 [CloverShellState]
 * @param content 页面主体内容
 */
@Composable
fun CloverAdaptiveShellScaffold(
    modifier: Modifier = Modifier,
    externalHazeState: HazeState? = null,
    background: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    items: List<CloverNavItem> = emptyList(),
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    strategy: CloverShellStrategy = CloverShellStrategy.TopBarWithBottomNav,
    railHeader: @Composable (() -> Unit)? = null,
    railFooter: @Composable (() -> Unit)? = null,
    overlay: @Composable BoxScope.(CloverShellState) -> Unit = {},
    content: @Composable BoxScope.(CloverShellState) -> Unit
) {
    val localHazeState = rememberCloverHazeState()
    val resolvedHazeState = externalHazeState ?: localHazeState
    val isCompact = cloverIsCompactWidth()
    val layoutDirection = LocalLayoutDirection.current

    // 安全区域：包含状态栏、导航栏、刘海/挖孔的 start/end
    val safeDrawingInsets = WindowInsets.safeDrawing.asPaddingValues()
    val statusBarTop = WindowInsets.statusBarsIgnoringVisibility
        .asPaddingValues()
        .calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()

    val hasTopBar = strategy.titleBarPosition == CloverShellTitleBarPosition.Top
    val hasBottomTitleBar = strategy.titleBarPosition == CloverShellTitleBarPosition.Bottom
    val hasBottomNav = strategy.navigationPosition == CloverShellNavigationPosition.Bottom && items.isNotEmpty()
    val hasSideNav = strategy.navigationPosition == CloverShellNavigationPosition.Start ||
            strategy.navigationPosition == CloverShellNavigationPosition.End
    val hasRail = hasSideNav && strategy.navigationStyle != CloverShellNavigationStyle.View && items.isNotEmpty()
    val hasView = hasSideNav && strategy.navigationStyle == CloverShellNavigationStyle.View && items.isNotEmpty()

    val navWidth = when {
        hasRail -> CloverSizes.navigationRailWidth
        hasView -> CloverSizes.navigationViewWidth
        else -> 0.dp
    }
    val topBarHeight = if (hasTopBar) CloverSizes.titleBarHeight else 0.dp
    val bottomBarHeight = if (hasBottomTitleBar || hasBottomNav) {
        (if (hasBottomTitleBar) CloverSizes.titleBarHeight else 0.dp) +
                (if (hasBottomNav) CloverBottomNavbarDefaults.ItemHeight else 0.dp)
    } else 0.dp

    val safeStart = safeDrawingInsets.calculateStartPadding(layoutDirection)
    val safeEnd = safeDrawingInsets.calculateEndPadding(layoutDirection)

    // 内容区 Padding：只避开外壳条带本身，不额外避开状态栏/导航栏（因为内容已画到它们后面）
    val contentPadding = remember(
        isCompact,
        layoutDirection,
        strategy,
        navWidth,
        topBarHeight,
        bottomBarHeight,
        safeStart,
        safeEnd
    ) {
        PaddingValues(
            start = if (hasSideNav && strategy.navigationPosition == CloverShellNavigationPosition.Start) {
                safeStart + navWidth
            } else {
                safeStart
            },
            top = topBarHeight,
            end = if (hasSideNav && strategy.navigationPosition == CloverShellNavigationPosition.End) {
                safeEnd + navWidth
            } else {
                safeEnd
            },
            bottom = bottomBarHeight + navBarBottom
        )
    }

    val shellState = remember(resolvedHazeState, isCompact, layoutDirection, strategy, contentPadding) {
        object : CloverShellState {
            override val hazeState: HazeState = resolvedHazeState
            override val isCompactWidth: Boolean = isCompact
            override val layoutDirection: LayoutDirection = layoutDirection
            override val strategy: CloverShellStrategy = strategy
            override val contentPadding: PaddingValues = contentPadding
        }
    }

    val dividerColor = LocalCloverColorScheme.current.outline.copy(alpha = 0.4f)

    Box(modifier = modifier.fillMaxSize()) {
        // 1) 统一背景材质
        background()

        // 2) 内容层：作为 Haze 模糊源，全屏绘制到条带后方以实现 edge-to-edge
        // 如果外部已传入 HazeState，则假设外部（如 DemoBackgroundHost）已提供 hazeSource，
        // 这里不再重复注册，避免多个 source 节点共享同一个 HazeState 导致 zIndex 冲突/透明。
        val contentModifier = if (externalHazeState == null) {
            Modifier
                .fillMaxSize()
                .hazeSource(state = resolvedHazeState)
        } else {
            Modifier.fillMaxSize()
        }
        Box(modifier = contentModifier) {
            content(shellState)
        }

        // 3) 顶部标题栏背景（延伸到状态栏）
        if (hasTopBar) {
            val topStartPadding = if (hasSideNav && strategy.navigationPosition == CloverShellNavigationPosition.Start) {
                safeStart + navWidth
            } else safeStart
            val topEndPadding = if (hasSideNav && strategy.navigationPosition == CloverShellNavigationPosition.End) {
                safeEnd + navWidth
            } else safeEnd

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = topStartPadding, end = topEndPadding)
                    .height(statusBarTop + topBarHeight)
                    .align(Alignment.TopCenter)
                    .cloverSolid(
                        backgroundColor = CloverMaterial.Solid.backgroundColor()
                    )
            )

            // 顶部分隔线：标题栏下方
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = topStartPadding, end = topEndPadding)
                    .padding(top = statusBarTop + topBarHeight)
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(dividerColor)
            )

            // 顶部标题栏内容
            CloverTitleBar(
                title = title,
                leading = navigationIcon,
                trailing = actions,
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = topStartPadding, top = statusBarTop, end = topEndPadding)
                    .height(topBarHeight)
                    .align(Alignment.TopCenter)
            )
        }

        // 4) 侧边导航栏（Rail 或 NavigationView）
        if (hasSideNav) {
            val isStart = strategy.navigationPosition == CloverShellNavigationPosition.Start
            val navAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
            val navHorizontalPadding = if (isStart) {
                PaddingValues(start = safeStart)
            } else {
                PaddingValues(end = safeEnd)
            }
            val sideNavTop = statusBarTop + if (hasTopBar) topBarHeight else 0.dp

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(navHorizontalPadding)
                    .padding(top = sideNavTop, bottom = navBarBottom)
                    .width(navWidth)
                    .align(navAlignment)
                    .cloverSolid(
                        backgroundColor = CloverMaterial.Solid.backgroundColor()
                    )
            )

            // 侧边分隔线
            val dividerModifier = if (isStart) {
                Modifier
                    .fillMaxHeight()
                    .padding(top = sideNavTop, bottom = navBarBottom)
                    .padding(start = safeStart + navWidth)
                    .width(1.dp)
                    .align(Alignment.CenterStart)
            } else {
                Modifier
                    .fillMaxHeight()
                    .padding(top = sideNavTop, bottom = navBarBottom)
                    .padding(end = safeEnd + navWidth)
                    .width(1.dp)
                    .align(Alignment.CenterEnd)
            }
            Box(
                modifier = dividerModifier.background(dividerColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(navHorizontalPadding)
                    .padding(top = sideNavTop, bottom = navBarBottom)
                    .width(navWidth)
                    .align(navAlignment)
            ) {
                if (hasRail) {
                    CloverNavigationRail(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        backgroundColor = Color.Transparent,
                        header = railHeader?.let { { it() } },
                        footer = railFooter?.let { { it() } },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    var viewExpanded by remember { mutableStateOf(true) }
                    cn.lemondrop.clover.ui.layout.CloverNavigationView(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        expanded = viewExpanded,
                        onToggleExpanded = { viewExpanded = !viewExpanded },
                        backgroundColor = Color.Transparent,
                        header = railHeader?.let { { it() } },
                        footer = railFooter?.let { { it() } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 5) 底部栏：Bottom TitleBar 和/或 BottomNavbar
        if (hasBottomTitleBar || hasBottomNav) {
            // 5a) 系统导航栏下方背景（与底部栏同色，避免导航栏区域透明）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = safeStart, end = safeEnd)
                    .height(navBarBottom)
                    .align(Alignment.BottomCenter)
                    .cloverSolid(
                        backgroundColor = CloverMaterial.Solid.backgroundColor()
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = safeStart, end = safeEnd)
                    .padding(bottom = navBarBottom)
                    .height(bottomBarHeight)
                    .align(Alignment.BottomCenter)
                    .cloverSolid(
                        backgroundColor = CloverMaterial.Solid.backgroundColor()
                    )
            ) {
                // 底部栏顶部分隔线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor)
                )

                // 单手操作：导航栏在标题栏上方
                if (hasBottomNav) {
                    CloverBottomNavbar(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (hasBottomTitleBar) {
                    CloverTitleBar(
                        title = title,
                        leading = navigationIcon,
                        trailing = actions,
                        backgroundColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 6) overlay 层：sheet/flyout/dialog 放在这里，与 content 共享 resolvedHazeState
        overlay(shellState)
    }
}

/**
 * Clover 默认亚克力背景。
 *
 * 作为 [CloverAdaptiveShellScaffold] 的兜底背景，在内容滚动时透过条带看到模糊效果。
 */
@Composable
fun CloverAcrylicBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalCloverColorScheme.current.background)
    )
}
