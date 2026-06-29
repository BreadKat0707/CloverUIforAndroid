package cn.lemondrop.clover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * 整个底栏宽度按选项数量等分，每个选项在各自的区域内居中。
 * 选中项使用强调色高亮，未选中项使用弱化色。
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 选中回调
 * @param modifier 外部 modifier
 */
@Composable
fun CloverBottomNavbar(
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CloverSizes.bottomNavHeight)
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val tint = if (selected) LocalCloverColorScheme.current.primary else {
                LocalCloverColorScheme.current.onSurfaceVariant
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .cloverClickable(
                        onClick = { onItemSelected(index) },
                        cornerRadius = 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription ?: item.label,
                    modifier = Modifier.size(CloverSizes.iconMedium),
                    tint = tint
                )
                Spacer(modifier = Modifier.height(4.dp))
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

/**
 * Clover Design 底部导航栏默认参数
 */
object CloverBottomNavbarDefaults {
    val height = CloverSizes.bottomNavHeight
    val iconSize = CloverSizes.iconMedium
    val iconSpacing = 4.dp
    val itemCornerRadius = 8.dp
}
