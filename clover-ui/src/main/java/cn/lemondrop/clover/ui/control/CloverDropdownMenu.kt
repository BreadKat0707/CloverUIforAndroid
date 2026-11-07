package cn.lemondrop.clover

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.material.CloverMaterial
import cn.lemondrop.clover.material.cloverSolid
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

/**
 * Clover Design 下拉菜单项数据（用于 [CloverDropdownMenu] 的 items 便捷重载）。
 *
 * @param text 显示文字
 * @param onClick 点击回调
 * @param icon 可选左侧图标
 * @param enabled 是否可用
 */
data class CloverDropdownMenuItemData(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val enabled: Boolean = true
)

/**
 * Clover Design 下拉菜单（items 便捷重载）。
 *
 * 直接把 [items] 渲染为一组 [CloverDropdownMenuItem]。
 * 外观与 [CloverFlyout] 一致：圆角、可选亚克力、从锚点缩放弹出，点击遮罩/空白处或按返回键关闭。
 */
@Composable
fun CloverDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<CloverDropdownMenuItemData>,
    modifier: Modifier = Modifier,
    anchorBounds: Rect? = null,
    hazeState: HazeState? = null,
    reveal: Boolean = false,
) {
    CloverDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        anchorBounds = anchorBounds,
        hazeState = hazeState,
        reveal = reveal,
    ) {
        items.forEach { item ->
            CloverDropdownMenuItem(
                text = item.text,
                onClick = {
                    onDismissRequest()
                    item.onClick()
                },
                icon = item.icon,
                enabled = item.enabled
            )
        }
    }
}

/**
 * Clover Design 下拉菜单容器。
 *
 * 与 Material3 [androidx.compose.material3.DropdownMenu] 不同，此组件作为同层覆盖层绘制，
 * 不再使用 [Popup]，以便与祖先节点的 [HazeState] 共享亚克力模糊。
 * 调用方应将其放在 root 级容器（如 `Box(Modifier.fillMaxSize())`）中，避免被父布局裁剪。
 *
 * 视觉风格与 [CloverFlyout] 一致：
 * - 传入 [anchorBounds]（触发控件在 root 坐标系下的边界）时，面板智能定位在锚点下方，
 *   空间不足则翻到上方，水平对齐锚点左缘并贴屏幕边裁剪；
 * - 传入 [hazeState] 时使用亚克力毛玻璃背景，否则使用纯色 surface 兜底；
 * - 从锚点缩放进入/退出，带一层很淡的遮罩，点击遮罩或按返回键会先播放退出动画再关闭。
 *
 * @param expanded 是否显示
 * @param onDismissRequest 关闭回调
 * @param modifier 外部 modifier
 * @param anchorBounds 触发控件边界；null 时面板在窗口顶部居中弹出
 * @param hazeState 页面级 Haze 源；传入则启用亚克力
 * @param reveal 是否给面板整体启用 CloverReveal 边缘辉光（默认 false）
 * @param content 菜单内容，通常配合 [CloverDropdownMenuItem]
 */
@Composable
fun CloverDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchorBounds: Rect? = null,
    hazeState: HazeState? = null,
    reveal: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = expanded

    var hasOpened by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) { if (expanded) hasOpened = true }

    LaunchedEffect(hasOpened, visibleState.isIdle, visibleState.currentState, visibleState.targetState) {
        if (hasOpened && visibleState.isIdle && !visibleState.currentState && !visibleState.targetState) {
            hasOpened = false
            onDismissRequest()
        }
    }

    if (expanded || visibleState.currentState) {
        BackHandler(enabled = visibleState.currentState || visibleState.targetState) {
            visibleState.targetState = false
        }

        val scrimColor = LocalCloverColorScheme.current.scrim.copy(
            alpha = CloverDropdownMenuDefaults.scrimAlpha
        )
        val statusTop = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
        val navBottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
        val density = LocalDensity.current

        var panelHeightPx by remember { mutableIntStateOf(0) }
        var isPanelMeasured by remember { mutableStateOf(false) }

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            // 遮罩：点击空白处收回
            AnimatedVisibility(
                visibleState = visibleState,
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
                            onClick = { visibleState.targetState = false }
                        )
                )
            }

            val metrics = computePxMetrics(
                panelWidth = CloverDropdownMenuDefaults.width,
                gap = CloverDropdownMenuDefaults.anchorGap,
                margin = CloverDropdownMenuDefaults.screenEdgePadding,
                statusTop = statusTop,
                navBottom = navBottom,
                containerWidthPx = constraints.maxWidth.toFloat(),
                containerHeightPx = constraints.maxHeight.toFloat(),
                density = density
            )
            val (xPx, yPx, transformOrigin) = if (anchorBounds != null) {
                val pos = calculateAnchoredPosition(
                    anchorBounds = anchorBounds,
                    panelWidthPx = metrics.panelWidthPx,
                    panelHeightPx = panelHeightPx,
                    gapPx = metrics.gapPx,
                    marginPx = metrics.marginPx,
                    statusTopPx = metrics.statusTopPx,
                    navBottomPx = metrics.navBottomPx,
                    containerWidthPx = metrics.containerWidthPx,
                    containerHeightPx = metrics.containerHeightPx,
                    horizontalAlign = AnchorHorizontalAlign.Left
                )
                Triple(pos.xPx, pos.yPx, pos.transformOrigin)
            } else {
                // 无锚点时居中偏上
                Triple(
                    (metrics.containerWidthPx - metrics.panelWidthPx) / 2f,
                    metrics.statusTopPx + metrics.marginPx,
                    TransformOrigin(0.5f, 0f)
                )
            }

            AnimatedVisibility(
                visibleState = visibleState,
                modifier = Modifier.offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) },
                enter = fadeIn(animationSpec = tween(150)) +
                        scaleIn(
                            initialScale = CloverDropdownMenuDefaults.initialScale,
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            transformOrigin = transformOrigin,
                        ),
                exit = fadeOut(animationSpec = tween(120)) +
                        scaleOut(
                            targetScale = CloverDropdownMenuDefaults.initialScale,
                            animationSpec = tween(160),
                            transformOrigin = transformOrigin,
                        ),
            ) {
                DropdownMenuPanel(
                    hazeState = hazeState,
                    reveal = reveal,
                    onMeasured = {
                        panelHeightPx = it
                        isPanelMeasured = true
                    },
                    content = content,
                    modifier = Modifier.alpha(if (isPanelMeasured) 1f else 0f),
                )
            }
        }
    }
}

/**
 * Clover Design 下拉菜单项。
 *
 * 视觉风格与 [CloverMenuItem] 一致：图标 + 文字一行，支持禁用态。
 *
 * @param text 菜单文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param icon 左侧图标
 * @param enabled 是否可用
 */
@Composable
fun CloverDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    CloverMenuItem(
        label = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        icon = icon,
        enabled = enabled
    )
}

/**
 * 下拉菜单面板本体：固定宽度、圆角，可选亚克力背景，吞掉自身点击避免误关。
 */
@Composable
private fun DropdownMenuPanel(
    hazeState: HazeState?,
    reveal: Boolean,
    onMeasured: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surface = LocalCloverColorScheme.current.surface
    val shape = RoundedCornerShape(CloverDropdownMenuDefaults.cornerRadius)
    val base = modifier
        .width(CloverDropdownMenuDefaults.width)
        .onSizeChanged { onMeasured(it.height) }
        .clip(shape)
    val panelModifier = if (hazeState != null) {
        base
            .cloverSolid(
                backgroundColor = CloverMaterial.Solid.backgroundColor()
            )
    } else {
        base.background(surface)
    }
        .let { if (reveal) it.cloverReveal(shape = shape) else it }
    Column(
        modifier = panelModifier
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {},
            )
            .padding(vertical = CloverDropdownMenuDefaults.contentVerticalPadding),
        horizontalAlignment = Alignment.Start,
        content = content,
    )
}

/**
 * Clover Design 下拉菜单默认参数。
 */
object CloverDropdownMenuDefaults {
    val width = 200.dp
    val cornerRadius = CloverSizes.menuCornerRadius
    val contentVerticalPadding = 8.dp
    val screenEdgePadding = 4.dp
    val anchorGap = 4.dp
    val initialScale = 0.9f
    val scrimAlpha = 0.32f
}
