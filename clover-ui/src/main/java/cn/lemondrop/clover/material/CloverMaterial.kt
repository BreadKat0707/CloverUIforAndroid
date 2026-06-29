package cn.lemondrop.clover.material

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.lemondrop.clover.LocalCloverColorScheme
import cn.lemondrop.clover.isCloverDark
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Clover Design Material 材质系统
 *
 * 目前仅实现 Acrylic（亚克力），后续会加入 Mica。
 */
object CloverMaterial {

    /**
     * Acrylic（亚克力）材质参数
     *
     * - 对底层内容进行高斯模糊
     * - 叠加一层带透明度的主题色 tint
     * - 加入细微噪声，模拟磨砂塑料质感
     */
    object Acrylic {
        val blurRadius: Dp = 40.dp
        val noiseFactor: Float = 0.1f
        const val tintAlpha: Float = 0.70f

        /**
         * 默认 fallback 背景色，用于 Haze 不可用时显示为纯色。
         * 深色模式偏黑，浅色模式偏白。
         */
        @Composable
        fun backgroundColor(
            fallback: Color = LocalCloverColorScheme.current.surface
        ): Color {
            return if (isCloverDark()) {
                Color.Black.copy(alpha = 0.50f)
            } else {
                Color.White.copy(alpha = 0.65f)
            }
        }

        /**
         * 生成 Acrylic 风格的 HazeTint 组合。
         *
         * @param baseColor 基础叠色，未指定时使用当前 surface 色
         */
        @Composable
        fun tints(baseColor: Color = LocalCloverColorScheme.current.surface): List<HazeTint> {
            return listOf(HazeTint(baseColor.copy(alpha = tintAlpha)))
        }
    }

    /**
     * Mica（云母）材质参数，后续实现。
     */
    object Mica {
        // TODO: 实现 Mica 效果
    }
}

/**
 * 将 Modifier 应用 Clover Acrylic 效果。
 *
 * 需要在某个祖先节点上调用 [hazeSource] 提供 [HazeState]。
 *
 * @param state Haze 状态
 * @param shape 可选裁剪形状
 * @param backgroundColor Haze 失效时的 fallback 背景色
 * @param tints 叠色层
 * @param blurRadius 模糊半径
 * @param noiseFactor 噪声强度
 */
fun Modifier.cloverAcrylic(
    state: HazeState,
    shape: Shape? = null,
    backgroundColor: Color? = null,
    tints: List<HazeTint>? = null,
    blurRadius: Dp = CloverMaterial.Acrylic.blurRadius,
    noiseFactor: Float = CloverMaterial.Acrylic.noiseFactor
): Modifier = composed {
    val resolvedBackgroundColor = backgroundColor ?: CloverMaterial.Acrylic.backgroundColor()
    val resolvedTints = tints ?: CloverMaterial.Acrylic.tints()
    this.then(
        if (shape != null) Modifier.clip(shape) else Modifier
    ).hazeEffect(state = state) {
        this.backgroundColor = resolvedBackgroundColor
        this.tints = resolvedTints
        this.blurRadius = blurRadius
        this.noiseFactor = noiseFactor
    }
}

/**
 * Clover Acrylic 表面容器。
 *
 * 等价于对 Box 应用 [cloverAcrylic]，方便直接包裹内容。
 */
@Composable
fun CloverAcrylicSurface(
    state: HazeState,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    backgroundColor: Color = CloverMaterial.Acrylic.backgroundColor(),
    tints: List<HazeTint> = CloverMaterial.Acrylic.tints(),
    blurRadius: Dp = CloverMaterial.Acrylic.blurRadius,
    noiseFactor: Float = CloverMaterial.Acrylic.noiseFactor,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.cloverAcrylic(
            state = state,
            shape = shape,
            backgroundColor = backgroundColor,
            tints = tints,
            blurRadius = blurRadius,
            noiseFactor = noiseFactor
        )
    ) {
        content()
    }
}
