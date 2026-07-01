package cn.lemondrop.clover

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.material.CloverMaterial
import cn.lemondrop.clover.material.cloverAcrylic
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

/**
 * Clover Design Flyout 菜单项（用于 [CloverFlyout] 的 items 便捷重载）
 *
 * @param label 显示文字
 * @param onClick 点击回调
 * @param icon 可选左侧图标
 * @param isDestructive 是否危险操作（红色高亮）
 */
data class CloverFlyoutItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
)

/**
 * Clover Design Flyout 弹出菜单（items 便捷重载）
 *
 * 直接把 [items] 渲染为一组 [CloverMenuItem]；点击某项时先关闭菜单再执行其回调。
 * 定位与背景规则见下方 content 版说明。
 */
@Composable
fun CloverFlyout(
    visible: Boolean,
    onDismiss: () -> Unit,
    items: List<CloverFlyoutItem>,
    modifier: Modifier = Modifier,
    anchorBounds: Rect? = null,
    hazeState: HazeState? = null,
) {
    CloverFlyout(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        anchorBounds = anchorBounds,
        hazeState = hazeState,
    ) {
        items.forEach { item ->
            CloverMenuItem(
                label = item.label,
                onClick = {
                    onDismiss()
                    item.onClick()
                },
                icon = item.icon,
                isDestructive = item.isDestructive,
            )
        }
    }
}

/**
 * Clover Design Flyout 弹出菜单
 *
 * 小型菜单面板，带遮罩、缩放进入/退出动画、系统返回键关闭。
 *
 * 定位：
 * - 传入 [anchorBounds]（触发控件在 root 坐标系下的边界，通常用
 *   `Modifier.onGloballyPositioned { it.boundsInRoot() }` 获取）时，面板智能定位在
 *   锚点附近——优先在其下方，空间不足翻到上方，水平对齐锚点右缘并贴屏幕边裁剪。
 * - 不传 [anchorBounds] 时，回退到屏幕右下角（导航栏之上）。
 *
 * 背景：
 * - 传入 [hazeState]（需祖先/兄弟节点 `hazeSource(state)`）时使用亚克力毛玻璃；
 * - 否则使用纯色 surface 兜底。
 *
 * @param visible 是否显示
 * @param onDismiss 关闭回调
 * @param modifier 外部 modifier
 * @param anchorBounds 触发控件边界；null 则右下角弹出
 * @param hazeState 页面级 Haze 源；传入则启用亚克力
 * @param content 菜单内容，通常配合 [CloverMenuItem]
 */
@Composable
fun CloverFlyout(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    anchorBounds: Rect? = null,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrimColor = LocalCloverColorScheme.current.scrim.copy(alpha = CloverFlyoutDefaults.scrimAlpha)
    val statusTop = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
    val density = LocalDensity.current

    var panelHeightPx by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        BackHandler(enabled = visible) { onDismiss() }

        // 遮罩：淡入淡出
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(180)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }

        if (anchorBounds != null) {
            // 智能锚定定位
            val panelWpx = with(density) { CloverFlyoutDefaults.width.toPx() }
            val gapPx = with(density) { CloverFlyoutDefaults.anchorGap.toPx() }
            val marginPx = with(density) { CloverFlyoutDefaults.screenEdgePadding.toPx() }
            val statusTopPx = with(density) { statusTop.toPx() }
            val navBottomPx = with(density) { navBottom.toPx() }
            val containerWpx = constraints.maxWidth.toFloat()
            val containerHpx = constraints.maxHeight.toFloat()

            val fitsBelow =
                anchorBounds.bottom + gapPx + panelHeightPx <= containerHpx - navBottomPx - marginPx
            val yPx = if (fitsBelow) {
                anchorBounds.bottom + gapPx
            } else {
                (anchorBounds.top - gapPx - panelHeightPx).coerceAtLeast(statusTopPx + marginPx)
            }
            val maxX = (containerWpx - marginPx - panelWpx).coerceAtLeast(marginPx)
            val xPx = (anchorBounds.right - panelWpx).coerceIn(marginPx, maxX)
            val pivotX = if (panelWpx > 0f) {
                ((anchorBounds.center.x - xPx) / panelWpx).coerceIn(0f, 1f)
            } else {
                1f
            }
            val transformOrigin = TransformOrigin(pivotX, if (fitsBelow) 0f else 1f)

            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) },
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                    initialScale = CloverFlyoutDefaults.initialScale,
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    transformOrigin = transformOrigin,
                ),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(
                    targetScale = CloverFlyoutDefaults.initialScale,
                    animationSpec = tween(180),
                    transformOrigin = transformOrigin,
                ),
            ) {
                FlyoutPanel(
                    hazeState = hazeState,
                    onMeasured = { panelHeightPx = it },
                    content = content,
                )
            }
        } else {
            // 回退：屏幕右下角
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navBottom, end = CloverFlyoutDefaults.screenEdgePadding),
                enter = scaleIn(
                    initialScale = CloverFlyoutDefaults.initialScale,
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    transformOrigin = TransformOrigin(1f, 1f),
                ) + fadeIn(animationSpec = tween(200)) +
                    slideInVertically(animationSpec = tween(250)) { it / 3 },
                exit = scaleOut(
                    targetScale = CloverFlyoutDefaults.initialScale,
                    animationSpec = tween(180),
                    transformOrigin = TransformOrigin(1f, 1f),
                ) + fadeOut(animationSpec = tween(150)) +
                    slideOutVertically(animationSpec = tween(180)) { it / 3 },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    FlyoutPanel(
                        hazeState = hazeState,
                        onMeasured = { panelHeightPx = it },
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * Flyout 面板本体：固定宽度、圆角，可选亚克力背景，吞掉自身点击避免误关。
 */
@Composable
private fun FlyoutPanel(
    hazeState: HazeState?,
    onMeasured: (Int) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surface = LocalCloverColorScheme.current.surface
    val base = Modifier
        .width(CloverFlyoutDefaults.width)
        .onSizeChanged { onMeasured(it.height) }
        .clip(RoundedCornerShape(CloverFlyoutDefaults.cornerRadius))
    val panelModifier = if (hazeState != null) {
        base
            .background(surface) // Haze 失效时的保底背景
            .cloverAcrylic(
                state = hazeState,
                backgroundColor = Color.Transparent,
                tints = CloverMaterial.Acrylic.tints(baseColor = surface),
                blurRadius = CloverFlyoutDefaults.blurRadius,
            )
    } else {
        base.background(surface)
    }
    Column(
        modifier = panelModifier
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {},
            )
            .padding(vertical = CloverFlyoutDefaults.contentVerticalPadding),
        content = content,
    )
}

/**
 * Clover Design Flyout 默认参数
 */
object CloverFlyoutDefaults {
    val width = 210.dp
    val cornerRadius = CloverSizes.menuCornerRadius
    val contentVerticalPadding = 10.dp
    val screenEdgePadding = 4.dp
    val scrimAlpha = 0.3f
    val initialScale = 0.85f
    val blurRadius = 40.dp
    val anchorGap = 4.dp
}
