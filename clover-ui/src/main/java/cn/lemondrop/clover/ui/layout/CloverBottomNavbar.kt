package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 底部导航栏项数据
 *
 * @param label 标签文字
 * @param icon 图标
 * @param contentDescription 无障碍描述，默认使用 label
 */
data class CloverNavItem(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String? = null
)

/**
 * Clover Design 底部导航栏
 *
 * 位于屏幕底部，用于一级页面切换。选中项使用强调色高亮，未选中项使用弱化色。
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 选中回调
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，null 表示透明（通常由外层 Haze 面板提供背景）
 */
@Composable
fun CloverBottomNavbar(
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .height(CloverSizes.bottomNavHeight)
        .padding(horizontal = 12.dp)

    val containerModifier = if (backgroundColor != null) {
        baseModifier.background(backgroundColor)
    } else {
        baseModifier
    }

    Row(
        modifier = containerModifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val tint = if (selected) CloverColors.accent else {
                if (isCloverDark()) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
            }

            Box(
                modifier = Modifier
                    .cloverClickable(
                        onClick = { onItemSelected(index) },
                        cornerRadius = 8.dp
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription ?: item.label,
                        modifier = Modifier.size(22.dp),
                        tint = tint
                    )
                    Text(
                        text = item.label,
                        style = CloverTypography.caption,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Clover Design 底部导航栏默认参数
 */
object CloverBottomNavbarDefaults {
    val height = CloverSizes.bottomNavHeight
    val horizontalPadding = 12.dp
    val iconSize = 22.dp
    val iconSpacing = 2.dp
    val itemCornerRadius = 8.dp
}
