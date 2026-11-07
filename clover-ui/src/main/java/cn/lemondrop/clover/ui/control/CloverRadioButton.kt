package cn.lemondrop.clover

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RadioAnimationDuration = 167

/**
 * Clover Design RadioButton（WinUI 风格）
 *
 * - 20dp 外圈，1.5dp 描边
 * - 选中时内部 10dp 实心圆点从小到大弹出
 * - 167ms 颜色/圆点动画
 *
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 */
@Composable
fun CloverRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    // 描边色
    val borderTarget = when {
        !enabled -> LocalCloverColorScheme.current.onSurfaceVariant
            .copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
        selected -> LocalCloverColorScheme.current.primary
        else -> LocalCloverColorScheme.current.onSurfaceVariant
    }
    val border by animateColorAsState(
        targetValue = borderTarget,
        animationSpec = tween(RadioAnimationDuration),
        label = "radioBorder"
    )

    // 内部圆点大小
    val dotSize by animateDpAsState(
        targetValue = if (selected) 12.dp else 0.dp,
        animationSpec = tween(RadioAnimationDuration),
        label = "radioDot"
    )

    // 圆点色
    val dotColor = when {
        !enabled -> LocalCloverColorScheme.current.primary
            .copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
        else -> LocalCloverColorScheme.current.primary
    }

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(1.5.dp, border, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (dotSize > 0.dp) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
