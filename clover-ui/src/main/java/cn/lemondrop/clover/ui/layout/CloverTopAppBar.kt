package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Search
import io.github.composefluent.component.Text

/**
 * Clover Design 顶部标题栏（MD3 Small TopAppBar 风格）
 *
 * 用于大屏幕设备，替代底部的 `CloverTitleBar`。
 *
 * 布局参考 Material Design 3 的 Small Top App Bar：
 * - 容器高度 64dp
 * - 左侧可选导航图标
 * - 标题左对齐，默认使用 `titleLarge` 字体
 * - 右侧操作区
 *
 * @param title 标题内容
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，默认使用当前 surface
 * @param contentColor 内容/图标颜色，默认使用当前 onSurface
 * @param navigationIcon 左侧导航图标/返回按钮
 * @param actions 右侧操作区
 */
@Composable
fun CloverTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalCloverColorScheme.current.surface,
    contentColor: Color = LocalCloverColorScheme.current.onSurface,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .height(CloverSizes.topAppBarHeight)
        .background(backgroundColor)
        .padding(horizontal = CloverSizes.listItemHorizontalPadding)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = baseModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(CloverTopAppBarDefaults.navigationIconSpacing))
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                    title()
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/**
 * Clover Design 顶部标题栏默认参数
 */
object CloverTopAppBarDefaults {
    val height = CloverSizes.topAppBarHeight
    val horizontalPadding = CloverSizes.listItemHorizontalPadding
    val navigationIconSpacing = 12.dp
}

@Preview
@Composable
private fun CloverTopAppBarPreview() {
    CloverTopAppBar(
        title = {
            Text(
                text = "媒体库",
                style = CloverTypography.titleBar,
                color = LocalCloverColorScheme.current.onSurface
            )
        }
    )
}
