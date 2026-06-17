package cn.lemondrop.clover

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint

/**
 * Clover Design Haze 效果封装
 *
 * 把 Haze 默认色调、降级策略集中管理，便于后续拆成独立模块。
 *
 * @param enabled 是否启用毛玻璃效果；低版本或省电模式可设为 false，自动退化为纯色背景
 */
object CloverHazeDefaults {

    /**
     * 判断当前设备是否支持 Haze 的 RenderEffect 实现
     */
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * 生成 Clover 风格的 HazeTint 组合
     *
     * @param baseColor 基础叠色
     * @param useLuminosity 是否加入 Luminosity 混合
     * @param useExclusion 是否加入 Exclusion 混合
     */
    fun tints(
        baseColor: Color,
        useLuminosity: Boolean = true,
        useExclusion: Boolean = true
    ): List<HazeTint> {
        return buildList {
            add(HazeTint(color = baseColor))
            if (useLuminosity) {
                add(
                    HazeTint(
                        color = Color.White.copy(alpha = 0.05f),
                        blendMode = BlendMode.Luminosity
                    )
                )
            }
            if (useExclusion) {
                add(
                    HazeTint(
                        color = Color.White.copy(alpha = 0.04f),
                        blendMode = BlendMode.Exclusion
                    )
                )
            }
        }
    }

    /**
     * 带默认透明度的表面叠色
     */
    @Composable
    fun surfaceTint(alpha: Float = 0.40f): Color {
        val isDark = isCloverDark()
        return (if (isDark) CloverColors.surfaceDark else CloverColors.surfaceLight)
            .copy(alpha = alpha)
    }
}

/**
 * 创建一个 Clover 风格的 HazeState
 */
@Composable
fun rememberCloverHazeState(): HazeState = dev.chrisbanes.haze.rememberHazeState()
