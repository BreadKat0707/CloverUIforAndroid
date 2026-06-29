package cn.lemondrop.clover

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Clover Design RadioButton
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

    val borderColor = when {
        !enabled -> LocalCloverColorScheme.current.onSurfaceVariant.copy(alpha = 0.3f)
        selected -> LocalCloverColorScheme.current.primary
        else -> LocalCloverColorScheme.current.onSurfaceVariant
    }

    val dotSize = animateDpAsState(
        targetValue = if (selected) 10.dp else 0.dp,
        animationSpec = tween(150),
        label = "radioDot"
    ).value

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
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
                    .background(
                        if (enabled) LocalCloverColorScheme.current.primary else LocalCloverColorScheme.current.primary.copy(alpha = 0.4f)
                    )
            )
        }
    }
}
