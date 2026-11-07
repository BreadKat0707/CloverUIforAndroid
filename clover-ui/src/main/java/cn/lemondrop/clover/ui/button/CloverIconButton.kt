package cn.lemondrop.clover

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Clover Design 图标按钮默认参数。
 *
 * 图标按钮与普通按钮视觉语义不同：默认使用透明容器，仅在按压时显示 Reveal 光效，
 * 适合用于标题栏、工具栏等需要小尺寸操作按钮的场景。
 */
object CloverIconButtonDefaults {
    /** 默认圆角。 */
    val shape: Shape = RoundedCornerShape(8.dp)

    /** 默认图标大小。 */
    val iconSize = 24.dp

    /** 默认最小触摸区域。 */
    val touchTargetSize = 48.dp

    /**
     * 图标按钮默认颜色：透明背景 + 深色模式白色 / 浅色模式 onSurface。
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = if (isCloverDark()) Color.White else LocalCloverColorScheme.current.onSurface,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.38f)
    ): CloverButtonColors = CloverButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}

/**
 * Clover Design 图标按钮。
 *
 * 与普通按钮分离，默认不带填充背景，适合标题栏、工具栏等场景。
 * 默认最小触摸区域为 48.dp，符合无障碍规范。
 *
 * @param icon 图标
 * @param contentDescription 内容描述
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 * @param colors 颜色配置
 * @param shape 形状
 * @param reveal 是否启用 Reveal 按压光效
 */
@Composable
fun CloverIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CloverButtonColors = CloverIconButtonDefaults.colors(),
    shape: Shape = CloverIconButtonDefaults.shape,
    reveal: Boolean = true
) {
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    CloverButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(
            minWidth = CloverIconButtonDefaults.touchTargetSize,
            minHeight = CloverIconButtonDefaults.touchTargetSize
        ),
        enabled = enabled,
        size = CloverButtonSize.Medium,
        colors = colors,
        shape = shape,
        reveal = reveal,
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(CloverIconButtonDefaults.iconSize),
            tint = contentColor
        )
    }
}
