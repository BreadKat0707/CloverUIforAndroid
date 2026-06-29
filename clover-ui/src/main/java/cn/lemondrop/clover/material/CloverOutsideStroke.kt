package cn.lemondrop.clover.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 描边方向
 */
enum class CloverStrokeSide {
    Start, Top, End, Bottom;

    companion object {
        val ALL = setOf(Start, Top, End, Bottom)
    }
}

/**
 * 在内容外侧绘制一条描边。
 *
 * 与 `Modifier.border()` 不同，这里的描边画在内容外部，不会压缩内容本身。
 * 外层总尺寸会等于内容尺寸加上指定方向上的描边宽度。
 *
 * @param width 描边宽度
 * @param color 描边颜色
 * @param sides 需要描边的方向，默认四边
 * @param shape 裁剪形状，默认矩形
 * @param modifier 外部 modifier
 * @param content 内容
 */
@Composable
fun CloverOutsideStroke(
    width: Dp,
    color: Color,
    sides: Set<CloverStrokeSide> = CloverStrokeSide.ALL,
    shape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val start = if (CloverStrokeSide.Start in sides) width else 0.dp
    val top = if (CloverStrokeSide.Top in sides) width else 0.dp
    val end = if (CloverStrokeSide.End in sides) width else 0.dp
    val bottom = if (CloverStrokeSide.Bottom in sides) width else 0.dp

    Box(modifier = modifier) {
        // 描边层：占据外圈
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = start, top = top, end = end, bottom = bottom)
                .background(color = color, shape = shape)
        )
        // 内容层：内缩后覆盖中间区域
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = start, top = top, end = end, bottom = bottom),
            content = { content() }
        )
    }
}

/**
 *  convenience：只在单侧外侧描边。
 */
@Composable
fun CloverOutsideStroke(
    width: Dp,
    color: Color,
    side: CloverStrokeSide,
    shape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = CloverOutsideStroke(
    width = width,
    color = color,
    sides = setOf(side),
    shape = shape,
    modifier = modifier,
    content = content
)
