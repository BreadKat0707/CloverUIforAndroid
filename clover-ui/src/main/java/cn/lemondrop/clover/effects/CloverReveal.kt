package cn.lemondrop.clover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Clover Design Reveal 按压光效
 *
 * 参考 WinUI2 / UWP 的 Reveal Highlight：
 * - [CloverRevealHost] 绘制一个跨项共享的 SpotLight，跟随指针移动
 * - 每个子项用 [Modifier.cloverRevealItem] 只负责自己的边框亮光；
 *   指针靠近时边框渐亮，相邻项共享同一片光照
 * - 独立组件用 [Modifier.cloverReveal] 拥有完整的光斑 + 边框 + 水波纹反馈
 *
 * 已适配亮色/暗色主题。
 */
object CloverRevealDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)

    val hoverRadius: Dp = 90.dp
    val pressedRadius: Dp = 180.dp
    val releasedRippleRadiusFactor: Float = 2.4f

    val revealDistance: Dp = 60.dp
    val borderWidth: Dp = 1.5.dp

    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)

    @Composable
    fun backgroundColor(): Color = LocalCloverColorScheme.current.surface

    /**
     * 返回当前主题下的 Reveal 颜色组合。
     */
    @Composable
    fun colors(): CloverRevealColors {
        val isDark = isCloverDark()
        return if (isDark) {
            CloverRevealColors(
                spotlightColor = Color.White.copy(alpha = 0.30f),
                borderColor = Color.White.copy(alpha = 0.45f),
                spotlightBlendMode = BlendMode.Screen,
                borderBlendMode = BlendMode.Overlay
            )
        } else {
            CloverRevealColors(
                spotlightColor = Color.Black.copy(alpha = 0.16f),
                borderColor = Color.Black.copy(alpha = 0.30f),
                spotlightBlendMode = BlendMode.Overlay,
                borderBlendMode = BlendMode.Overlay
            )
        }
    }
}

/**
 * Reveal 颜色与混合模式组合。
 */
data class CloverRevealColors(
    val spotlightColor: Color,
    val borderColor: Color,
    val spotlightBlendMode: BlendMode,
    val borderBlendMode: BlendMode
)

/**
 * 共享 Reveal 光照层状态。
 */
@Stable
class CloverRevealState {
    var pointerPosition: Offset by mutableStateOf(Offset.Zero)
        internal set
    var isHover: Boolean by mutableStateOf(false)
        internal set
    var isPressed: Boolean by mutableStateOf(false)
        internal set
    var hostCoordinates: LayoutCoordinates? by mutableStateOf(null)
        internal set
}

/**
 * 在 RevealHost 内部提供共享 [CloverRevealState] 的 CompositionLocal。
 */
val LocalCloverRevealState = staticCompositionLocalOf<CloverRevealState?> { null }

@Composable
fun rememberCloverRevealState(): CloverRevealState = remember { CloverRevealState() }

/**
 * 计算点 [point] 到矩形 [this] 的最短距离。点在矩形内部时返回 0。
 */
private fun Rect.distanceTo(point: Offset): Float {
    if (contains(point)) return 0f
    val dx = when {
        point.x < left -> left - point.x
        point.x > right -> point.x - right
        else -> 0f
    }
    val dy = when {
        point.y < top -> top - point.y
        point.y > bottom -> point.y - bottom
        else -> 0f
    }
    return sqrt(dx * dx + dy * dy)
}

/**
 * 共享 Reveal 光照层容器。
 *
 * 包裹列表/按钮后，会在整片区域上方绘制一个跟随指针的 SpotLight，
 * 让相邻子项共享同一片光照。子项使用 [Modifier.cloverRevealItem] 只点亮自己的边框。
 */
@Composable
fun CloverRevealHost(
    modifier: Modifier = Modifier,
    state: CloverRevealState = rememberCloverRevealState(),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier) { content() }
        return
    }

    val density = LocalDensity.current
    val colors = CloverRevealDefaults.colors()

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                state.hostCoordinates = coordinates
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        when (event.type) {
                            PointerEventType.Enter -> {
                                state.pointerPosition = change.position
                                state.isHover = true
                            }

                            PointerEventType.Move -> {
                                state.pointerPosition = change.position
                                state.isHover = true
                            }

                            PointerEventType.Exit -> {
                                state.isHover = false
                                state.isPressed = false
                            }

                            PointerEventType.Press -> {
                                state.pointerPosition = change.position
                                state.isPressed = true
                            }

                            PointerEventType.Release -> {
                                state.isPressed = false
                            }
                        }
                    }
                }
            }
            .drawWithContent {
                drawContent()

                val radius = if (state.isPressed && state.isHover) {
                    with(density) { CloverRevealDefaults.pressedRadius.toPx() }
                } else {
                    with(density) { CloverRevealDefaults.hoverRadius.toPx() }
                }
                val alpha = if (state.isPressed && state.isHover) 0.9f else 0.55f

                if (state.isHover && radius > 0) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to colors.spotlightColor.copy(alpha = alpha),
                            0.5f to colors.spotlightColor.copy(alpha = alpha * 0.25f),
                            1.0f to Color.Transparent,
                            center = state.pointerPosition,
                            radius = radius
                        ),
                        center = state.pointerPosition,
                        radius = radius,
                        blendMode = colors.spotlightBlendMode
                    )
                }
            }
    ) {
        CompositionLocalProvider(LocalCloverRevealState provides state) {
            content()
        }
    }
}

/**
 * 给 [CloverRevealHost] 内部的子项添加共享 Reveal 边框亮光。
 *
 * 效果：
 * - 指针靠近时，该项边框渐亮
 * - 相邻项会一起被点亮（共享光照）
 * - 不会产生额外的背景填充，也不会把相邻项“按下去”
 *
 * @param shape 项的形状，决定边框绘制区域
 * @param enabled 是否启用
 */
fun Modifier.cloverRevealItem(
    shape: Shape = CloverRevealDefaults.shape,
    enabled: Boolean = true
): Modifier = if (!enabled) {
    this
} else {
    composed {
        val state = LocalCloverRevealState.current
        if (state == null) {
            return@composed this.cloverReveal(shape = shape, enabled = true)
        }

        val density = LocalDensity.current
        val colors = CloverRevealDefaults.colors()

        var itemCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        val itemOffset by remember {
            derivedStateOf {
                val host = state.hostCoordinates
                val item = itemCoordinates
                if (host != null && item != null && host.isAttached && item.isAttached) {
                    host.localPositionOf(item, Offset.Zero)
                } else {
                    Offset.Zero
                }
            }
        }

        val pointerLocal by remember {
            derivedStateOf { state.pointerPosition - itemOffset }
        }

        val distance by remember {
            derivedStateOf {
                val rect = Rect(
                    Offset.Zero,
                    Size(size.width.toFloat(), size.height.toFloat())
                )
                rect.distanceTo(pointerLocal)
            }
        }

        val revealDistancePx = with(density) { CloverRevealDefaults.revealDistance.toPx() }
        val isNear = distance < revealDistancePx
        val proximity = if (isNear) 1f - (distance / revealDistancePx).coerceIn(0f, 1f) else 0f

        val borderAlpha by animateFloatAsState(
            targetValue = if (isNear) proximity * 0.8f else 0f,
            animationSpec = tween(160),
            label = "revealItemBorderAlpha"
        )

        this
            .onGloballyPositioned { itemCoordinates = it }
            .onSizeChanged { size = it }
            .clip(shape)
            .drawWithContent {
                val itemSize = Size(size.width.toFloat(), size.height.toFloat())

                if (borderAlpha > 0.001f) {
                    val outline = shape.createOutline(itemSize, layoutDirection, this)
                    drawOutline(
                        outline = outline,
                        color = colors.borderColor.copy(alpha = borderAlpha),
                        style = Stroke(width = CloverRevealDefaults.borderWidth.toPx()),
                        blendMode = colors.borderBlendMode
                    )
                }

                drawContent()
            }
    }
}

/**
 * 给单个组件添加独立的 Reveal 按压光效（SpotLight + 边框 + 水波纹）。
 *
 * 适用于不在 [CloverRevealHost] 内部、需要完整按压反馈的按钮/卡片。
 *
 * @param shape 裁剪形状
 * @param enabled 是否启用
 */
fun Modifier.cloverReveal(
    shape: Shape = CloverRevealDefaults.shape,
    enabled: Boolean = true
): Modifier = if (!enabled) {
    this
} else {
    composed {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val colors = CloverRevealDefaults.colors()

        var pointerPosition by remember { mutableStateOf(Offset.Zero) }
        var isHover by remember { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        val itemSize = Size(size.width.toFloat(), size.height.toFloat())
        val isPointerInside = pointerPosition.x in 0f..itemSize.width &&
                pointerPosition.y in 0f..itemSize.height

        val targetSpotAlpha = when {
            isPressed && isPointerInside -> 1f
            isHover && isPointerInside -> 0.55f
            else -> 0f
        }
        val targetSpotRadius = when {
            isPressed && isPointerInside -> with(density) { CloverRevealDefaults.pressedRadius.toPx() }
            isHover && isPointerInside -> with(density) { CloverRevealDefaults.hoverRadius.toPx() }
            else -> 0f
        }
        val spotAlpha by animateFloatAsState(
            targetValue = targetSpotAlpha,
            animationSpec = tween(150),
            label = "revealSpotAlpha"
        )
        val spotRadius by animateFloatAsState(
            targetValue = targetSpotRadius,
            animationSpec = tween(220),
            label = "revealSpotRadius"
        )

        val borderAlpha by animateFloatAsState(
            targetValue = if ((isHover || isPressed) && isPointerInside) 0.8f else 0f,
            animationSpec = tween(150),
            label = "revealBorderAlpha"
        )

        val rippleRadius = remember { Animatable(0f) }
        val rippleAlpha = remember { Animatable(0f) }

        fun startReleaseRipple(position: Offset) {
            val maxSize = maxOf(size.width, size.height).toFloat()
            val target = maxSize * CloverRevealDefaults.releasedRippleRadiusFactor
            scope.launch {
                rippleRadius.snapTo(0f)
                rippleAlpha.snapTo(0.8f)
                launch { rippleRadius.animateTo(target, tween(380)) }
                launch { rippleAlpha.animateTo(0f, tween(380)) }
            }
        }

        this
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val inside = change.position.x in 0f..itemSize.width &&
                                change.position.y in 0f..itemSize.height

                        when (event.type) {
                            PointerEventType.Enter -> {
                                pointerPosition = change.position
                                isHover = true
                            }

                            PointerEventType.Move -> {
                                pointerPosition = change.position
                                isHover = inside
                            }

                            PointerEventType.Exit -> {
                                isHover = false
                                isPressed = false
                            }

                            PointerEventType.Press -> {
                                pointerPosition = change.position
                                isPressed = true
                                scope.launch {
                                    rippleRadius.snapTo(0f)
                                    rippleAlpha.snapTo(0f)
                                }
                            }

                            PointerEventType.Release -> {
                                val releasePosition = pointerPosition
                                isPressed = false
                                isHover = inside
                                startReleaseRipple(releasePosition)
                            }
                        }
                    }
                }
            }
            .clip(shape)
            .drawWithContent {
                if (spotAlpha > 0.001f && spotRadius > 0) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to colors.spotlightColor.copy(alpha = spotAlpha),
                            0.5f to colors.spotlightColor.copy(alpha = spotAlpha * 0.25f),
                            1.0f to Color.Transparent,
                            center = pointerPosition,
                            radius = spotRadius
                        ),
                        center = pointerPosition,
                        radius = spotRadius,
                        blendMode = colors.spotlightBlendMode
                    )
                }

                if (rippleAlpha.value > 0.001f && rippleRadius.value > 0) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to colors.spotlightColor.copy(alpha = rippleAlpha.value),
                            0.45f to colors.spotlightColor.copy(alpha = rippleAlpha.value * 0.22f),
                            1.0f to Color.Transparent,
                            center = pointerPosition,
                            radius = rippleRadius.value
                        ),
                        center = pointerPosition,
                        radius = rippleRadius.value,
                        blendMode = colors.spotlightBlendMode
                    )
                }

                if (borderAlpha > 0.001f) {
                    val outline = shape.createOutline(itemSize, layoutDirection, this)
                    drawOutline(
                        outline = outline,
                        color = colors.borderColor.copy(alpha = borderAlpha),
                        style = Stroke(width = CloverRevealDefaults.borderWidth.toPx()),
                        blendMode = colors.borderBlendMode
                    )
                }

                drawContent()
            }
    }
}

/**
 * 带 Reveal 按压光效的按钮/卡片表面。
 */
@Composable
fun CloverRevealSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CloverRevealDefaults.shape,
    backgroundColor: Color = CloverRevealDefaults.backgroundColor(),
    contentColor: Color = remember(backgroundColor) {
        if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    },
    contentPadding: PaddingValues = CloverRevealDefaults.contentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .cloverReveal(shape = shape, enabled = enabled)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CloverSpacing.sm),
                content = content
            )
        }
    }
}
