package cn.lemondrop.clover

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Clover Design 线性进度条
 *
 * @param progress 当前进度 0..1，传 null 表示不确定进度
 * @param modifier 外部 modifier
 * @param color 进度颜色
 * @param trackColor 轨道颜色
 */
@Composable
fun CloverLinearProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = CloverColors.accent,
    trackColor: Color = if (isCloverDark()) CloverColors.surfaceVariantDark else CloverColors.surfaceVariantLight
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier.fillMaxWidth(),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier.fillMaxWidth(),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Clover Design 圆形进度条
 *
 * @param progress 当前进度 0..1，传 null 表示不确定进度
 * @param modifier 外部 modifier
 * @param color 进度颜色
 * @param trackColor 轨道颜色
 * @param size 直径
 * @param strokeWidth 线宽
 */
@Composable
fun CloverCircularProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = CloverColors.accent,
    trackColor: Color = if (isCloverDark()) CloverColors.surfaceVariantDark else CloverColors.surfaceVariantLight,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    val m = modifier.size(size)
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = m,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
    } else {
        CircularProgressIndicator(
            modifier = m,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
    }
}
