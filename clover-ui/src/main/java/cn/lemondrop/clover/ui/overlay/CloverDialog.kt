package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 通用 Dialog
 *
 * 居中显示的圆角弹窗，包含标题、自定义内容和底部操作按钮。
 *
 * @param onDismissRequest 点击遮罩或返回键关闭回调
 * @param modifier 外部 modifier
 * @param title 标题文字
 * @param buttons 底部按钮区
 * @param content 自定义内容
 */
@Composable
fun CloverDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    buttons: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isCloverDark()
    val bgColor = LocalCloverColorScheme.current.surface

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) LocalCloverColorScheme.current.scrim.copy(alpha = 0.5f)
                    else LocalCloverColorScheme.current.scrim.copy(alpha = 0.4f)
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismissRequest
                )
        )

        // 弹窗面板
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(CloverSizes.dialogCornerRadius))
                .background(bgColor)
                .padding(CloverSizes.sheetHorizontalPadding, CloverSizes.sheetVerticalPadding)
                .clickable(enabled = false) { /* 阻止点击穿透 */ }
        ) {
            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    style = CloverTypography.titleBar,
                    color = LocalCloverColorScheme.current.onSurface,
                    modifier = Modifier.padding(bottom = CloverSpacing.md)
                )
            }

            CompositionLocalProvider(
                LocalContentColor provides LocalCloverColorScheme.current.onSurface
            ) {
                content()
            }

            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CloverSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttons()
            }
        }
    }
}

/**
 * Clover Design 通用 Dialog 默认参数
 */
object CloverDialogDefaults {
    val maxWidth = 400.dp
    val horizontalPadding = 24.dp
    val cornerRadius = CloverSizes.dialogCornerRadius
    val contentPadding = PaddingValues(
        horizontal = CloverSizes.sheetHorizontalPadding,
        vertical = CloverSizes.sheetVerticalPadding
    )
}
