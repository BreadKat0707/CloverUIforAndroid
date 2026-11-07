package cn.lemondrop.clover

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * WinUI 动画时长 token。
 */
private val FastAnimationDuration = 167
private val FasterAnimationDuration = 83

/**
 * Clover Design Switch（WinUI ToggleSwitch 风格）
 *
 * - 轨道 40×20dp，圆角 10dp，1dp 描边
 * - 滑块 12dp 圆形，按压时涨大到 14dp
 * - 颜色和滑块位置使用 167ms/83ms 动画过渡
 *
 * @param checked 当前是否打开
 * @param onCheckedChange 状态变化回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 */
@Composable
fun CloverSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 轨道填充色：On 主色，Off 透明
    val trackFillOn = LocalCloverColorScheme.current.primary
    val trackFillOff = Color.Transparent

    val trackFillTarget = when {
        !enabled -> if (checked) trackFillOn.copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
            else Color.Transparent
        checked -> trackFillOn
        else -> trackFillOff
    }
    val trackFill by animateColorAsState(
        targetValue = trackFillTarget,
        animationSpec = tween(FastAnimationDuration),
        label = "switchTrackFill"
    )

    // 轨道描边色
    val strokeOff = LocalCloverColorScheme.current.onSurfaceVariant.copy(alpha = 0.4f)
    val strokeOn = LocalCloverColorScheme.current.primary
    val strokeTarget = when {
        !enabled -> if (checked) strokeOn.copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
            else strokeOff.copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
        checked -> strokeOn
        else -> strokeOff
    }
    val stroke by animateColorAsState(
        targetValue = strokeTarget,
        animationSpec = tween(FastAnimationDuration),
        label = "switchStroke"
    )

    // 滑块色：始终白色
    val knobColor = Color.White
    val knobSize by animateDpAsState(
        targetValue = if (isPressed && enabled) 18.dp else 16.dp,
        animationSpec = tween(FasterAnimationDuration),
        label = "switchKnobSize"
    )

    // 轨道尺寸：44×24dp，适合触摸操作
    val trackWidth = 44.dp
    val trackHeight = 24.dp
    val knobPadding = 3.dp  // 轨道内缘到滑块边缘距离
    val knobOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - knobPadding * 2 - knobSize else knobPadding,
        animationSpec = tween(FasterAnimationDuration),
        label = "switchKnobOffset"
    )

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(trackFill)
            .border(1.5.dp, stroke, RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(knobSize)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(knobColor)
        )
    }
}

/**
 * 带文字的 Switch 行
 */
@Composable
fun CloverSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null
) {
    CloverBasicItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = {
            CloverSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        onClick = { if (enabled) onCheckedChange(!checked) }
    )
}
