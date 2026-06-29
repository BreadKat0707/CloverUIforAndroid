package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Clover Design 标准列表项
 *
 * 基于 [CloverBasicItem]，增加了固定高度、左侧选中/播放指示条。
 * 指示条紧贴条目左边缘显示。
 *
 * @param title 主标题
 * @param subtitle 副标题，null 时不显示
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param modifier 外部 modifier
 * @param leading 左侧图标/封面/占位内容
 * @param trailing 右侧控件（箭头、更多按钮等）
 * @param isSelected 是否选中（显示左侧指示条）
 * @param isPlaying 是否正在播放（显示更高指示条）
 * @param isLarge 是否使用大高度（64.dp），默认 56.dp
 * @param backgroundColor 列表项背景色，默认透明
 */
@Composable
fun CloverListItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    isSelected: Boolean = false,
    isPlaying: Boolean = false,
    isLarge: Boolean = false,
    backgroundColor: Color = Color.Transparent
) {
    val indicatorHeight = when {
        isPlaying -> 40.dp
        isSelected -> 24.dp
        else -> 0.dp
    }

    val indicator: @Composable (() -> Unit) = {
        if (indicatorHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(indicatorHeight)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(LocalCloverColorScheme.current.primary)
            )
        } else {
            // 占位，保持选中/未选中项的 leading 对齐
            Spacer(modifier = Modifier.width(3.dp))
        }
    }

    CloverBasicItem(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .height(if (isLarge) CloverSizes.listItemHeightLarge else CloverSizes.listItemHeight),
        indicator = indicator,
        leading = leading,
        trailing = trailing?.let { { it() } },
        backgroundColor = backgroundColor
    )
}
