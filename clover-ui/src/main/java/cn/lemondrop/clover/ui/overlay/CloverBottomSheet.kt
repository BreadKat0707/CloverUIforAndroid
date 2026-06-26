package cn.lemondrop.clover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import io.github.composefluent.component.Text
import kotlinx.coroutines.launch

/**
 * Clover Design 底部 Sheet 容器
 *
 * - 最大高度不超过系统状态栏与导航栏之间的安全区域
 * - 出现：从屏幕底部向上弹出，带遮罩淡入
 * - 顶部带可拖拽手柄，下拉时面板跟手移动，超过阈值后收起
 * - 内容底部与 sheet 面板保留一定间距，不会紧贴
 * - 四个圆角大小一致
 *
 * @param onDismiss Sheet 完全收起后触发
 * @param modifier 外部 modifier
 * @param title 可选标题
 * @param hazeState 可选 Haze 状态，提供时使用亚克力背景而非纯色
 * @param hazeTints Haze 色调，仅在传入 hazeState 时生效
 * @param content Sheet 内容
 */
@Composable
fun CloverBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    hazeState: HazeState? = null,
    hazeTints: List<HazeTint> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isCloverDark()
    val bgColor = if (isDark) CloverColors.surfaceDark else CloverColors.surfaceLight
    val statusBarPadding = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // 面板最大高度：屏幕高度 - 状态栏 - 导航栏
        val maxPanelHeight = maxHeight - statusBarPadding - navBarPadding
        val screenHeightPx = with(density) { maxHeight.roundToPx().toFloat() }
        val dragOffset = remember { Animatable(screenHeightPx) }

        // 入场动画：从屏幕底部外滑入
        LaunchedEffect(Unit) {
            dragOffset.animateTo(0f, animationSpec = tween(300))
        }

        val progress = 1f - (dragOffset.value / screenHeightPx).coerceIn(0f, 1f)
        val scrimAlpha = if (isDark) 0.5f * progress else 0.4f * progress

        val dismissSheet = {
            scope.launch {
                dragOffset.animateTo(screenHeightPx, animationSpec = tween(250))
                onDismiss()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        (if (isDark) CloverColors.scrimDark else CloverColors.scrimLight)
                            .copy(alpha = scrimAlpha)
                    )
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = { dismissSheet() }
                    )
            )

            // 面板
            val panelShape = RoundedCornerShape(CloverBottomSheetDefaults.cornerRadius)
            val basePanelModifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CloverBottomSheetDefaults.horizontalPadding,
                    vertical = CloverBottomSheetDefaults.verticalPadding
                )
                .padding(bottom = navBarPadding)
                .heightIn(max = maxPanelHeight)
                .offset { IntOffset(0, dragOffset.value.toInt()) }
                .clip(panelShape)

            val panelModifier = if (hazeState != null) {
                basePanelModifier
                    .background(Color.Transparent)
                    .hazeEffect(state = hazeState) {
                        backgroundColor = Color.Transparent
                        blurRadius = 50.dp
                        tints = hazeTints.ifEmpty {
                            listOf(
                                HazeTint(
                                    (if (isDark) CloverColors.surfaceDark else CloverColors.surfaceLight)
                                        .copy(alpha = 0.40f)
                                )
                            )
                        }
                        noiseFactor = 0.1f
                    }
            } else {
                basePanelModifier.background(bgColor)
            }

            Column(
                modifier = panelModifier
                    .clickable(enabled = false) { /* 阻止点击穿透 */ }
            ) {
                DragHandle(
                    dragOffset = dragOffset,
                    title = title,
                    onDrag = { deltaPx ->
                        scope.launch {
                            dragOffset.snapTo((dragOffset.value + deltaPx).coerceAtLeast(0f))
                        }
                    },
                    onDragEnd = {
                        val threshold = with(density) { CloverBottomSheetDefaults.dragDismissThreshold.toPx() }
                        if (dragOffset.value > threshold) {
                            dismissSheet()
                        } else {
                            scope.launch { dragOffset.animateTo(0f, spring()) }
                        }
                    }
                )

                // 内容区（与 sheet 面板内缘保持 16.dp 边距，底部额外留白）
                val contentNestedScroll = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // 如果 sheet 已经被下拉，优先把滑动用来移动 sheet
                            if (dragOffset.value > 0f && available.y != 0f) {
                                val newOffset = (dragOffset.value + available.y)
                                    .coerceAtLeast(0f)
                                val consumed = newOffset - dragOffset.value
                                scope.launch { dragOffset.snapTo(newOffset) }
                                return Offset(0f, consumed)
                            }
                            return Offset.Zero
                        }

                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // 子列表滚动到顶后继续向下滑，把剩余滑动交给 sheet 下拉
                            if (available.y > 0f) {
                                val newOffset = (dragOffset.value + available.y)
                                    .coerceAtLeast(0f)
                                scope.launch { dragOffset.snapTo(newOffset) }
                                return Offset(0f, available.y)
                            }
                            return Offset.Zero
                        }

                        override suspend fun onPostFling(
                            consumed: Velocity,
                            available: Velocity
                        ): Velocity {
                            val threshold = with(density) {
                                CloverBottomSheetDefaults.dragDismissThreshold.toPx()
                            }
                            if (dragOffset.value > threshold || available.y > 2000f) {
                                dismissSheet()
                            } else if (dragOffset.value > 0f) {
                                scope.launch { dragOffset.animateTo(0f, spring()) }
                            }
                            return super.onPostFling(consumed, available)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = CloverBottomSheetDefaults.horizontalPadding)
                        .padding(bottom = CloverBottomSheetDefaults.contentBottomPadding)
                        .nestedScroll(contentNestedScroll),
                    content = content
                )
            }
        }
    }
}

/**
 * 顶部拖拽手柄（包含可选标题）
 *
 * 整个头部区域都能下拉收起 Sheet，避免标题区手势穿透到父布局。
 */
@Composable
private fun DragHandle(
    dragOffset: Animatable<Float, AnimationVector1D>,
    title: String?,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val alpha = if (dragOffset.value > 0.5f) 0.7f else 0.4f
    val isDark = isCloverDark()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { onDragEnd() },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // 始终允许向下拖拽；sheet 已被下拉时也允许向上拖回
                        if (dragAmount > 0f || dragOffset.value > 0f) {
                            onDrag(dragAmount)
                        }
                    }
                )
            }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
                    )
                    .alpha(alpha)
            )

            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    style = CloverTypography.titleBar,
                    color = if (isDark) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = CloverBottomSheetDefaults.horizontalPadding,
                            vertical = CloverSpacing.md
                        )
                )
            }
        }
    }
}

/**
 * Clover Design Sheet 默认参数
 */
object CloverBottomSheetDefaults {
    val horizontalPadding: Dp = CloverSizes.sheetHorizontalPadding
    val verticalPadding: Dp = CloverSizes.sheetVerticalPadding
    val cornerRadius: Dp = CloverSizes.sheetCornerRadius
    val dragHandleHeight: Dp = 20.dp
    val dragDismissThreshold: Dp = 80.dp
    val contentBottomPadding: Dp = 16.dp
}

/**
 * Clover Design Sheet 内菜单项
 *
 * 图标 + 文字一行，支持危险操作红色高亮，支持选中指示条。
 * 基于 [CloverBasicItem]。
 *
 * @param label 文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param icon 左侧图标
 * @param isSelected 是否选中（显示左侧竖条，用于 DrawerMenu 标识当前页面）
 * @param isDestructive 是否为危险操作（删除等）
 */
@Composable
fun CloverMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    isDestructive: Boolean = false
) {
    val isDark = isCloverDark()
    val normalTitleColor = if (isDark) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight
    val titleColor = when {
        isSelected -> CloverColors.accent
        isDestructive -> if (isDark) CloverColors.errorDark else CloverColors.errorLight
        else -> normalTitleColor
    }
    val iconColor = when {
        isSelected -> CloverColors.accent
        isDestructive -> if (isDark) CloverColors.errorDark else CloverColors.errorLight
        else -> if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
    }

    val indicator: @Composable (() -> Unit) = {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(CloverColors.playingIndicator)
            )
        } else {
            // 占位，保持选中/未选中项的图标对齐
            Spacer(modifier = Modifier.width(3.dp))
        }
    }

    CloverBasicItem(
        title = label,
        onClick = onClick,
        modifier = modifier,
        indicator = indicator,
        leading = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = iconColor
                )
            }
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = CloverSizes.listItemHorizontalPadding,
            vertical = CloverSpacing.md
        ),
        indicationCornerRadius = 0.dp,
        colors = CloverBasicItemDefaults.colors(titleColor = titleColor)
    )
}
