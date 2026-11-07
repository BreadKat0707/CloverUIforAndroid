package cn.lemondrop.clover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.ui.layout.CloverAdaptiveShellScaffold

/**
 * Clover Design 响应式页面脚手架
 *
 * 用于没有底部导航栏的二级页面（设置、统计、关于等）。
 *
 * @deprecated 已弃用。请迁移到 [CloverAdaptiveShellScaffold]，它统一处理了
 * 标题栏、导航栏、overlay 共享 HazeState 和刘海适配。
 */
@Deprecated(
    message = "CloverAdaptivePageScaffold 已被 CloverAdaptiveShellScaffold 取代",
    replaceWith = ReplaceWith("CloverAdaptiveShellScaffold")
)
@Composable
fun CloverAdaptivePageScaffold(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isCompact = cloverIsCompactWidth()
    val statusBarPadding = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()

    CloverAdaptiveShellScaffold(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        content = { state ->
            // 为旧 API 提供兼容内边距
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (isCompact) 8.dp else 0.dp,
                        bottom = if (isCompact) {
                            CloverSizes.titleBarHeight + navBarPadding + 16.dp
                        } else {
                            navBarPadding + 16.dp
                        }
                    )
            ) {
                content(
                    PaddingValues(
                        top = statusBarPadding,
                        bottom = navBarPadding
                    )
                )
            }
        }
    )
}
