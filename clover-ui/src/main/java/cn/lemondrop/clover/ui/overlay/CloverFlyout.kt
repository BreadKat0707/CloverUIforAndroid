package cn.lemondrop.clover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp

/**
 * Clover Design Flyout 弹出菜单
 *
 * 从屏幕底部/角落弹出的小型菜单面板，通常由标题栏的“更多”按钮触发。
 * 包含遮罩、缩放进入动画，以及导航栏底部避让。
 *
 * @param visible 是否显示
 * @param onDismiss 关闭回调
 * @param modifier 外部 modifier
 * @param content 菜单内容，通常配合 [CloverMenuItem] 使用
 */
@Composable
fun CloverFlyout(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isCloverDark()
    val bgColor = LocalCloverColorScheme.current.surface

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) LocalCloverColorScheme.current.scrim.copy(alpha = 0.3f)
                    else LocalCloverColorScheme.current.scrim.copy(alpha = 0.3f)
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .fillMaxSize()
            .padding(end = 4.dp),
        enter = fadeIn() + scaleIn(
            initialScale = 0.85f,
            transformOrigin = TransformOrigin(1f, 1f)
        ),
        exit = fadeOut() + scaleOut(
            targetScale = 0.85f,
            transformOrigin = TransformOrigin(1f, 1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .width(210.dp)
                    .clip(RoundedCornerShape(CloverSizes.menuCornerRadius))
                    .background(bgColor)
                    .padding(vertical = 10.dp)
            ) {
                content()
            }
        }
    }
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
}
