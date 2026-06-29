package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 横向列表项
 *
 * 用于横向滚动的封面 + 文字列表，例如：
 * - 艺术家页横向展示专辑
 * - 专辑页横向展示参与艺术家
 *
 * 排版为封面在上，标题和副标题在下，文字最多一行，超出截断。
 * 没有选中/播放指示条，圆角与标准列表项保持一致。
 *
 * @param title 主标题
 * @param subtitle 副标题，null 或空时不显示
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param modifier 外部 modifier
 * @param cover 封面/头像内容，通常放 Image 或带占位图的 Box
 * @param width 整个 item 的宽度，默认 140.dp
 * @param coverSize 封面尺寸，默认 140.dp × 140.dp
 * @param shape 封面圆角，默认与列表项一致
 */
@Composable
fun CloverHorizontalListItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    cover: @Composable () -> Unit,
    width: Dp = 140.dp,
    coverSize: Dp = 140.dp,
    shape: RoundedCornerShape = RoundedCornerShape(CloverSizes.coverCornerRadius)
) {
    val isDark = isCloverDark()
    val titleColor = LocalCloverColorScheme.current.onSurface
    val subtitleColor = LocalCloverColorScheme.current.onSurfaceVariant

    Column(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(CloverSizes.listItemCornerRadius))
            .background(Color.Transparent)
            .cloverClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                cornerRadius = CloverSizes.listItemCornerRadius
            ),
        horizontalAlignment = Alignment.Start
    ) {
        // 封面区域
        BoxPlaceholder(
            modifier = Modifier
                .size(coverSize)
                .clip(shape)
        ) {
            cover()
        }

        Spacer(modifier = Modifier.height(CloverSpacing.sm))

        // 标题
        Text(
            text = title,
            style = CloverTypography.itemTitleSmall,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 副标题
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = CloverTypography.itemSubtitle,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 仅用于占位的内部 Box，不影响外部测量，只在 content 为空时保持尺寸。
 * 这里直接作为 cover 容器使用。
 */
@Composable
private fun BoxPlaceholder(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
