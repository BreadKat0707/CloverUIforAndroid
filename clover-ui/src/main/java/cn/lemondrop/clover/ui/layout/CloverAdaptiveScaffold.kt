package cn.lemondrop.clover.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.CloverBottomNavbar
import cn.lemondrop.clover.CloverNavItem
import cn.lemondrop.clover.CloverNavigationRail
import cn.lemondrop.clover.CloverSizes
import cn.lemondrop.clover.CloverTitleBar
import cn.lemondrop.clover.CloverTopAppBar
import cn.lemondrop.clover.LocalCloverColorScheme
import cn.lemondrop.clover.material.CloverMaterial
import cn.lemondrop.clover.material.CloverOutsideStroke
import cn.lemondrop.clover.material.CloverStrokeSide
import cn.lemondrop.clover.material.cloverAcrylic
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * 自适应布局策略
 */
enum class CloverAdaptiveStrategy {
    /** 经典布局：顶部 TopAppBar + 底部 BottomNavbar */
    Classic,
    /** 手机偏好布局：底部 TitleBar + 底部 BottomNavbar */
    BottomCombined,
    /** 大屏布局：左侧 NavigationRail + 顶部 TopAppBar */
    RailWithTopBar,
    /** 大屏布局：左侧 NavigationRail + 底部 TitleBar */
    RailWithBottomBar,
    /** 大屏布局：左侧 NavigationView + 顶部 TopAppBar */
    ViewWithTopBar,
    /** 大屏布局：左侧 NavigationView + 底部 TitleBar */
    ViewWithBottomBar
}

/**
 * 侧边导航形态
 */
enum class CloverNavigationStyle {
    Rail,
    View
}

/**
 * 栏位材质
 */
sealed class CloverBarMaterial {
    /** 无特殊材质，由调用者自行处理背景 */
    data object None : CloverBarMaterial()

    /** 亚克力材质 */
    data object Acrylic : CloverBarMaterial()

    /** 纯色材质 */
    data class Solid(val color: Color? = null) : CloverBarMaterial()
}

/**
 * Clover 自适应页面骨架
 *
 * 通过 [strategy] 决定标题栏、导航栏的摆放位置；
 * 通过 [barMaterial] 统一为栏位应用 Acrylic 等材质。
 *
 * @param strategy 布局策略
 * @param modifier 外部 modifier
 * @param topBar 顶部栏 slot
 * @param bottomBar 底部栏 slot
 * @param navigationBar 侧边导航栏 slot（Rail 或 View）
 * @param floatingActionButton 浮动按钮 slot
 * @param barMaterial 栏位材质
 * @param content 页面主体内容
 */
@Composable
fun CloverAdaptiveScaffold(
    strategy: CloverAdaptiveStrategy,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    navigationBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    barMaterial: CloverBarMaterial = CloverBarMaterial.Acrylic,
    content: @Composable (PaddingValues) -> Unit
) {
    val hazeState = remember { HazeState() }
    val outlineColor = LocalCloverColorScheme.current.outline.copy(alpha = 0.4f)
    val outlineWidth = 1.dp

    @Composable
    fun MaterializedTopBar(bar: @Composable () -> Unit) {
        when (barMaterial) {
            is CloverBarMaterial.None -> bar()
            is CloverBarMaterial.Solid -> {
                val bg = barMaterial.color ?: LocalCloverColorScheme.current.surface
                Box(modifier = Modifier.fillMaxWidth().background(bg)) { bar() }
            }
            is CloverBarMaterial.Acrylic -> {
                CloverOutsideStroke(
                    width = outlineWidth,
                    color = outlineColor,
                    side = CloverStrokeSide.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cloverAcrylic(state = hazeState)
                    ) { bar() }
                }
            }
        }
    }

    @Composable
    fun MaterializedBottomBar(bar: @Composable () -> Unit) {
        when (barMaterial) {
            is CloverBarMaterial.None -> bar()
            is CloverBarMaterial.Solid -> {
                val bg = barMaterial.color ?: LocalCloverColorScheme.current.surface
                Box(modifier = Modifier.fillMaxWidth().background(bg)) { bar() }
            }
            is CloverBarMaterial.Acrylic -> {
                CloverOutsideStroke(
                    width = outlineWidth,
                    color = outlineColor,
                    side = CloverStrokeSide.Top
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cloverAcrylic(state = hazeState)
                    ) { bar() }
                }
            }
        }
    }

    @Composable
    fun MaterializedNavigationBar(bar: @Composable () -> Unit) {
        when (barMaterial) {
            is CloverBarMaterial.None -> bar()
            is CloverBarMaterial.Solid -> {
                val bg = barMaterial.color ?: LocalCloverColorScheme.current.surface
                Box(modifier = Modifier.fillMaxHeight().background(bg)) { bar() }
            }
            is CloverBarMaterial.Acrylic -> {
                CloverOutsideStroke(
                    width = outlineWidth,
                    color = outlineColor,
                    side = CloverStrokeSide.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .cloverAcrylic(state = hazeState)
                    ) { bar() }
                }
            }
        }
    }

    val hasNav = strategy in listOf(
        CloverAdaptiveStrategy.RailWithTopBar,
        CloverAdaptiveStrategy.RailWithBottomBar,
        CloverAdaptiveStrategy.ViewWithTopBar,
        CloverAdaptiveStrategy.ViewWithBottomBar
    ) && navigationBar != null

    val hasTop = strategy in listOf(
        CloverAdaptiveStrategy.Classic,
        CloverAdaptiveStrategy.RailWithTopBar,
        CloverAdaptiveStrategy.ViewWithTopBar
    ) && topBar != null

    val hasBottom = strategy in listOf(
        CloverAdaptiveStrategy.Classic,
        CloverAdaptiveStrategy.BottomCombined,
        CloverAdaptiveStrategy.RailWithBottomBar,
        CloverAdaptiveStrategy.ViewWithBottomBar
    ) && bottomBar != null

    val navWidth = when (strategy) {
        CloverAdaptiveStrategy.RailWithTopBar,
        CloverAdaptiveStrategy.RailWithBottomBar -> if (hasNav) CloverSizes.navigationRailWidth else 0.dp

        CloverAdaptiveStrategy.ViewWithTopBar,
        CloverAdaptiveStrategy.ViewWithBottomBar -> if (hasNav) CloverSizes.navigationViewWidth else 0.dp

        else -> 0.dp
    }

    val topHeight = if (hasTop) CloverSizes.topAppBarHeight else 0.dp
    val bottomHeight = if (hasBottom) when (strategy) {
        CloverAdaptiveStrategy.BottomCombined -> CloverSizes.titleBarHeight + CloverSizes.bottomNavHeight
        else -> CloverSizes.bottomNavHeight
    } else 0.dp

    Box(modifier = modifier.fillMaxSize()) {
        // 内容层，作为亚克力材质的模糊源
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            content(
                PaddingValues(
                    start = if (barMaterial is CloverBarMaterial.Acrylic) navWidth + outlineWidth else navWidth,
                    top = topHeight,
                    bottom = bottomHeight
                )
            )
        }

        // 顶部栏
        if (hasTop) {
            Box(modifier = Modifier.fillMaxWidth()) {
                MaterializedTopBar { topBar!!.invoke() }
            }
        }

        // 侧边导航栏
        if (hasNav) {
            Box(modifier = Modifier.fillMaxHeight()) {
                MaterializedNavigationBar { navigationBar!!.invoke() }
            }
        }

        // 底部栏
        if (hasBottom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                MaterializedBottomBar { bottomBar!!.invoke() }
            }
        }

        // 浮动按钮
        floatingActionButton?.let { fab ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = CloverSizes.listOuterHorizontalPadding,
                        bottom = bottomHeight + CloverSizes.listOuterHorizontalPadding
                    )
            ) {
                fab()
            }
        }
    }
}

/**
 * 带导航功能的 Clover 自适应页面骨架
 *
 * 内部会自动组合 [CloverTopAppBar] / [CloverTitleBar] / [CloverBottomNavbar] /
 * [CloverNavigationRail] / [CloverNavigationView]。
 */
@Composable
fun CloverAdaptiveNavigableScaffold(
    title: @Composable () -> Unit,
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    strategy: CloverAdaptiveStrategy = CloverAdaptiveStrategy.BottomCombined,
    navigationStyle: CloverNavigationStyle = CloverNavigationStyle.Rail,
    topBarActions: @Composable RowScope.() -> Unit = {},
    bottomBarLeading: @Composable (() -> Unit)? = null,
    bottomBarTrailing: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    barMaterial: CloverBarMaterial = CloverBarMaterial.Acrylic,
    content: @Composable (PaddingValues) -> Unit
) {
    val topBar: @Composable (() -> Unit)? = if (strategy in listOf(
            CloverAdaptiveStrategy.Classic,
            CloverAdaptiveStrategy.RailWithTopBar,
            CloverAdaptiveStrategy.ViewWithTopBar
        )) {
        {
            CloverTopAppBar(
                title = title,
                actions = topBarActions
            )
        }
    } else null

    val bottomBar: @Composable (() -> Unit)? = if (strategy in listOf(
            CloverAdaptiveStrategy.Classic,
            CloverAdaptiveStrategy.BottomCombined,
            CloverAdaptiveStrategy.RailWithBottomBar,
            CloverAdaptiveStrategy.ViewWithBottomBar
        )) {
        if (strategy == CloverAdaptiveStrategy.BottomCombined) {
            {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CloverTitleBar(
                        title = title,
                        leading = bottomBarLeading,
                        trailing = bottomBarTrailing
                    )
                    CloverBottomNavbar(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected
                    )
                }
            }
        } else {
            {
                CloverBottomNavbar(
                    items = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = onItemSelected
                )
            }
        }
    } else null

    val navigationBar: @Composable (() -> Unit)? = if (strategy in listOf(
            CloverAdaptiveStrategy.RailWithTopBar,
            CloverAdaptiveStrategy.RailWithBottomBar,
            CloverAdaptiveStrategy.ViewWithTopBar,
            CloverAdaptiveStrategy.ViewWithBottomBar
        )) {
        when (navigationStyle) {
            CloverNavigationStyle.Rail -> {
                {
                    CloverNavigationRail(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected
                    )
                }
            }
            CloverNavigationStyle.View -> {
                {
                    CloverNavigationView(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected
                    )
                }
            }
        }
    } else null

    CloverAdaptiveScaffold(
        strategy = strategy,
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        navigationBar = navigationBar,
        floatingActionButton = floatingActionButton,
        barMaterial = barMaterial,
        content = content
    )
}
