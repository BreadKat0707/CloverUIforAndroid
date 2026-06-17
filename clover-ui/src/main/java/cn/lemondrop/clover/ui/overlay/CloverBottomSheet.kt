package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import io.github.composefluent.component.Text

/**
 * Clover Design 底部 Sheet 容器
 *
 * 提供统一的全屏遮罩 + 底部圆角面板 + 导航栏避让。
 * 内容区由调用方自由填充，通常配合 [CloverMenuItem] 使用。
 *
 * @param onDismiss 点击遮罩关闭回调
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
    val navBarPadding = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()

    val basePanelModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = CloverBottomSheetDefaults.horizontalPadding,
            vertical = CloverBottomSheetDefaults.verticalPadding
        )
        .padding(bottom = navBarPadding)
        .clip(
            RoundedCornerShape(
                topStart = CloverBottomSheetDefaults.cornerRadius,
                topEnd = CloverBottomSheetDefaults.cornerRadius
            )
        )

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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) CloverColors.scrimDark.copy(alpha = 0.5f)
                    else CloverColors.scrimLight.copy(alpha = 0.4f)
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismiss
                )
        )

        // 面板
        Column(
            modifier = panelModifier
                .clickable(enabled = false) { /* 阻止点击穿透 */ }
        ) {
            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    style = CloverTypography.titleBar,
                    color = if (isDark) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight,
                    modifier = Modifier.padding(
                        horizontal = CloverBottomSheetDefaults.horizontalPadding,
                        vertical = CloverSpacing.md
                    )
                )
            }
            content()
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
}

/**
 * Clover Design Sheet 内菜单项
 *
 * 图标 + 文字一行，支持危险操作红色高亮。
 * 基于 [CloverBasicItem]。
 *
 * @param label 文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param icon 左侧图标
 * @param isDestructive 是否为危险操作（删除等）
 */
@Composable
fun CloverMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    val isDark = isCloverDark()
    val normalTitleColor = if (isDark) CloverColors.onSurfaceDark else CloverColors.onSurfaceLight
    val titleColor = if (isDestructive) {
        if (isDark) CloverColors.errorDark else CloverColors.errorLight
    } else normalTitleColor
    val iconColor = if (isDestructive) {
        if (isDark) CloverColors.errorDark else CloverColors.errorLight
    } else {
        if (isDark) CloverColors.onSurfaceVariantDark else CloverColors.onSurfaceVariantLight
    }

    CloverBasicItem(
        title = label,
        onClick = onClick,
        modifier = modifier,
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
            horizontal = CloverSizes.sheetHorizontalPadding,
            vertical = CloverSpacing.md
        ),
        indicationCornerRadius = 0.dp,
        colors = CloverBasicItemDefaults.colors(titleColor = titleColor)
    )
}
