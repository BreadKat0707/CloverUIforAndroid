package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
 * Clover Design 底部导航栏。
 *
 * 参考 Miuix NavigationBar 的交互与布局思路：
 * - 每个选项在各自的等分区域内居中，带圆角背景；
 * - 使用 `selectable` + [MutableInteractionSource] 处理按压状态与无障碍；
 * - 按压时图标/文字透明度降低，松手后恢复；
 * - 选中项使用主题主色，未选中项使用弱化色。
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 选中回调
 * @param modifier 外部 modifier
 * @param color 底栏背景色
 * @param contentColor 未选中项内容颜色
 * @param selectedColor 选中项内容颜色
 * @param pressedAlpha 按压时内容透明度
 * @param unselectedAlpha 未选中项内容默认透明度
 * @param itemShape 每个选项的按压背景形状
 * @param content 自定义项内容；默认显示图标 + 文字
 */
@Composable
fun CloverBottomNavbar(
    items: List<CloverNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalCloverColorScheme.current.surface,
    contentColor: Color = LocalCloverColorScheme.current.onSurfaceVariant,
    selectedColor: Color = LocalCloverColorScheme.current.primary,
    pressedAlpha: Float = CloverBottomNavbarDefaults.PressedAlpha,
    unselectedAlpha: Float = CloverBottomNavbarDefaults.UnselectedAlpha,
    itemShape: Shape = RoundedCornerShape(CloverBottomNavbarDefaults.ItemCornerRadius),
    content: @Composable RowScope.(index: Int, item: CloverNavItem, selected: Boolean) -> Unit = { index, item, selected ->
        DefaultBottomNavbarItem(
            item = item,
            selected = selected,
            contentColor = contentColor,
            selectedColor = selectedColor,
            pressedAlpha = pressedAlpha,
            unselectedAlpha = unselectedAlpha
        )
    }
) {
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val consumeTouches = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(consumeTouches)
            .background(color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CloverBottomNavbarDefaults.ItemHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = CloverBottomNavbarDefaults.ItemHorizontalPadding)
                        .clip(itemShape)
                        .cloverRevealItem(shape = itemShape)
                        .selectable(
                            selected = selected,
                            onClick = { onItemSelected(index) },
                            role = Role.Tab,
                            interactionSource = interactionSource,
                            indication = null
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(
                            LocalCloverBottomNavbarInteractionSource provides interactionSource
                        ) {
                            content(index, item, selected)
                        }
                    }
                    CloverSelectionIndicator(
                        selected = selected,
                        orientation = CloverIndicatorOrientation.Horizontal
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarBottomPadding)
        )
    }
}

/**
 * 默认底部导航项：图标 + 文字。
 */
@Composable
private fun DefaultBottomNavbarItem(
    item: CloverNavItem,
    selected: Boolean,
    contentColor: Color,
    selectedColor: Color,
    pressedAlpha: Float,
    unselectedAlpha: Float
) {
    val interactionSource = LocalCloverBottomNavbarInteractionSource.current
        ?: remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = if (selected) selectedColor else contentColor
    val alpha = when {
        isPressed -> pressedAlpha
        selected -> 1f
        else -> unselectedAlpha
    }
    val tint = baseColor.copy(alpha = alpha)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription ?: item.label,
            modifier = Modifier.size(CloverBottomNavbarDefaults.IconSize),
            tint = tint
        )
        Spacer(modifier = Modifier.height(CloverBottomNavbarDefaults.IconSpacing))
        Text(
            text = item.label,
            style = CloverTypography.caption,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 底部导航栏默认参数。
 */
object CloverBottomNavbarDefaults {
    val ItemHeight = 64.dp
    val IconSize = 24.dp
    val IconSpacing = 4.dp
    val ItemHorizontalPadding = 4.dp
    val ItemCornerRadius = 12.dp
    const val PressedAlpha = 0.5f
    const val UnselectedAlpha = 0.6f
}

/**
 * 内部 CompositionLocal，用于把 [CloverBottomNavbar] 的 interaction source 传递给默认项内容。
 */
internal val LocalCloverBottomNavbarInteractionSource =
    androidx.compose.runtime.staticCompositionLocalOf<MutableInteractionSource?> { null }
