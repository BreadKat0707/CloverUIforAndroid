package cn.lemondrop.clover

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.github.composefluent.component.Text

/**
 * Clover Design 列表分组标题
 */
@Composable
fun CloverSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val isDark = isCloverDark()
    val color = if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight

    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = CloverSizes.listItemHorizontalPadding,
                vertical = CloverSpacing.sm
            ),
        style = CloverTypography.sectionHeader,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
