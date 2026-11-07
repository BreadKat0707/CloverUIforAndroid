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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Clover Design 普通标题栏
 *
 * 用于顶部或底部的常规标题栏，显示普通大小的当前页面标题，不收折。
 * 左侧通常放返回/菜单按钮，右侧放页面级操作按钮。
 *
 * 背景区域会消费指针事件，避免点击穿透到标题栏后方的内容。
 *
 * @param title 标题内容
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，null 表示透明（通常由外层 Haze 面板提供背景）
 * @param contentColor 内容/图标颜色，默认跟随主题 onSurface，已适配深色模式
 * @param leading 左侧操作区
 * @param trailing 右侧操作区
 */
@Composable
fun CloverTitleBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    contentColor: Color = LocalCloverColorScheme.current.onSurface,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val consumeTouches = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
            }
        }
    }

    val baseModifier = modifier
        .fillMaxWidth()
        .height(CloverSizes.titleBarHeight)
        .then(consumeTouches)
        .padding(horizontal = CloverTitleBarDefaults.horizontalPadding)

    val containerModifier = if (backgroundColor != null) {
        baseModifier.background(backgroundColor)
    } else {
        baseModifier
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = containerModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(CloverTitleBarDefaults.leadingSpacing))
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                ProvideTextStyle(
                    value = CloverTypography.topAppBarCollapsed.copy(color = contentColor)
                ) {
                    title()
                }
            }
            trailing()
        }
    }
}

/**
 * Clover Design 普通标题栏默认参数
 */
object CloverTitleBarDefaults {
    val height = CloverSizes.titleBarHeight
    val horizontalPadding = 12.dp
    val leadingSpacing = 4.dp
}
