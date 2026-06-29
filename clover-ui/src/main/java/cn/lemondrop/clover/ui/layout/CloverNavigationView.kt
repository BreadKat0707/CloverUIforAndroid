package cn.lemondrop.clover.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.CloverNavItem
import cn.lemondrop.clover.CloverSizes
import cn.lemondrop.clover.CloverSpacing
import cn.lemondrop.clover.CloverTypography
import cn.lemondrop.clover.LocalCloverColorScheme
import cn.lemondrop.clover.cloverClickable
import com.composables.icons.lucide.Album
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Music
import io.github.composefluent.component.Text

/**
 * Fluent Design 风格的左侧导航视图（Navigation View）
 *
 * 与 [CloverNavigationRail] 相比， NavigationView 更宽，
 * 更适合展示带文字说明的导航项，并可扩展分组、页眉/页脚。
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 选中回调
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，null 表示由外层容器提供
 * @param header 顶部额外内容（如 Logo、搜索框）
 * @param footer 底部额外内容
 */
@Composable
fun CloverNavigationView(
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    header: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null
) {
    val bgColor = backgroundColor ?: Color.Transparent

    Column(
        modifier = modifier
            .width(CloverSizes.navigationViewWidth)
            .fillMaxHeight()
            .background(bgColor)
            .padding(horizontal = CloverSpacing.md, vertical = CloverSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CloverSpacing.xs)
    ) {
        header?.invoke()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val tint = if (selected) {
                    LocalCloverColorScheme.current.primary
                } else {
                    LocalCloverColorScheme.current.onSurfaceVariant
                }
                val itemBg = if (selected) {
                    LocalCloverColorScheme.current.surfaceVariant.copy(alpha = 0.5f)
                } else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(itemBg)
                        .cloverClickable(
                            onClick = { onItemSelected(index) },
                            cornerRadius = 8.dp
                        )
                        .padding(horizontal = CloverSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription ?: item.label,
                        modifier = Modifier.size(24.dp),
                        tint = tint
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.label,
                        style = CloverTypography.itemTitle,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        footer?.invoke()
    }
}

@Preview
@Composable
private fun CloverNavigationViewPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(LocalCloverColorScheme.current.background)
    ) {
        CloverNavigationView(
            items = listOf(
                CloverNavItem("歌曲", Lucide.Music),
                CloverNavItem("专辑", Lucide.Album),
                CloverNavItem("艺术家", Lucide.Mic),
                CloverNavItem("文件夹", Lucide.FolderOpen)
            ),
            selectedIndex = 0,
            onItemSelected = {}
        )
    }
}
