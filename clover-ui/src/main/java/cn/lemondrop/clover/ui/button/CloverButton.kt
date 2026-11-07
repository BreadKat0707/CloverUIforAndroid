package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 按钮尺寸
 */
enum class CloverButtonSize {
    Small, Medium, Large
}

/**
 * Clover Design 按钮颜色配置
 */
data class CloverButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val borderColor: Color = Color.Transparent,
    val disabledBorderColor: Color = Color.Transparent
)

/**
 * 根据背景色自动选择黑/白内容色，保证可读性。
 */
internal fun Color.contentColorFor(): Color {
    return if (this.luminance() > 0.5f) Color.Black else Color.White
}

/**
 * 返回深色/浅色模式下统一的内容色：深色固定白色，浅色根据容器色自动选择。
 */
@Composable
internal fun containerContentColor(containerColor: Color): Color {
    return if (isCloverDark()) Color.White else containerColor.contentColorFor()
}

/**
 * 返回强调内容色：统一使用主题主色，在深/浅色模式下均有足够辨识度。
 */
@Composable
internal fun accentContentColor(): Color {
    return LocalCloverColorScheme.current.primary
}

/**
 * Clover Design 按钮默认参数
 */
object CloverButtonDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)

    val smallHeight: Dp = 32.dp
    val mediumHeight: Dp = 40.dp
    val largeHeight: Dp = 48.dp

    val smallHorizontalPadding: Dp = 12.dp
    val mediumHorizontalPadding: Dp = 16.dp
    val largeHorizontalPadding: Dp = 20.dp

    val iconSize: Dp = 18.dp
    val iconSpacing: Dp = 8.dp

    /**
     * 实心按钮颜色（主强调色背景 + 自动黑/白文字）
     */
    @Composable
    fun filledColors(
        containerColor: Color = LocalCloverColorScheme.current.primary,
        contentColor: Color = containerContentColor(containerColor),
        disabledContainerColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.38f)
    ): CloverButtonColors = CloverButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    /**
     * 描边按钮颜色
     */
    @Composable
    fun outlinedColors(
        contentColor: Color = accentContentColor(),
        borderColor: Color = LocalCloverColorScheme.current.primary.copy(alpha = 0.55f),
        disabledContentColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.38f),
        disabledBorderColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.12f)
    ): CloverButtonColors = CloverButtonColors(
        containerColor = Color.Transparent,
        contentColor = contentColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = disabledContentColor,
        borderColor = borderColor,
        disabledBorderColor = disabledBorderColor
    )

    /**
     * 文字按钮颜色
     */
    @Composable
    fun textColors(
        contentColor: Color = accentContentColor(),
        disabledContentColor: Color = LocalCloverColorScheme.current.onSurface.copy(alpha = 0.38f)
    ): CloverButtonColors = CloverButtonColors(
        containerColor = Color.Transparent,
        contentColor = contentColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = disabledContentColor
    )
}

/**
 * 获取按钮尺寸对应的参数
 */
private fun sizeParams(size: CloverButtonSize): Triple<Dp, Dp, TextStyle> = when (size) {
    CloverButtonSize.Small -> Triple(
        CloverButtonDefaults.smallHeight,
        CloverButtonDefaults.smallHorizontalPadding,
        CloverTypography.itemSubtitle
    )

    CloverButtonSize.Medium -> Triple(
        CloverButtonDefaults.mediumHeight,
        CloverButtonDefaults.mediumHorizontalPadding,
        CloverTypography.itemTitle
    )

    CloverButtonSize.Large -> Triple(
        CloverButtonDefaults.largeHeight,
        CloverButtonDefaults.largeHorizontalPadding,
        CloverTypography.titleBar
    )
}

/**
 * Clover Design 通用按钮。
 *
 * 通过 [colors] 可组合出填充、描边、文字等样式。
 *
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 * @param size 尺寸
 * @param colors 颜色配置
 * @param shape 形状
 * @param reveal 是否启用 Reveal 按压光效
 * @param contentPadding 自定义内容内边距，null 时按尺寸默认
 * @param content 按钮内容
 */
@Composable
fun CloverButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CloverButtonSize = CloverButtonSize.Medium,
    colors: CloverButtonColors = CloverButtonDefaults.filledColors(),
    shape: Shape = CloverButtonDefaults.shape,
    reveal: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable RowScope.() -> Unit
) {
    val (height, horizontalPadding, textStyle) = sizeParams(size)
    val padding = contentPadding ?: PaddingValues(horizontal = horizontalPadding)

    val interactionSource = remember { MutableInteractionSource() }

    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    val borderColor = if (enabled) colors.borderColor else colors.disabledBorderColor

    val baseModifier = modifier
        .clip(shape)
        .background(containerColor, shape)
        .border(
            width = if (borderColor == Color.Transparent) 0.dp else 1.dp,
            color = borderColor,
            shape = shape
        )
        .let {
            if (reveal && enabled) {
                it.cloverReveal(shape = shape, enabled = true)
            } else {
                it
            }
        }
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = if (reveal && enabled) null else ripple(),
            onClick = onClick
        )
        .defaultMinSize(minHeight = height)
        .padding(padding)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = CloverButtonDefaults.iconSpacing,
            alignment = Alignment.CenterHorizontally
        ),
        content = content
    )
}

/**
 * 实心文字按钮
 *
 * @param text 按钮文字
 */
@Composable
fun CloverButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CloverButtonSize = CloverButtonSize.Medium,
    colors: CloverButtonColors = CloverButtonDefaults.filledColors(),
    shape: Shape = CloverButtonDefaults.shape,
    reveal: Boolean = true
) {
    val (_, _, textStyle) = sizeParams(size)
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    CloverButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        colors = colors,
        shape = shape,
        reveal = reveal
    ) {
        Text(text = text, style = textStyle, color = contentColor)
    }
}

/**
 * 描边按钮
 */
@Composable
fun CloverOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CloverButtonSize = CloverButtonSize.Medium,
    shape: Shape = CloverButtonDefaults.shape,
    reveal: Boolean = true
) {
    val colors = CloverButtonDefaults.outlinedColors()
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    CloverButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        colors = colors,
        shape = shape,
        reveal = reveal
    ) {
        val (_, _, textStyle) = sizeParams(size)
        Text(text = text, style = textStyle, color = contentColor)
    }
}

/**
 * 文字按钮
 */
@Composable
fun CloverTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CloverButtonSize = CloverButtonSize.Medium,
    shape: Shape = CloverButtonDefaults.shape,
    reveal: Boolean = true
) {
    val colors = CloverButtonDefaults.textColors()
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    CloverButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        colors = colors,
        shape = shape,
        reveal = reveal
    ) {
        val (_, _, textStyle) = sizeParams(size)
        Text(text = text, style = textStyle, color = contentColor)
    }
}
