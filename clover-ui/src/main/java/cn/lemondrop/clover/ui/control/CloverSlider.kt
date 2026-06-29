package cn.lemondrop.clover

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Clover Design Slider
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 * @param valueRange 取值范围
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloverSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val interactionSource = remember { MutableInteractionSource() }

    val accent = LocalCloverColorScheme.current.primary
    val trackInactive = LocalCloverColorScheme.current.surfaceVariant

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        interactionSource = interactionSource,
        colors = SliderDefaults.colors(
            thumbColor = accent,
            activeTrackColor = accent,
            inactiveTrackColor = trackInactive,
            disabledThumbColor = accent.copy(alpha = 0.4f),
            disabledActiveTrackColor = accent.copy(alpha = 0.4f),
            disabledInactiveTrackColor = trackInactive.copy(alpha = 0.4f)
        )
    )
}
