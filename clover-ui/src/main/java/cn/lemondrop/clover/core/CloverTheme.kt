package cn.lemondrop.clover

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Clover Design 主题
 *
 * 提供局部的深色/亮色模式覆盖。
 * 调用方可以用 [CloverTheme] 包裹局部 UI，让该范围内的组件跟随 [darkTheme]，
 * 而不影响应用其它部分。
 */
object CloverTheme {
    /**
     * 当前是否处于 Clover 深色模式。
     */
    val isDark: Boolean
        @Composable
        get() = isCloverDark()
}

/**
 * 局部深色模式开关。
 *
 * - `true`：强制深色
 * - `false`：强制亮色
 * - `null`：跟随系统（默认）
 */
val LocalCloverDarkMode = staticCompositionLocalOf<Boolean?> { null }

/**
 * 读取当前 Clover 深色模式。
 *
 * 优先使用 [LocalCloverDarkMode] 的局部覆盖值，没有覆盖则跟随系统。
 */
@Composable
fun isCloverDark(): Boolean {
    return LocalCloverDarkMode.current ?: isSystemInDarkTheme()
}

/**
 * Clover 主题包裹器。
 *
 * @param darkTheme 是否使用深色模式
 * @param content 主题作用域内的内容
 */
@Composable
fun CloverTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCloverDarkMode provides darkTheme) {
        content()
    }
}
