package cn.lemondrop.clover

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide

private val CheckboxAnimationDuration = 167
private val CheckboxAnimationDurationFast = 83

/**
 * Clover Design Checkbox（WinUI 风格）
 *
 * - 20dp 方框，4dp 圆角，1.5dp 描边
 * - 勾选时方框填充主色，勾号从无到有缩放淡入
 * - 167ms 颜色/描边过渡 + 勾号动画
 *
 * @param checked 是否选中
 * @param onCheckedChange 状态变化回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 */
@Composable
fun CloverCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    // 填充色
    val fillTarget = when {
        !enabled && checked -> LocalCloverColorScheme.current.primary
            .copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
        checked -> LocalCloverColorScheme.current.primary
        else -> Color.Transparent
    }
    val fill by animateColorAsState(
        targetValue = fillTarget,
        animationSpec = tween(CheckboxAnimationDuration),
        label = "checkboxFill"
    )

    // 描边色
    val borderTarget = when {
        !enabled -> LocalCloverColorScheme.current.onSurfaceVariant
            .copy(alpha = CloverBasicItemDefaults.DisabledAlpha)
        checked -> LocalCloverColorScheme.current.primary
        else -> LocalCloverColorScheme.current.onSurfaceVariant
    }
    val border by animateColorAsState(
        targetValue = borderTarget,
        animationSpec = tween(CheckboxAnimationDuration),
        label = "checkboxBorder"
    )

    // 勾号透明度
    val iconAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(CheckboxAnimationDurationFast),
        label = "checkboxIconAlpha"
    )

    // 勾号大小（从小到大弹出）
    val iconSize by animateDpAsState(
        targetValue = if (checked) 14.dp else 0.dp,
        animationSpec = tween(CheckboxAnimationDuration),
        label = "checkboxIconSize"
    )

    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(4.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Lucide.Check,
            contentDescription = if (checked) "Checked" else "Unchecked",
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}
