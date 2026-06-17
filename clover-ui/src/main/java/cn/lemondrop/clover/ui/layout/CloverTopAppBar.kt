package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Search
import io.github.composefluent.component.Text

/**
 * Clover Design 顶部标题栏
 *
 * 用于大屏幕设备，替代底部的 `CloverTitleBar`。
 *
 * @param title 标题内容
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，默认透明
 * @param navigationIcon 左侧导航图标/返回按钮
 * @param actions 右侧操作区
 */
@Composable
fun CloverTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .height(CloverSizes.topAppBarHeight)
        .padding(horizontal = CloverSizes.listItemHorizontalPadding)

    val containerModifier = if (backgroundColor != null) {
        baseModifier.background(backgroundColor)
    } else {
        baseModifier
    }

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon != null) {
            navigationIcon()
            Box(modifier = Modifier.padding(start = 12.dp))
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            title()
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
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
                color = if (isCloverDark()) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight
            )
        }
    )
}
