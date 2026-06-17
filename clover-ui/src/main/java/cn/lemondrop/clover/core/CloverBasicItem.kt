package cn.lemondrop.clover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 通用列表/设置项基座
 *
 * 对标 Miuix 的 BasicComponent：统一标题、副标题、前后操作区的样式与交互。
 * [CloverListItem]、[CloverMenuItem]、[CloverSettingItem] 等都基于此组件封装。
 *
 * @param title 主标题文字
 * @param subtitle 副标题文字，null 时不显示
 * @param leading 左侧图标/封面/占位内容
 * @param trailing 右侧控件（箭头、开关、更多按钮等）
 * @param enabled 是否可用，false 时文字变灰且不可点击
 * @param onClick 点击回调，null 表示不可点击
 * @param onLongClick 长按回调
 * @param backgroundColor 列表项背景色，默认透明
 * @param shape 背景形状/圆角
 * @param colors 文字颜色配置
 * @param contentPadding 内部 padding
 * @param indicationCornerRadius 按下高亮的圆角
 */
@Composable
fun CloverBasicItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    backgroundColor: Color = Color.Transparent,
    shape: Shape = CloverBasicItemDefaults.shape,
    colors: CloverBasicItemColors = CloverBasicItemDefaults.colors(),
    contentPadding: PaddingValues = CloverBasicItemDefaults.contentPadding,
    indicationCornerRadius: Dp = CloverBasicItemDefaults.indicationCornerRadius
) {
    val titleColor = if (enabled) colors.titleColor else colors.disabledTitleColor
    val subtitleColor = if (enabled) colors.subtitleColor else colors.disabledSubtitleColor

    val clickableModifier = if (onClick != null && enabled) {
        Modifier.cloverClickable(
            onClick = onClick,
            onLongClick = onLongClick ?: {},
            cornerRadius = indicationCornerRadius
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(shape)
            .then(clickableModifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CloverSpacing.md)
    ) {
        leading?.invoke()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = CloverTypography.itemTitle,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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

        trailing?.invoke(this)
    }
}

/**
 * [CloverBasicItem] 的文字颜色配置
 */
data class CloverBasicItemColors(
    val titleColor: Color,
    val subtitleColor: Color,
    val disabledTitleColor: Color,
    val disabledSubtitleColor: Color
)

/**
 * [CloverBasicItem] 的默认值与颜色工厂
 */
object CloverBasicItemDefaults {
    private const val DisabledAlpha = 0.38f

    @Composable
    fun colors(
        titleColor: Color = if (isCloverDark()) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight,
        subtitleColor: Color = if (isCloverDark()) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight,
        disabledTitleColor: Color = titleColor.copy(alpha = DisabledAlpha),
        disabledSubtitleColor: Color = subtitleColor.copy(alpha = DisabledAlpha)
    ): CloverBasicItemColors = CloverBasicItemColors(
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        disabledTitleColor = disabledTitleColor,
        disabledSubtitleColor = disabledSubtitleColor
    )

    val contentPadding: PaddingValues = PaddingValues(
        horizontal = CloverSizes.listItemHorizontalPadding,
        vertical = CloverSizes.listItemVerticalPadding
    )

    val shape: Shape = RoundedCornerShape(CloverSizes.listItemCornerRadius)

    val indicationCornerRadius: Dp = CloverSizes.listItemCornerRadius
}
