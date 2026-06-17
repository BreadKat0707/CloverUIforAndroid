package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Album
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Music
import io.github.composefluent.component.Text

/**
 * Clover Design 侧边导航栏（Navigation Rail）
 *
 * 用于平板、折叠屏展开、桌面等大屏幕设备，替代底部的 `CloverBottomNavbar`。
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 选中回调
 * @param modifier 外部 modifier
 * @param backgroundColor 背景色，默认使用 surface 颜色
 * @param header 顶部额外内容（如 Logo、播放控制）
 * @param footer 底部额外内容
 */
@Composable
fun CloverNavigationRail(
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    header: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null
) {
    val isDark = isCloverDark()
    val bgColor = backgroundColor ?: if (isDark) CloverColors.surfaceDark else CloverColors.surfaceLight

    Column(
        modifier = modifier
            .width(CloverSizes.navigationRailWidth)
            .fillMaxHeight()
            .background(bgColor)
            .padding(vertical = CloverSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header?.invoke()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CloverSpacing.sm, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val tint = if (selected) CloverColors.accent else {
                    if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
                }
                val itemBg = if (selected) {
                    (if (isDark) CloverColors.surfaceVariantDark else CloverColors.surfaceVariantLight)
                        .copy(alpha = 0.5f)
                } else Color.Transparent

                Box(
                    modifier = Modifier
                        .background(itemBg)
                        .cloverClickable(
                            onClick = { onItemSelected(index) },
                            cornerRadius = 8.dp
                        )
                        .padding(horizontal = CloverSpacing.sm, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription ?: item.label,
                            modifier = Modifier.size(24.dp),
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

        footer?.invoke()
    }
}

@Preview
@Composable
private fun CloverNavigationRailPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        CloverNavigationRail(
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
