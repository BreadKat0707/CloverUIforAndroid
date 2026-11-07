package cn.lemondrop.clover.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.lemondrop.clover.CloverNavItem

/**
 * 自适应布局策略
 *
 * @deprecated 已弃用。请使用 [CloverAdaptiveShellScaffold]，它提供更统一的
 * 外壳材质、刘海适配和 overlay 共享 HazeState 能力。
 */
@Deprecated(
    message = "CloverAdaptiveStrategy 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
enum class CloverAdaptiveStrategy {
    Classic,
    BottomCombined,
    RailWithTopBar,
    RailWithBottomBar,
    ViewWithTopBar,
    ViewWithBottomBar
}

/**
 * 侧边导航形态
 *
 * @deprecated 已弃用。请使用 [CloverAdaptiveShellScaffold]，默认使用 NavigationRail。
 */
@Deprecated(
    message = "CloverNavigationStyle 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
enum class CloverNavigationStyle {
    Rail,
    View
}

/**
 * 栏位材质
 *
 * @deprecated 已弃用。CloverAdaptiveShellScaffold 统一使用 Acrylic。
 */
@Deprecated(
    message = "CloverBarMaterial 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
sealed class CloverBarMaterial {
    data object None : CloverBarMaterial()
    data object Acrylic : CloverBarMaterial()
    data class Solid(val color: androidx.compose.ui.graphics.Color? = null) : CloverBarMaterial()
}

/**
 * Clover 自适应页面骨架
 *
 * @deprecated 已弃用。请迁移到 [CloverAdaptiveShellScaffold]。
 * 为保持二进制兼容，当前实现内部已委托给 [CloverAdaptiveShellScaffold]。
 */
@Deprecated(
    message = "CloverAdaptiveScaffold 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
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
    // 简化委托：把原来的 slot 放到一个统一的 Box 里，但不再保证材质与内边距完全对齐旧行为。
    // 仅用于避免旧调用方编译失败。
    @Suppress("DEPRECATION")
    CloverAdaptiveShellScaffold(
        modifier = modifier,
        overlay = {
            floatingActionButton?.invoke()
        },
        content = { state ->
            BoxWithPaddingCompat(
                topBar = topBar,
                bottomBar = bottomBar,
                navigationBar = navigationBar,
                content = content
            )
        }
    )
}

/**
 * 带导航功能的 Clover 自适应页面骨架
 *
 * @deprecated 已弃用。请迁移到 [CloverAdaptiveShellScaffold]。
 */
@Deprecated(
    message = "CloverAdaptiveNavigableScaffold 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
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
    @Suppress("DEPRECATION")
    CloverAdaptiveShellScaffold(
        modifier = modifier,
        title = title,
        navigationIcon = bottomBarLeading,
        actions = {
            topBarActions()
            bottomBarTrailing()
        },
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        overlay = {
            floatingActionButton?.invoke()
        },
        content = { state ->
            content(state.contentPadding)
        }
    )
}

@Composable
private fun BoxWithPaddingCompat(
    topBar: @Composable (() -> Unit)?,
    bottomBar: @Composable (() -> Unit)?,
    navigationBar: @Composable (() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit
) {
    // 为旧 API 提供最小兼容：让 content 占满可用空间，外部条带由调用者自己传入。
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        content(PaddingValues())
        topBar?.invoke()
        bottomBar?.invoke()
        navigationBar?.invoke()
    }
}
