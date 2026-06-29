package cn.lemondrop.clover

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Clover Design 列表骨架屏
 *
 * 数据加载时展示占位列表项，避免白屏。
 * 使用闪烁渐变模拟加载中状态。
 *
 * @param count 占位行数，默认 6
 */
@Composable
fun CloverListSkeleton(
    count: Int = 6
) {
    val isDark = isCloverDark()
    val baseColor = LocalCloverColorScheme.current.surfaceVariant

    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.4f),
        baseColor.copy(alpha = 0.7f),
        baseColor.copy(alpha = 0.4f)
    )

    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Column {
        repeat(count) {
            SkeletonRow(brush = brush)
        }
    }
}

@Composable
private fun SkeletonRow(
    brush: Brush
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CloverSizes.listItemHeight)
            .padding(
                horizontal = CloverSizes.listItemHorizontalPadding,
                vertical = CloverSizes.listItemVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CloverSpacing.md)
    ) {
        // 封面占位
        Box(
            modifier = Modifier
                .size(CloverSizes.coverSmall)
                .clip(RoundedCornerShape(CloverSizes.coverCornerRadius))
                .background(brush)
        )

        // 文字占位
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CloverSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(13.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}
