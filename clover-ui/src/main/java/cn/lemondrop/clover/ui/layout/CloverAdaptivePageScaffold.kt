package cn.lemondrop.clover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Clover Design 响应式页面脚手架
 *
 * 用于没有底部导航栏的二级页面（设置、统计、关于等）：
 * - Compact：顶部留状态栏，底部显示 `CloverTitleBar`
 * - Medium / Expanded：顶部显示 `CloverTopAppBar`，底部无标题栏
 *
 * @param title 页面标题
 * @param modifier 外部 modifier
 * @param navigationIcon 左侧导航/返回图标
 * @param actions 右侧操作按钮
 * @param content 页面内容，接收合适的 PaddingValues
 */
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

    Box(modifier = modifier.fillMaxSize()) {
        if (isCompact) {
            content(
                PaddingValues(
                    top = statusBarPadding + 8.dp,
                    bottom = CloverSizes.titleBarHeight + navBarPadding + 16.dp
                )
            )

            CloverTitleBar(
                title = title,
                leading = navigationIcon,
                trailing = actions,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarPadding),
                backgroundColor = null
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                CloverTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    modifier = Modifier.statusBarsPadding()
                )

                Box(modifier = Modifier.weight(1f)) {
                    content(
                        PaddingValues(
                            top = 8.dp,
                            bottom = navBarPadding + 16.dp
                        )
                    )
                }
            }
        }
    }
}
