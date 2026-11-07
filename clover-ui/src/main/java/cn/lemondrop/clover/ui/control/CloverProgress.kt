package cn.lemondrop.clover

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** WinUI ProgressBar 缓动曲线：[0.4, 0.0, 0.6, 1.0] */
private val ProgressBarEasing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)

/**
 * Clover Design 线性进度条（WinUI ProgressBar 风格）
 *
 * - 3dp 高、2dp 圆角
 * - 确定态：从左到右填充
 * - 不确定态：双滑块交错滑动（对齐 WinUI 的两段动画，2s 周期）
 *
 * @param progress 当前进度 0..1，传 null 表示不确定进度
 */
@Composable
fun CloverLinearProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = LocalCloverColorScheme.current.primary,
    trackColor: Color = LocalCloverColorScheme.current.surfaceVariant
) {
    val cornerRadius = 2.dp
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
    ) {
        val containerWidthPx = with(density) { maxWidth.roundToPx() }

        if (progress != null) {
            val fraction = progress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(3.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(color)
            )
        } else {
            // WinUI 不确定态：两个矩形分段交错滑动
            // 帧1: 0s→1.5s 从左滑到右，1.5s→2s 停在右侧
            // 帧2: 0s→0.75s 停在左侧，0.75s→2s 从左滑到右
            val barWidthFrac = 0.4f
            val barWidthPx = (containerWidthPx * barWidthFrac).toInt()
            val travelPx = (containerWidthPx + barWidthPx).toFloat()

            val infiniteTransition = rememberInfiniteTransition(label = "progressBar")

            val bar1Frac by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                        0f at 0
                        1f at 1500 using ProgressBarEasing
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "bar1"
            )
            val bar2Frac by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                        0f at 0
                        0f at 750
                        1f at 2000 using ProgressBarEasing
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "bar2"
            )

            // 滑块 1（领头）
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidthFrac)
                    .height(3.dp)
                    .offset { IntOffset((-barWidthPx + travelPx * bar1Frac).roundToInt(), 0) }
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(color.copy(alpha = 0.6f))
            )
            // 滑块 2（跟随）
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidthFrac)
                    .height(3.dp)
                    .offset { IntOffset((-barWidthPx + travelPx * bar2Frac).roundToInt(), 0) }
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(color)
            )
        }
    }
}

/**
 * Clover Design 圆形进度环（WinUI ProgressRing 风格）
 *
 * - 32dp 默认、4dp 线宽、圆形端点
 * - 确定态：从顶部顺时针填充弧线
 * - 不确定态：约 300° 弧线持续线性旋转（WinUI 的 chasing arc，非 M3 呼吸环）
 */
@Composable
fun CloverCircularProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = LocalCloverColorScheme.current.primary,
    trackColor: Color = LocalCloverColorScheme.current.surfaceVariant,
    size: Dp = 32.dp,
    strokeWidth: Dp = 4.dp
) {
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    val canvasPx = with(density) { size.toPx() }
    val halfStroke = strokePx / 2f

    if (progress != null) {
        // 确定态
        val sweep = progress.coerceIn(0f, 1f) * 360f
        Canvas(modifier = modifier.size(size)) {
            val topLeft = Offset(halfStroke, halfStroke)
            val arcSize = Size(canvasPx - strokePx, canvasPx - strokePx)
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
            drawArc(trackColor, -90f, 360f, false, topLeft = topLeft, size = arcSize, style = stroke)
            drawArc(color, -90f, sweep, false, topLeft = topLeft, size = arcSize, style = stroke)
        }
    } else {
        // 不确定态：~300° 弧线持续线性旋转（WinUI chasing arc）
        val infiniteTransition = rememberInfiniteTransition(label = "progressRing")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ringRotation"
        )
        val arcSweep = 300f

        Canvas(modifier = modifier.size(size)) {
            val topLeft = Offset(halfStroke, halfStroke)
            val arcSize = Size(canvasPx - strokePx, canvasPx - strokePx)
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
            drawArc(trackColor, 0f, 360f, false, topLeft = topLeft, size = arcSize, style = stroke)
            drawArc(color, rotation - 90f, arcSweep, false, topLeft = topLeft, size = arcSize, style = stroke)
        }
    }
}
