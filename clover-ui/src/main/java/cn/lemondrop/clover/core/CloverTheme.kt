package cn.lemondrop.clover

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Clover Design 主题。
 *
 * 提供局部的深色/亮色模式覆盖以及颜色方案。
 * 调用方可以用 [CloverTheme] 包裹局部 UI，让该范围内的组件跟随 [darkTheme] 和 [colorScheme]，
 * 而不影响应用其它部分。
 */
object CloverTheme {
    /**
     * 当前是否处于 Clover 深色模式。
     */
    val isDark: Boolean
        @Composable
        get() = isCloverDark()

    /**
     * 当前作用域内的颜色方案。
     */
    val colorScheme: CloverColorScheme
        @Composable
        get() = LocalCloverColorScheme.current
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
 * @param colorScheme 颜色方案，默认根据 [dynamicColor] 与 [darkTheme] 生成
 * @param dynamicColor 是否使用系统 Material You 动态取色（Android 12+）
 * @param darkTheme 是否使用深色模式
 * @param content 主题作用域内的内容
 */
@Composable
fun CloverTheme(
    colorScheme: CloverColorScheme = rememberCloverColorScheme(
        dynamicColor = false,
        darkTheme = isSystemInDarkTheme()
    ),
    dynamicColor: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = rememberCloverColorScheme(dynamicColor = dynamicColor, darkTheme = darkTheme)
    CompositionLocalProvider(
        LocalCloverColorScheme provides scheme,
        LocalCloverDarkMode provides darkTheme
    ) {
        content()
    }
}

/**
 * Clover 主题包裹器（只切换深浅，使用默认静态配色）。
 */
@Composable
fun CloverTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCloverColorScheme provides rememberCloverColorScheme(
            dynamicColor = false,
            darkTheme = darkTheme
        ),
        LocalCloverDarkMode provides darkTheme
    ) {
        content()
    }
}
