package cn.lemondrop.clover.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect

/**
 * Clover Design Material 材质系统
 *
 * 包含 Acrylic（亚克力）、Solid（纯色）与 Mica（云母）三种材质参数。
 * Acrylic 见 [Modifier.cloverAcrylic]，Solid 见 [Modifier.cloverSolid]，Mica 见 [Modifier.cloverMica]。
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
         * 生成 Acrylic 风格的 HazeColorEffect 组合。
         *
         * @param baseColor 基础叠色，未指定时使用当前 surface 色
         */
        @Composable
        fun tints(baseColor: Color = LocalCloverColorScheme.current.surface): List<HazeColorEffect> {
            return listOf(HazeColorEffect.tint(baseColor.copy(alpha = tintAlpha)))
        }
    }

    /**
     * Solid（纯色）材质参数。
     *
     * 当 Haze 模糊不可用时使用的不透明/半透明纯色背景。
     */
    object Solid {
        /** 默认纯色背景，跟随主题 surface。 */
        @Composable
        fun backgroundColor(): Color = LocalCloverColorScheme.current.surface
    }

    /**
     * Mica（云母）材质参数。
     *
     * - 以当前桌面壁纸为基底，重度模糊后只保留色调、不暴露细节
     * - 叠加深/浅主题色微 tint（Alt 变体更强）
     * - 覆盖一层低透明度噪点，模拟云母颗粒感
     * - 取不到壁纸时回退到主题 surface 色
     *
     * 具体绘制见 [Modifier.cloverMica] 与 [CloverMicaSurface]。
     */
    object Mica {
        /** 壁纸模糊强度（通过离屏降采样近似，避免模糊到内容层）。 */
        val blurRadius: Dp = 120.dp

        /** 噪点纹理边长（像素）。 */
        const val noiseTextureSize: Int = 256

        /** 默认变体 tint 透明度。 */
        const val tintAlpha: Float = 0.08f

        /** Alt 变体 tint 透明度。 */
        const val tintAlphaAlt: Float = 0.16f

        /** 默认变体噪点透明度。 */
        const val noiseAlpha: Float = 0.04f

        /** Alt 变体噪点透明度。 */
        const val noiseAlphaAlt: Float = 0.06f

        /**
         * 当前变体的 tint 颜色：默认取 secondary，Alt 取 primary。
         */
        @Composable
        fun tintColor(isAlt: Boolean): Color {
            val scheme = LocalCloverColorScheme.current
            return if (isAlt) scheme.primary else scheme.secondary
        }

        /** 取不到壁纸时的回退底色。 */
        @Composable
        fun fallbackColor(): Color = LocalCloverColorScheme.current.surface
    }
}

/**
 * 将 Modifier 应用 Clover Solid（纯色）背景。
 *
 * 不带模糊，仅用于需要纯色的场景。后续可替换为 [cloverAcrylic] 恢复模糊。
 *
 * @param shape 可选裁剪形状
 * @param backgroundColor 背景色，默认主题 surface
 */
fun Modifier.cloverSolid(
    shape: Shape? = null,
    backgroundColor: Color? = null
): Modifier = composed {
    val resolvedBackgroundColor = backgroundColor ?: CloverMaterial.Solid.backgroundColor()
    val base = if (shape != null) Modifier.clip(shape) else Modifier
    this
        .then(base)
        .background(resolvedBackgroundColor)
}

/**
 * Clover Solid 表面容器。
 *
 * 不带模糊，仅绘制纯色背景。
 *
 * @param modifier 外部 modifier
 * @param shape 可选裁剪形状
 * @param backgroundColor 背景色，默认主题 surface
 * @param content 内容
 */
@Composable
fun CloverSolidSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    backgroundColor: Color = CloverMaterial.Solid.backgroundColor(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.cloverSolid(shape = shape, backgroundColor = backgroundColor)
    ) {
        content()
    }
}

/**
 * 将 Modifier 应用 Clover Acrylic 效果。
 *
 * 需要在某个祖先节点上调用 [hazeSource] 提供 [HazeState]。
 *
 * @param state Haze 状态
 * @param shape 可选裁剪形状
 * @param backgroundColor 绘制在模糊内容后方的底色
 * @param tints 叠色层
 * @param blurRadius 模糊半径
 * @param noiseFactor 噪声强度
 */
fun Modifier.cloverAcrylic(
    state: HazeState,
    shape: Shape? = null,
    backgroundColor: Color? = null,
    tints: List<HazeColorEffect>? = null,
    blurRadius: Dp = CloverMaterial.Acrylic.blurRadius,
    noiseFactor: Float = CloverMaterial.Acrylic.noiseFactor
): Modifier = composed {
    val resolvedBackgroundColor = backgroundColor ?: CloverMaterial.Acrylic.backgroundColor()
    val resolvedTints = tints ?: CloverMaterial.Acrylic.tints()

    val style = remember(resolvedBackgroundColor, resolvedTints, blurRadius, noiseFactor) {
        HazeBlurStyle(
            backgroundColor = resolvedBackgroundColor,
            colorEffects = resolvedTints,
            blurRadius = blurRadius,
            noiseFactor = noiseFactor
        )
    }

    val base = if (shape != null) Modifier.clip(shape) else Modifier
    this
        .then(base)
        .hazeEffect(state = state) {
            blurEffect { this.style = style }
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
    tints: List<HazeColorEffect> = CloverMaterial.Acrylic.tints(),
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
