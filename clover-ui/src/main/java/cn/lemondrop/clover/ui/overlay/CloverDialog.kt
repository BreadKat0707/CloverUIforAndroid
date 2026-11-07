package cn.lemondrop.clover

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import io.github.composefluent.component.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.material.CloverMaterial
import cn.lemondrop.clover.material.cloverSolid
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import dev.chrisbanes.haze.HazeState

/**
 * Clover Design 弹窗位置
 */
enum class CloverDialogPosition {
    /** 屏幕中央 */
    Center,
    /** 屏幕底部 */
    Bottom
}

/**
 * Clover Design 弹窗按钮排列方式
 */
enum class CloverDialogButtonArrangement {
    /** 跟随 [position] 默认行为：居中时并排均分，底部时竖向拉伸 */
    Auto,
    /** 竖向堆叠，每个按钮拉伸到两侧 */
    StackedFill,
    /** 横向并排，按钮均分宽度 */
    SideBySide
}

/**
 * Clover Design 通用 Dialog（Miuix 风格）
 *
 * 作为同层覆盖层绘制，而非系统独立窗口，因此可与祖先节点的 [HazeState] 共享
 * 亚克力模糊。调用方应将其放在 root 级容器（如 `Box(Modifier.fillMaxSize())`）中，
 * 以确保遮罩覆盖整个页面。
 *
 * 居中或底部显示；底部使用从下滑入/向下滑出动画。
 *
 * @param visible 是否显示
 * @param onDismissRequest 关闭回调
 * @param modifier 外部 modifier
 * @param title 标题文字
 * @param position 弹窗位置，默认居中
 * @param buttonArrangement 底部按钮排列方式，默认跟随 position
 * @param hazeState 页面级 Haze 源；传入则启用亚克力背景
 * @param confirmButton 右侧/下方主操作按钮，默认 null
 * @param dismissButton 左侧/上方取消/次要按钮，默认 null
 * @param buttons 完全自定义底部按钮区；传入时覆盖 confirmButton/dismissButton 的默认布局
 * @param content 自定义内容
 */
@Composable
fun CloverDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    position: CloverDialogPosition = CloverDialogPosition.Center,
    buttonArrangement: CloverDialogButtonArrangement = CloverDialogButtonArrangement.Auto,
    hazeState: HazeState? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    buttons: @Composable ColumnScope.() -> Unit = {
        DefaultDialogButtons(
            arrangement = when (buttonArrangement) {
                CloverDialogButtonArrangement.Auto -> if (position == CloverDialogPosition.Center)
                    CloverDialogButtonArrangement.SideBySide
                else
                    CloverDialogButtonArrangement.StackedFill
                else -> buttonArrangement
            },
            confirmButton = confirmButton,
            dismissButton = dismissButton
        )
    },
    content: @Composable ColumnScope.() -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = visible

    var wasEverVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { if (visible) wasEverVisible = true }

    LaunchedEffect(visibleState.isIdle, visibleState.currentState, visibleState.targetState) {
        if (visibleState.isIdle && !visibleState.currentState && !visibleState.targetState && wasEverVisible) {
            wasEverVisible = false
            onDismissRequest()
        }
    }

    if (visible || visibleState.currentState) {
        BackHandler(enabled = visibleState.currentState || visibleState.targetState) {
            visibleState.targetState = false
        }

        CloverDialogPanel(
            modifier = modifier,
            visibleState = visibleState,
            position = position,
            hazeState = hazeState,
            title = title,
            buttons = buttons,
            content = content
        )
    }
}

/**
 * Clover Design 窗口级 Dialog（系统独立窗口）
 *
 * 使用系统 [Dialog] 窗口显示，不依赖调用方 root 容器大小，可自然覆盖全屏。
 * 视觉与行为与同层 [CloverDialog] 保持一致；返回键/点击遮罩会触发关闭动画，
 * 动画结束后回调 [onDismissRequest]。
 *
 * 注意：窗口级 Dialog 无法与祖先 [HazeState] 共享模糊源，因此 [hazeState] 参数
 * 在此无效，背景始终使用纯色。
 *
 * @param visible 是否显示
 * @param onDismissRequest 关闭回调
 * @param modifier 外部 modifier
 * @param title 标题文字
 * @param position 弹窗位置，默认居中
 * @param buttonArrangement 底部按钮排列方式，默认跟随 position
 * @param confirmButton 右侧/下方主操作按钮
 * @param dismissButton 左侧/上方取消/次要按钮
 * @param buttons 完全自定义底部按钮区
 * @param content 自定义内容
 */
@Composable
fun CloverWindowDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    position: CloverDialogPosition = CloverDialogPosition.Center,
    buttonArrangement: CloverDialogButtonArrangement = CloverDialogButtonArrangement.Auto,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    buttons: @Composable ColumnScope.() -> Unit = {
        DefaultDialogButtons(
            arrangement = when (buttonArrangement) {
                CloverDialogButtonArrangement.Auto -> if (position == CloverDialogPosition.Center)
                    CloverDialogButtonArrangement.SideBySide
                else
                    CloverDialogButtonArrangement.StackedFill
                else -> buttonArrangement
            },
            confirmButton = confirmButton,
            dismissButton = dismissButton
        )
    },
    content: @Composable ColumnScope.() -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = visible

    var wasEverVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { if (visible) wasEverVisible = true }

    LaunchedEffect(visibleState.isIdle, visibleState.currentState, visibleState.targetState) {
        if (visibleState.isIdle && !visibleState.currentState && !visibleState.targetState && wasEverVisible) {
            wasEverVisible = false
            onDismissRequest()
        }
    }

    if (visible || visibleState.currentState) {
        Dialog(
            onDismissRequest = { visibleState.targetState = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            CloverDialogPanel(
                modifier = modifier,
                visibleState = visibleState,
                position = position,
                hazeState = null,
                title = title,
                buttons = buttons,
                content = content
            )
        }
    }
}

/**
 * Dialog 面板内容（同层与窗口级共用）
 */
@Composable
private fun CloverDialogPanel(
    modifier: Modifier,
    visibleState: MutableTransitionState<Boolean>,
    position: CloverDialogPosition,
    hazeState: HazeState?,
    title: String?,
    buttons: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrimAlpha = if (isCloverDark()) CloverDialogDefaults.scrimAlphaDark
    else CloverDialogDefaults.scrimAlphaLight
    val scrimColor = LocalCloverColorScheme.current.scrim.copy(alpha = scrimAlpha)
    val surface = LocalCloverColorScheme.current.surface
    val onSurface = LocalCloverColorScheme.current.onSurface

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = when (position) {
            CloverDialogPosition.Center -> Alignment.Center
            CloverDialogPosition.Bottom -> Alignment.BottomCenter
        }
    ) {
        // 遮罩
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

        CompositionLocalProvider(
            LocalContentColor provides onSurface
        ) {
            val shape = RoundedCornerShape(CloverDialogDefaults.cornerRadius)

            val baseModifier = when (position) {
                CloverDialogPosition.Center -> Modifier
                    .widthIn(max = CloverDialogDefaults.maxWidth)
                    .padding(horizontal = CloverDialogDefaults.windowHorizontalPadding)
                    .clip(shape)

                CloverDialogPosition.Bottom -> Modifier
                    .fillMaxWidth()
                    .padding(
                        start = CloverDialogDefaults.bottomHorizontalPadding,
                        end = CloverDialogDefaults.bottomHorizontalPadding,
                        bottom = CloverDialogDefaults.bottomVerticalPadding +
                                WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues()
                                    .calculateBottomPadding()
                    )
                    .clip(shape)
            }

            val panelModifier = if (hazeState != null) {
                baseModifier
                    .cloverSolid(
                        backgroundColor = CloverMaterial.Solid.backgroundColor()
                    )
            } else {
                baseModifier.background(surface)
            }

            val enter = when (position) {
                CloverDialogPosition.Center -> fadeIn(animationSpec = tween(180)) +
                        scaleIn(
                            initialScale = 0.9f,
                            animationSpec = tween(250),
                        )

                CloverDialogPosition.Bottom -> slideInVertically(animationSpec = tween(300)) { it } +
                        fadeIn(animationSpec = tween(250))
            }

            val exit = when (position) {
                CloverDialogPosition.Center -> fadeOut(animationSpec = tween(150)) +
                        scaleOut(
                            targetScale = 0.95f,
                            animationSpec = tween(200),
                        )

                CloverDialogPosition.Bottom -> slideOutVertically(animationSpec = tween(250)) { it } +
                        fadeOut(animationSpec = tween(200))
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = enter,
                exit = exit,
            ) {
                Column(
                    modifier = panelModifier
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = {}
                        )
                        .padding(CloverDialogDefaults.contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!title.isNullOrEmpty()) {
                        Text(
                            text = title,
                            style = CloverTypography.titleBar,
                            color = onSurface,
                            modifier = Modifier.padding(bottom = CloverSpacing.md)
                        )
                    }

                    content()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = CloverSpacing.xl),
                        verticalArrangement = Arrangement.spacedBy(CloverDialogDefaults.buttonSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = buttons
                    )
                }
            }
        }
    }
}

/**
 * 默认 Dialog 底部按钮布局。
 */
@Composable
private fun ColumnScope.DefaultDialogButtons(
    arrangement: CloverDialogButtonArrangement,
    confirmButton: @Composable (() -> Unit)?,
    dismissButton: @Composable (() -> Unit)?
) {
    when (arrangement) {
        CloverDialogButtonArrangement.SideBySide -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    CloverDialogDefaults.buttonSpacing,
                    Alignment.End
                )
            ) {
                if (dismissButton != null) {
                    Box(modifier = Modifier.weight(1f)) { dismissButton() }
                }
                if (confirmButton != null) {
                    Box(modifier = Modifier.weight(1f)) { confirmButton() }
                }
            }
        }
        else -> {
            if (dismissButton != null) {
                Box(modifier = Modifier.fillMaxWidth()) { dismissButton() }
            }
            if (confirmButton != null) {
                Box(modifier = Modifier.fillMaxWidth()) { confirmButton() }
            }
        }
    }
}

/**
 * Clover Design 通用 Dialog 默认参数
 */
object CloverDialogDefaults {
    val maxWidth = 400.dp
    val cornerRadius = CloverSizes.dialogCornerRadius
    val windowHorizontalPadding = 24.dp
    val bottomHorizontalPadding = 12.dp
    val bottomVerticalPadding = 16.dp
    val contentPadding = PaddingValues(
        horizontal = CloverSizes.sheetHorizontalPadding,
        vertical = CloverSizes.sheetVerticalPadding
    )
    val buttonSpacing = 12.dp
    val scrimAlphaLight = 0.32f
    val scrimAlphaDark = 0.4f
    val blurRadius = 40.dp
}

/**
 * Dialog 确认按钮（Miuix 风格：实心主色按钮）
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 */
@Composable
fun CloverDialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    CloverButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        size = CloverButtonSize.Medium,
        colors = CloverButtonDefaults.filledColors()
    )
}

/**
 * Dialog 取消按钮（Miuix 风格：文字/描边按钮）
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param enabled 是否可用
 * @param outlined 是否使用描边样式；false 为纯文字样式
 */
@Composable
fun CloverDialogDismissButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false
) {
    val colors = if (outlined) CloverButtonDefaults.outlinedColors()
    else CloverButtonDefaults.textColors()
    CloverButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        size = CloverButtonSize.Medium,
        colors = colors
    )
}
