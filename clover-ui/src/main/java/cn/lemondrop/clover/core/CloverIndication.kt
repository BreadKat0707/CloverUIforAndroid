package cn.lemondrop.clover

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Clover Design 按下/悬停/焦点指示效果
 *
 * 不使用 Material 默认水波纹，而是用一个半透明的圆角矩形覆盖层，
 * 保持无边界设计风格，同时给用户提供清晰的按压反馈。
 */
class CloverIndication(
    private val cornerRadius: Dp,
    private val color: Color
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        CloverIndicationNode(interactionSource, cornerRadius, color)

    override fun hashCode(): Int = 31 * cornerRadius.hashCode() + color.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CloverIndication) return false
        return cornerRadius == other.cornerRadius && color == other.color
    }
}

private class CloverIndicationNode(
    private val interactionSource: InteractionSource,
    private val cornerRadius: Dp,
    private val color: Color
) : Modifier.Node(), DrawModifierNode {

    private var isPressed by mutableStateOf(false)
    private var isHovered by mutableStateOf(false)
    private var isFocused by mutableStateOf(false)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release,
                    is PressInteraction.Cancel -> isPressed = false
                    is HoverInteraction.Enter -> isHovered = true
                    is HoverInteraction.Exit -> isHovered = false
                    is FocusInteraction.Focus -> isFocused = true
                    is FocusInteraction.Unfocus -> isFocused = false
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isPressed || isHovered || isFocused) {
            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                size = size
            )
        }
    }
}

/**
 * Clover 风格的点击修饰符
 *
 * 支持单击、长按，以及圆角高亮按下效果。
 */
@Composable
fun Modifier.cloverClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    cornerRadius: Dp = CloverSizes.listItemCornerRadius,
    color: Color? = null
): Modifier {
    val isDark = isCloverDark()
    val overlayColor = color ?: (LocalCloverColorScheme.current.onSurface)
        .copy(alpha = CloverPressOverlayAlpha)
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .combinedClickable(
            interactionSource = interactionSource,
            indication = CloverIndication(cornerRadius, overlayColor),
            onClick = onClick,
            onLongClick = onLongClick
        )
}

private val CloverPressOverlayAlpha = 0.08f
