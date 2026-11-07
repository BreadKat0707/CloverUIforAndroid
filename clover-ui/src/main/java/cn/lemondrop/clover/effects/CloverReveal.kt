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
import androidx.compose.runtime.LaunchedEffect
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
 * Clover Design Reveal 按压光效
 *
 * 设计原则：
 * - [CloverRevealHost] 只负责追踪整片区域的指针位置与按压状态，**不绘制任何图形**；
 * - 子项通过 [cloverRevealItem] 读取共享状态，仅当指针为中心的圆形作用域覆盖到自身时，
 *   才在自己的边缘绘制辉光；
 * - 独立组件通过 [cloverReveal] 拥有自包含的作用域判断、边缘辉光与按压水波纹；
 * - 按压松开后，按压光效立即消失，水波纹扩散后自然消退；
 * - 没有任何跨元素的背景聚光灯被直接绘制出来。
 */
object CloverRevealDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)

    /** 非按压时，以指针为中心的圆形作用域半径 */
    val hoverRadius: Dp = 24.dp

    /** 按压时，以指针为中心的圆形作用域半径 */
    val pressedRadius: Dp = 36.dp

    /** 按压水波纹扩散半径系数（相对项最大边） */
    val releasedRippleRadiusFactor: Float = 1.8f

    /** 按压水波纹动画时长（毫秒） */
    const val rippleAnimationDurationMs: Int = 900

    /** 内边框高亮宽度 */
    val borderWidth: Dp = 0.2.dp

    /** 边缘辉光向外扩散宽度 */
    val glowWidth: Dp = 4.dp

    /** hover 时边缘辉光最大 alpha，保持极弱以免松手后残留感 */
    const val hoverGlowAlpha: Float = 0.18f

    /** 按压时边缘辉光最大 alpha */
    const val pressedGlowAlpha: Float = 1f

    /** 按压但指针滑出元素时的辉光最大 alpha */
    const val pressedOutsideGlowAlpha: Float = 0.35f

    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)

    @Composable
    fun backgroundColor(): Color = LocalCloverColorScheme.current.surface

    /**
     * 当前主题下的边缘辉光颜色。
     * 浅色模式使用主色，深色模式使用白色，均保持较高透明度以避免脏感。
     */
    @Composable
    fun glowColor(): Color {
        return if (isCloverDark()) {
            Color.White.copy(alpha = 0.55f)
        } else {
            LocalCloverColorScheme.current.primary.copy(alpha = 0.50f)
        }
    }

    /**
     * 当前主题下的水波纹颜色。
     */
    @Composable
    fun rippleColor(): Color {
        return if (isCloverDark()) {
            Color.White.copy(alpha = 0.22f)
        } else {
            LocalCloverColorScheme.current.primary.copy(alpha = 0.22f)
        }
    }
}

/**
 * 共享 Reveal 状态。
 */
@Stable
class CloverRevealState {
    /** 指针在 Host 坐标系中的位置；Unspecified 表示未追踪 */
    var pointerPosition: Offset by mutableStateOf(Offset.Unspecified)
        internal set
    var isHover: Boolean by mutableStateOf(false)
        internal set
    var isPressed: Boolean by mutableStateOf(false)
        internal set
    /** 当前被 Host 追踪的按压起点（Host 坐标系）。用于判断哪一个子项应触发水波纹。 */
    var pressStartPosition: Offset by mutableStateOf(Offset.Unspecified)
        internal set
    /** Host 自身坐标，用于把 pressStartPosition 转换到子项本地坐标。 */
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
 * 共享 Reveal 容器。
 *
 * 仅追踪整片区域的指针状态并通过 [LocalCloverRevealState] 下发；
 * **本身不绘制任何光效图形**。
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
                                state.pressStartPosition = Offset.Unspecified
                            }

                            PointerEventType.Press -> {
                                state.pointerPosition = change.position
                                state.pressStartPosition = change.position
                                state.isPressed = true
                            }

                            PointerEventType.Release -> {
                                state.isPressed = false
                                state.pressStartPosition = Offset.Unspecified
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
 * 给 [CloverRevealHost] 内部子项添加 Reveal 边缘辉光与按压水波纹。
 *
 * 效果：
 * - 以 Host 指针为中心、一定半径的圆形作用域覆盖到当前项时，项的边缘才发光；
 * - 未被作用域覆盖的项不显示任何光效；
 * - 按压时高亮增强，并在当前项内产生 Fluent 风格水波纹（可通过 [rippleEnabled] 关闭）；
 * - 松手后按压光效立即消失，水波纹扩散后自然消退。
 *
 * @param shape 项的形状
 * @param enabled 是否启用
 * @param rippleEnabled 是否启用按压水波纹
 */
fun Modifier.cloverRevealItem(
    shape: Shape = CloverRevealDefaults.shape,
    enabled: Boolean = true,
    rippleEnabled: Boolean = true
): Modifier = if (!enabled) {
    this
} else {
    composed {
        val state = LocalCloverRevealState.current
        if (state == null) {
            return@composed this.cloverReveal(shape = shape, enabled = true, rippleEnabled = rippleEnabled)
        }

        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val glowColor = CloverRevealDefaults.glowColor()
        val rippleColor = CloverRevealDefaults.rippleColor()

        var itemCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        // 子项自己追踪按压状态，避免 Host 的 pointerInput 被子项 clickable 拦截后 isPressed 不释放。
        var isSelfPressed by remember { mutableStateOf(false) }

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

        val itemSize = Size(size.width.toFloat(), size.height.toFloat())
        val rect = Rect(Offset.Zero, itemSize)

        val isPointerInside = pointerLocal != Offset.Unspecified && rect.contains(pointerLocal)

        // 以指针为中心的圆形作用域半径；自身按压时辉光范围略大
        val radiusPx = with(density) {
            (if (isSelfPressed) CloverRevealDefaults.pressedRadius else CloverRevealDefaults.hoverRadius).toPx()
        }

        // 辉光强度：指针在元素内时全亮；在元素外时按距离衰减
        val pointerDistance = if (pointerLocal == Offset.Unspecified) Float.MAX_VALUE
        else rect.distanceTo(pointerLocal)
        val illumination = when {
            pointerLocal == Offset.Unspecified -> 0f
            isPointerInside -> 1f
            pointerDistance < radiusPx -> 1f - (pointerDistance / radiusPx).coerceIn(0f, 1f)
            else -> 0f
        }

        val targetGlowAlpha = when {
            isSelfPressed && isPointerInside -> CloverRevealDefaults.pressedGlowAlpha
            isSelfPressed -> illumination * CloverRevealDefaults.pressedOutsideGlowAlpha
            isPointerInside -> illumination * CloverRevealDefaults.hoverGlowAlpha
            else -> 0f
        }
        val glowAlpha by animateFloatAsState(
            targetValue = targetGlowAlpha,
            animationSpec = tween(120),
            label = "revealItemGlowAlpha"
        )

        // 按压水波纹
        val rippleRadius = remember { Animatable(0f) }
        val rippleAlpha = remember { Animatable(0f) }
        val rippleCenter = remember { mutableStateOf(Offset.Zero) }

        fun startRipple(position: Offset) {
            if (!rippleEnabled) return
            val maxSize = maxOf(size.width, size.height).toFloat()
            val target = maxSize * CloverRevealDefaults.releasedRippleRadiusFactor
            rippleCenter.value = position
            val duration = CloverRevealDefaults.rippleAnimationDurationMs
            scope.launch {
                rippleRadius.snapTo(0f)
                rippleAlpha.snapTo(0.8f)
                launch { rippleRadius.animateTo(target, tween(duration)) }
                launch { rippleAlpha.animateTo(0f, tween(duration)) }
            }
        }

        this
            .onGloballyPositioned { itemCoordinates = it }
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val inside = change.position.x in 0f..itemSize.width &&
                                change.position.y in 0f..itemSize.height
                        when (event.type) {
                            PointerEventType.Press -> {
                                if (inside) isSelfPressed = true
                            }
                            PointerEventType.Release -> {
                                if (isSelfPressed && inside) {
                                    startRipple(change.position)
                                }
                                isSelfPressed = false
                            }
                            PointerEventType.Exit -> {
                                isSelfPressed = false
                            }
                        }
                    }
                }
            }
            .clip(shape)
            .drawWithContent {
                val borderWidthPx = with(density) { CloverRevealDefaults.borderWidth.toPx() }
                val glowWidthPx = with(density) { CloverRevealDefaults.glowWidth.toPx() }

                drawContent()

                // 边缘辉光：使用以指针为中心的径向渐变作为边框 brush，
                // 只有指针附近的边缘才会出现光效。
                if (glowAlpha > 0.001f && pointerLocal != Offset.Unspecified && radiusPx > 0) {
                    val outline = shape.createOutline(itemSize, layoutDirection, this)
                    val baseColor = glowColor

                    // 外层柔和光晕
                    if (glowWidthPx > 0) {
                        val outerGradient = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = glowAlpha * 0.5f),
                                baseColor.copy(alpha = 0f)
                            ),
                            center = pointerLocal,
                            radius = radiusPx
                        )
                        drawOutline(
                            outline = outline,
                            brush = outerGradient,
                            style = Stroke(width = borderWidthPx + glowWidthPx)
                        )
                    }

                    // 内层核心高亮
                    val innerGradient = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = glowAlpha),
                            baseColor.copy(alpha = 0f)
                        ),
                        center = pointerLocal,
                        radius = radiusPx * 0.55f
                    )
                    drawOutline(
                        outline = outline,
                        brush = innerGradient,
                        style = Stroke(width = borderWidthPx)
                    )
                }

                // 按压水波纹
                if (rippleEnabled && rippleAlpha.value > 0.001f && rippleRadius.value > 0) {
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
 * 给单个组件添加独立的 Reveal 边缘辉光与按压水波纹。
 *
 * 适用于不在 [CloverRevealHost] 内部的按钮/卡片。
 * 自身追踪指针，以组件内指针为中心形成圆形作用域，覆盖自身时显示边缘辉光。
 *
 * @param shape 裁剪形状
 * @param enabled 是否启用
 * @param rippleEnabled 是否启用按压水波纹
 */
fun Modifier.cloverReveal(
    shape: Shape = CloverRevealDefaults.shape,
    enabled: Boolean = true,
    rippleEnabled: Boolean = true
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
        var size by remember { mutableStateOf(IntSize.Zero) }

        val itemSize = Size(size.width.toFloat(), size.height.toFloat())
        val rect = Rect(Offset.Zero, itemSize)
        val isPointerInside = pointerPosition != Offset.Unspecified && rect.contains(pointerPosition)

        // 以自身内指针为中心的圆形作用域是否覆盖自身
        val radiusPx = with(density) {
            (if (isPressed) CloverRevealDefaults.pressedRadius else CloverRevealDefaults.hoverRadius).toPx()
        }
        val pointerDistance = if (pointerPosition == Offset.Unspecified) Float.MAX_VALUE
        else rect.distanceTo(pointerPosition)
        val isIlluminated = isPointerInside || pointerDistance < radiusPx
        val illumination = if (isPointerInside) 1f
        else if (isIlluminated) 1f - (pointerDistance / radiusPx).coerceIn(0f, 1f)
        else 0f

        val targetGlowAlpha = when {
            isPressed && isPointerInside -> CloverRevealDefaults.pressedGlowAlpha
            isPressed -> illumination * CloverRevealDefaults.pressedOutsideGlowAlpha
            isIlluminated -> illumination * CloverRevealDefaults.hoverGlowAlpha
            else -> 0f
        }
        val glowAlpha by animateFloatAsState(
            targetValue = targetGlowAlpha,
            animationSpec = tween(80),
            label = "revealGlowAlpha"
        )

        val rippleRadius = remember { Animatable(0f) }
        val rippleAlpha = remember { Animatable(0f) }
        val rippleCenter = remember { mutableStateOf(Offset.Zero) }

        fun startRipple(position: Offset) {
            if (!rippleEnabled) return
            val maxSize = maxOf(size.width, size.height).toFloat()
            val target = maxSize * CloverRevealDefaults.releasedRippleRadiusFactor
            rippleCenter.value = position
            val duration = CloverRevealDefaults.rippleAnimationDurationMs
            scope.launch {
                rippleRadius.snapTo(0f)
                rippleAlpha.snapTo(0.75f)
                launch { rippleRadius.animateTo(target, tween(duration)) }
                launch { rippleAlpha.animateTo(0f, tween(duration)) }
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
                                if (releasePosition != Offset.Unspecified) {
                                    startRipple(releasePosition)
                                }
                                isPressed = false
                                isHover = inside
                            }
                        }
                    }
                }
            }
            .clip(shape)
            .drawWithContent {
                drawContent()

                // 边缘辉光：径向渐变 brush 作为边框，只有指针附近才亮
                if (glowAlpha > 0.001f && pointerPosition != Offset.Unspecified) {
                    val outline = shape.createOutline(itemSize, layoutDirection, this)
                    val borderWidthPx = CloverRevealDefaults.borderWidth.toPx()
                    val glowWidthPx = CloverRevealDefaults.glowWidth.toPx()
                    val baseColor = glowColor

                    if (glowWidthPx > 0) {
                        val outerGradient = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = glowAlpha * 0.5f),
                                baseColor.copy(alpha = 0f)
                            ),
                            center = pointerPosition,
                            radius = radiusPx
                        )
                        drawOutline(
                            outline = outline,
                            brush = outerGradient,
                            style = Stroke(width = borderWidthPx + glowWidthPx)
                        )
                    }

                    val innerGradient = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = glowAlpha),
                            baseColor.copy(alpha = 0f)
                        ),
                        center = pointerPosition,
                        radius = radiusPx * 0.55f
                    )
                    drawOutline(
                        outline = outline,
                        brush = innerGradient,
                        style = Stroke(width = borderWidthPx)
                    )
                }

                // 按压水波纹
                if (rippleEnabled && rippleAlpha.value > 0.001f && rippleRadius.value > 0) {
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
 * 带 Reveal 边缘辉光与按压水波纹的按钮/卡片表面。
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
