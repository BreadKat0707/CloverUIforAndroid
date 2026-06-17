package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Clover Design 底部标题栏
 *
 * 位于屏幕最底部（单手可达区域），左侧通常放菜单/返回按钮，
 * 中间显示当前页面标题，右侧放页面级操作按钮。
 *
 * @param title 标题内容
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，null 表示透明（通常由外层 Haze 面板提供背景）
 * @param leading 左侧操作区
 * @param trailing 右侧操作区
 */
@Composable
fun CloverTitleBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .height(CloverSizes.titleBarHeight)
        .padding(horizontal = 12.dp)

    val containerModifier = if (backgroundColor != null) {
        baseModifier.background(backgroundColor)
    } else {
        baseModifier
    }

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(4.dp))
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            title()
        }
        trailing()
    }
}

/**
 * Clover Design 底部标题栏默认参数
 */
object CloverTitleBarDefaults {
    val height = CloverSizes.titleBarHeight
    val horizontalPadding = 12.dp
    val leadingSpacing = 4.dp
}
