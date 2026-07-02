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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Clover Design Reveal 边缘辉光按压效果
 *
 * 目标效果：
 * - 指针靠近元素边缘时，元素边框发出淡淡辉光（浅色模式用主色辉光，深色模式用白色/浅主色辉光）。
 * - 按压时辉光增强，并伴随一个以按压点为中心的水波纹向外扩散后消失。
 * - 光效严格限制在当前元素边界附近，不会像聚光灯一样照到相邻元素。
 * - 按压结束后，spotlight 与水波纹会立即进入消失动画，不会残留。
 *
 * 使用方式：
 * - 独立组件：直接对组件应用 [Modifier.cloverReveal]。
 * - 列表/组内：用 [CloverRevealHost] 包裹，子项用 [Modifier.cloverRevealItem] 共享指针状态，
 *   但辉光仍只绘制在各自元素边缘。
 */
object CloverRevealDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)

    /** 触发边缘辉光的距离 */
    val revealDistance: Dp = 48.dp

    /** 辉光边框宽度 */
    val borderWidth: Dp = 1.5.dp

    /** 辉光向外扩散宽度 */
    val glowWidth: Dp = 12.dp

    /** 按压水波纹扩散半径系数 */
    val releasedRippleRadiusFactor: Float = 2.4f

    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)

    @Composable
    fun backgroundColor(): Color = LocalCloverColorScheme.current.surface

    /**
     * 当前主题下的 Reveal 颜色。
     *
     * 浅色模式使用主题主色的明亮版本作为辉光，避免黑底上不可见；
     * 深色模式使用白色/浅主色，保证对比度。
     */
    @Composable
    fun glowColor(): Color {
        val scheme = LocalCloverColorScheme.current
        return if (isCloverDark()) {
            scheme.primary.copy(alpha = 0.55f)
        } else {
            scheme.primary.copy(alpha = 0.45f)
        }
    }

    /**
     * 当前主题下的水波纹颜色。
     */
    @Composable
    fun rippleColor(): Color {
        val scheme = LocalCloverColorScheme.current
        return if (isCloverDark()) {
            Color.White.copy(alpha = 0.18f)
        } else {
            scheme.primary.copy(alpha = 0.20f)
        }
    }
}

/**
 * 共享 Reveal 状态。
 *
 * 只共享指针位置与按压状态，不再绘制跨元素聚光灯。
 */
@Stable
class CloverRevealState {
    var pointerPosition: Offset by mutableStateOf(Offset.Unspecified)
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
 * 计算点 [point] 到矩形 [this] 的最短距离。
 * 点在矩形内部时返回 0；在矩形外返回到边缘的最短距离。
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
 * 共享 Reveal 容器。
 *
 * 仅负责追踪整片区域的指针状态并通过 [LocalCloverRevealState] 下发；
 * 不再绘制跨元素聚光灯，每个子项用 [cloverRevealItem] 自行绘制边缘辉光。
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
                            PointerEventType.Enter,
                            PointerEventType.Move -> {
                                state.pointerPosition = change.position
                                state.isHover = true
                            }

                            PointerEventType.Exit -> {
                                state.isHover = false
                                state.isPressed = false
                                state.pointerPosition = Offset.Unspecified
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
    ) {
        CompositionLocalProvider(LocalCloverRevealState provides state) {
            content()
        }
    }
}

/**
 * 给 [CloverRevealHost] 内部子项添加边缘辉光。
 *
 * 效果：
 * - 指针靠近元素边缘时，元素边框发出主题色辉光；
 * - 按压时辉光增强；
 * - 辉光严格限制在元素自身边界，不会照到相邻元素；
 * - 按压结束后辉光立即消退。
 *
 * @param shape 项的形状
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
        val glowColor = CloverRevealDefaults.glowColor()

        var itemCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        val itemOffset by remember {
            androidx.compose.runtime.derivedStateOf {
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
            androidx.compose.runtime.derivedStateOf {
                if (state.pointerPosition == Offset.Unspecified) Offset.Unspecified
                else state.pointerPosition - itemOffset
            }
        }

        val revealDistancePx = with(density) { CloverRevealDefaults.revealDistance.toPx() }
        val glowWidthPx = with(density) { CloverRevealDefaults.glowWidth.toPx() }
        val borderWidthPx = with(density) { CloverRevealDefaults.borderWidth.toPx() }

        val (proximity, isPressed) = remember(pointerLocal, size, state.isPressed, revealDistancePx) {
            val local = pointerLocal
            if (local == Offset.Unspecified || size == IntSize.Zero) {
                0f to false
            } else {
                val rect = Rect(Offset.Zero, Size(size.width.toFloat(), size.height.toFloat()))
                val distance = rect.distanceTo(local)
                val isInside = rect.contains(local)
                val isNear = distance < revealDistancePx
                val proximityValue = when {
                    isInside -> 1f
                    isNear -> 1f - (distance / revealDistancePx).coerceIn(0f, 1f)
                    else -> 0f
                }
                proximityValue to state.isPressed
            }
        }

        val animatedGlowAlpha by animateFloatAsState(
            targetValue = if (isPressed) 1f else proximity * 0.75f,
            animationSpec = tween(160),
            label = "revealItemGlowAlpha"
        )

        this
            .onGloballyPositioned { itemCoordinates = it }
            .onSizeChanged { size = it }
            .clip(shape)
            .drawWithContent {
                if (animatedGlowAlpha <= 0.001f || size == IntSize.Zero) {
                    drawContent()
                    return@drawWithContent
                }

                val itemSize = Size(size.width.toFloat(), size.height.toFloat())
                val outline = shape.createOutline(itemSize, layoutDirection, this)

                // 外发光：在边框外侧绘制一层渐变辉光
                if (glowWidthPx > 0) {
                    drawOutline(
                        outline = outline,
                        color = glowColor.copy(alpha = animatedGlowAlpha * 0.45f),
                        style = Stroke(width = borderWidthPx + glowWidthPx * 2),
                    )
                    drawOutline(
                        outline = outline,
                        color = glowColor.copy(alpha = animatedGlowAlpha * 0.75f),
                        style = Stroke(width = borderWidthPx + glowWidthPx),
                    )
                }

                // 内边框高亮
                drawOutline(
                    outline = outline,
                    color = glowColor.copy(alpha = animatedGlowAlpha),
                    style = Stroke(width = borderWidthPx),
                )

                drawContent()
            }
    }
}

/**
 * 给单个组件添加独立的 Reveal 边缘辉光 + 按压水波纹。
 *
 * 适用于不在 [CloverRevealHost] 内部的按钮/卡片。
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
        val glowColor = CloverRevealDefaults.glowColor()
        val rippleColor = CloverRevealDefaults.rippleColor()

        var pointerPosition by remember { mutableStateOf(Offset.Unspecified) }
        var isHover by remember { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }
        var isReleased by remember { mutableStateOf(false) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        val revealDistancePx = with(density) { CloverRevealDefaults.revealDistance.toPx() }
        val glowWidthPx = with(density) { CloverRevealDefaults.glowWidth.toPx() }
        val borderWidthPx = with(density) { CloverRevealDefaults.borderWidth.toPx() }

        val itemSize = Size(size.width.toFloat(), size.height.toFloat())
        val rect = Rect(Offset.Zero, itemSize)
        val isPointerInside = pointerPosition != Offset.Unspecified && rect.contains(pointerPosition)
        val pointerDistance = if (pointerPosition == Offset.Unspecified) Float.MAX_VALUE
        else rect.distanceTo(pointerPosition)
        val isPointerNear = isPointerInside || pointerDistance < revealDistancePx
        val proximity = if (isPointerInside) 1f
        else if (isPointerNear) 1f - (pointerDistance / revealDistancePx).coerceIn(0f, 1f)
        else 0f

        val targetGlowAlpha = when {
            isPressed && isPointerInside -> 1f
            isPressed -> proximity * 0.6f
            isPointerNear -> proximity * 0.75f
            else -> 0f
        }
        val glowAlpha by animateFloatAsState(
            targetValue = targetGlowAlpha,
            animationSpec = tween(160),
            label = "revealGlowAlpha"
        )

        val rippleRadius = remember { Animatable(0f) }
        val rippleAlpha = remember { Animatable(0f) }
        val rippleCenter = remember { mutableStateOf(Offset.Zero) }

        fun startReleaseRipple(position: Offset) {
            val maxSize = maxOf(size.width, size.height).toFloat()
            val target = maxSize * CloverRevealDefaults.releasedRippleRadiusFactor
            rippleCenter.value = position
            scope.launch {
                rippleRadius.snapTo(0f)
                rippleAlpha.snapTo(0.75f)
                launch { rippleRadius.animateTo(target, tween(380)) }
                launch { rippleAlpha.animateTo(0f, tween(380)) }
            }
        }

        fun resetPressState() {
            isPressed = false
            isReleased = true
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
                                resetPressState()
                            }

                            PointerEventType.Press -> {
                                pointerPosition = change.position
                                isPressed = true
                                isReleased = false
                                scope.launch {
                                    rippleRadius.snapTo(0f)
                                    rippleAlpha.snapTo(0f)
                                }
                            }

                            PointerEventType.Release -> {
                                val releasePosition = pointerPosition
                                if (releasePosition != Offset.Unspecified) {
                                    startReleaseRipple(releasePosition)
                                }
                                resetPressState()
                                isHover = inside
                            }
                        }
                    }
                }
            }
            .clip(shape)
            .drawWithContent {
                if (size == IntSize.Zero) {
                    drawContent()
                    return@drawWithContent
                }

                val outline = shape.createOutline(itemSize, layoutDirection, this)

                // 外发光
                if (glowAlpha > 0.001f) {
                    if (glowWidthPx > 0) {
                        drawOutline(
                            outline = outline,
                            color = glowColor.copy(alpha = glowAlpha * 0.45f),
                            style = Stroke(width = borderWidthPx + glowWidthPx * 2),
                        )
                        drawOutline(
                            outline = outline,
                            color = glowColor.copy(alpha = glowAlpha * 0.75f),
                            style = Stroke(width = borderWidthPx + glowWidthPx),
                        )
                    }
                    drawOutline(
                        outline = outline,
                        color = glowColor.copy(alpha = glowAlpha),
                        style = Stroke(width = borderWidthPx),
                    )
                }

                drawContent()

                // 按压水波纹：在内容之上绘制
                if (rippleAlpha.value > 0.001f && rippleRadius.value > 0) {
                    drawCircle(
                        color = rippleColor,
                        radius = rippleRadius.value,
                        center = rippleCenter.value,
                        alpha = rippleAlpha.value
                    )
                }
            }
    }
}

/**
 * 带 Reveal 边缘辉光的按钮/卡片表面。
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
