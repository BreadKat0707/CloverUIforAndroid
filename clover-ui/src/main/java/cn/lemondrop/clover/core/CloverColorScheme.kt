package cn.lemondrop.clover

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Clover Design 颜色方案。
 *
 * 与 Material3 [ColorScheme] 解耦，只保留 Clover 组件实际需要的 token。
 *
 * @param primary 主强调色（开关打开、选中、进度条、按钮背景）
 * @param onPrimary primary 上的文字/图标色
 * @param secondary 次强调色
 * @param onSecondary secondary 上的文字/图标色
 * @param background 页面背景
 * @param onBackground background 上的文字/图标色
 * @param surface 卡片/面板/列表项表面
 * @param onSurface surface 上的文字/图标色
 * @param surfaceVariant 次级表面、轨道、禁用背景
 * @param onSurfaceVariant surfaceVariant 上的文字/图标色
 * @param outline 边框、分隔线
 * @param shadow 阴影色
 * @param error 错误/危险
 * @param scrim 遮罩
 */
@Immutable
data class CloverColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val shadow: Color,
    val error: Color,
    val scrim: Color,
) {
    /**
     * 兼容旧叫法：accent 等价于 primary。
     */
    val accent: Color get() = primary
}

/**
 * 默认浅色方案。
 */
val CloverLightColorScheme = CloverColorScheme(
    primary = Color(0xFF869AFF),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF869AFF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFFF3F3F3),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0x1F000000),
    shadow = Color(0xFF000000),
    error = Color(0xFFB3261E),
    scrim = Color(0xFF000000),
)

/**
 * 默认深色方案。
 */
val CloverDarkColorScheme = CloverColorScheme(
    primary = Color(0xFF869AFF),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF869AFF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF2D2D2D),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0x1FFFFFFF),
    shadow = Color(0xFF000000),
    error = Color(0xFFF2B8B5),
    scrim = Color(0xFF000000),
)

/**
 * 当前作用域内的 Clover 颜色方案。
 */
val LocalCloverColorScheme = staticCompositionLocalOf { CloverLightColorScheme }

/**
 * 将 Material3 [ColorScheme] 映射为 [CloverColorScheme]。
 *
 * 注意：Material3 [ColorScheme] 没有 [shadow] token，这里固定使用黑色。
 */
private fun ColorScheme.toCloverColorScheme(): CloverColorScheme = CloverColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    shadow = Color.Black,
    error = error,
    scrim = scrim,
)

/**
 * 根据当前设置生成 [CloverColorScheme]。
 *
 * @param dynamicColor 是否使用系统 Material You 动态取色（Android 12+）
 * @param darkTheme 是否使用深色模式
 */
@Composable
fun rememberCloverColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean,
): CloverColorScheme {
    val context = LocalContext.current
    return androidx.compose.runtime.remember(dynamicColor, darkTheme) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val materialScheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            materialScheme.toCloverColorScheme()
        } else {
            if (darkTheme) CloverDarkColorScheme else CloverLightColorScheme
        }
    }
}
