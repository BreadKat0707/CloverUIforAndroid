package cn.lemondrop.clover

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Clover Design Switch
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
    val trackColor = when {
        !enabled -> if (checked) CloverColors.accent.copy(alpha = 0.4f) else CloverColors.surfaceVariantDark.copy(alpha = 0.3f)
        checked -> CloverColors.accent
        else -> if (isCloverDark()) CloverColors.surfaceVariantDark else CloverColors.surfaceVariantLight
    }

    val thumbColor = if (isCloverDark()) Color.White else Color.White

    val interactionSource = remember { MutableInteractionSource() }

    val thumbOffset = animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = tween(200),
        label = "switchThumb"
    ).value

    Box(
        modifier = modifier
            .width(52.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .offset(x = thumbOffset)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
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
