package cn.lemondrop.clover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 指示条方向。
 */
enum class CloverIndicatorOrientation {
    /** 竖条：贴内容区左侧，用于列表项、菜单项、侧边导航 */
    Vertical,
    /** 横条：贴内容区底部，用于底部导航栏、顶部标签 */
    Horizontal
}

/**
 * 指示条默认参数（对齐 WinUI Fluent Design）。
 */
object CloverIndicatorDefaults {
    val verticalWidth: Dp = 3.dp
    val verticalHeight: Dp = 16.dp
    val verticalHeightPlaying: Dp = 28.dp
    val horizontalWidth: Dp = 16.dp
    val horizontalHeight: Dp = 3.dp
    val cornerRadius: Dp = 2.dp
    const val animationDurationMs = 250
}

/**
 * Clover Design 选中/当前项指示条。
 *
 * 对齐 WinUI NavigationView 的 [SelectionIndicator](https://learn.microsoft.com/windows-app/)
 * 设计：选中时从小圆角竖条/横条动画淡入，未选中时淡出，不占额外占位空间。
 *
 * 始终保持固定的槽位尺寸，避免 opacity 动画引起布局抖动。
 *
 * @param selected 是否选中/当前项
 * @param orientation 竖条（列表/导航侧栏）或横条（底部导航/标签栏）
 * @param modifier 外部 modifier
 * @param color 指示条颜色，默认主题主色
 * @param playing 播放中模式（竖条加高到 28dp，区分普通选中）
 */
@Composable
fun CloverSelectionIndicator(
    selected: Boolean,
    orientation: CloverIndicatorOrientation,
    modifier: Modifier = Modifier,
    color: Color = LocalCloverColorScheme.current.primary,
    playing: Boolean = false
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected || playing) 1f else 0f,
        animationSpec = tween(CloverIndicatorDefaults.animationDurationMs),
        label = "indicatorAlpha"
    )

    when (orientation) {
        CloverIndicatorOrientation.Vertical -> {
            // 固定宽度的竖条槽位，居中对齐
            Box(
                modifier = modifier.width(CloverIndicatorDefaults.verticalWidth),
                contentAlignment = Alignment.Center
            ) {
                val barHeight = if (playing) CloverIndicatorDefaults.verticalHeightPlaying
                    else CloverIndicatorDefaults.verticalHeight
                Box(
                    modifier = Modifier
                        .width(CloverIndicatorDefaults.verticalWidth)
                        .height(barHeight)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(CloverIndicatorDefaults.cornerRadius))
                        .background(color)
                )
            }
        }

        CloverIndicatorOrientation.Horizontal -> {
            // 固定高度的横条槽位，居中
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(CloverIndicatorDefaults.horizontalHeight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(CloverIndicatorDefaults.horizontalWidth)
                        .height(CloverIndicatorDefaults.horizontalHeight)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(CloverIndicatorDefaults.cornerRadius))
                        .background(color)
                )
            }
        }
    }
}
