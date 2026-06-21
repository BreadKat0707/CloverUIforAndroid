package cn.lemondrop.clover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 列表空状态组件
 *
 * 用于列表没有数据时展示插图、标题、说明和可选操作。
 * 插图默认使用传入的 Lucide 图标（大尺寸），后续可替换为自定义矢量插画。
 *
 * @param title 主标题
 * @param message 说明文字
 * @param icon 插图图标，null 时不显示
 * @param actionLabel 操作按钮文字，null 时不显示按钮
 * @param onActionClick 操作按钮点击回调
 * @param modifier 外部 modifier
 */
@Composable
fun CloverEmptyList(
    title: String,
    message: String,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isCloverDark()
    val titleColor = if (isDark) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight
    val bodyColor = if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
    val iconColor = if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CloverSizes.listOuterHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(CloverSpacing.lg))
        }

        Text(
            text = title,
            style = CloverTypography.itemTitle,
            color = titleColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CloverSpacing.sm))

        Text(
            text = message,
            style = CloverTypography.itemSubtitle,
            color = bodyColor,
            textAlign = TextAlign.Center
        )

        if (!actionLabel.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(CloverSpacing.lg))
            Text(
                text = actionLabel,
                style = CloverTypography.button,
                color = CloverColors.accent,
                modifier = Modifier.cloverClickable(
                    onClick = onActionClick,
                    cornerRadius = 8.dp
                )
            )
        }
    }
}
