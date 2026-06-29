package cn.lemondrop.clover

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide

/**
 * Clover Design Checkbox
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

    val backgroundColor = when {
        !enabled && checked -> LocalCloverColorScheme.current.primary.copy(alpha = 0.4f)
        !enabled -> Color.Transparent
        checked -> LocalCloverColorScheme.current.primary
        else -> Color.Transparent
    }

    val borderColor = when {
        !enabled -> LocalCloverColorScheme.current.onSurfaceVariant.copy(alpha = 0.3f)
        checked -> LocalCloverColorScheme.current.primary
        else -> LocalCloverColorScheme.current.onSurfaceVariant
    }

    val iconAlpha = animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(150),
        label = "checkboxCheck"
    ).value

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (iconAlpha > 0.01f) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
