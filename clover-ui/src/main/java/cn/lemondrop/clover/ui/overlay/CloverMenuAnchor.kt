package cn.lemondrop.clover

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * 锚点水平对齐方向。
 */
internal enum class AnchorHorizontalAlign {
    /** 面板左缘对齐锚点左缘 */
    Left,
    /** 面板右缘对齐锚点右缘 */
    Right
}

/**
 * 智能锚点定位结果。
 */
internal data class AnchoredPosition(
    val xPx: Float,
    val yPx: Float,
    val transformOrigin: TransformOrigin
)

/**
 * 根据锚点边界、面板尺寸和屏幕约束计算智能定位。
 *
 * 优先将面板放在锚点下方；空间不足时翻到上方。
 * 水平方向根据 [horizontalAlign] 将面板对齐锚点的左缘或右缘，
 * 并确保面板不超出屏幕边缘。
 *
 * @param anchorBounds 锚点在 root 坐标系下的边界
 * @param panelWidthPx 面板宽度（px）
 * @param panelHeightPx 面板当前高度（px），未测量时为 0
 * @param gapPx 面板与锚点之间的间距（px）
 * @param marginPx 面板与屏幕边缘的最小间距（px）
 * @param statusTopPx 系统状态栏高度（px）
 * @param navBottomPx 系统导航栏高度（px）
 * @param containerWidthPx 容器宽度（px）
 * @param containerHeightPx 容器高度（px）
 * @param horizontalAlign 水平对齐方式
 */
internal fun calculateAnchoredPosition(
    anchorBounds: Rect,
    panelWidthPx: Float,
    panelHeightPx: Int,
    gapPx: Float,
    marginPx: Float,
    statusTopPx: Float,
    navBottomPx: Float,
    containerWidthPx: Float,
    containerHeightPx: Float,
    horizontalAlign: AnchorHorizontalAlign
): AnchoredPosition {
    val fitsBelow = anchorBounds.bottom + gapPx + panelHeightPx <=
            containerHeightPx - navBottomPx - marginPx
    val yPx = if (fitsBelow) {
        anchorBounds.bottom + gapPx
    } else {
        (anchorBounds.top - gapPx - panelHeightPx).coerceAtLeast(statusTopPx + marginPx)
    }
    val maxX = (containerWidthPx - marginPx - panelWidthPx).coerceAtLeast(marginPx)
    val xPx = when (horizontalAlign) {
        AnchorHorizontalAlign.Left -> anchorBounds.left.coerceIn(marginPx, maxX)
        AnchorHorizontalAlign.Right -> (anchorBounds.right - panelWidthPx).coerceIn(marginPx, maxX)
    }
    val pivotX = if (panelWidthPx > 0f) {
        ((anchorBounds.center.x - xPx) / panelWidthPx).coerceIn(0f, 1f)
    } else {
        if (horizontalAlign == AnchorHorizontalAlign.Left) 0f else 1f
    }
    val transformOrigin = TransformOrigin(pivotX, if (fitsBelow) 0f else 1f)
    return AnchoredPosition(xPx, yPx, transformOrigin)
}

/**
 * 从 Dp 值批量计算 px 值的便捷工具。
 */
internal data class PxMetrics(
    val panelWidthPx: Float,
    val gapPx: Float,
    val marginPx: Float,
    val statusTopPx: Float,
    val navBottomPx: Float,
    val containerWidthPx: Float,
    val containerHeightPx: Float
)

internal fun computePxMetrics(
    panelWidth: Dp,
    gap: Dp,
    margin: Dp,
    statusTop: Dp,
    navBottom: Dp,
    containerWidthPx: Float,
    containerHeightPx: Float,
    density: Density
): PxMetrics = with(density) {
    PxMetrics(
        panelWidthPx = panelWidth.toPx(),
        gapPx = gap.toPx(),
        marginPx = margin.toPx(),
        statusTopPx = statusTop.toPx(),
        navBottomPx = navBottom.toPx(),
        containerWidthPx = containerWidthPx,
        containerHeightPx = containerHeightPx
    )
}
