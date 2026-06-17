package cn.lemondrop.clover

import androidx.compose.ui.graphics.Color

/**
 * Clover Design 颜色 Token
 *
 * 不直接依赖 Material3，所有组件从这里取色。
 * 后续可扩展为根据主题/设置动态生成。
 */
object CloverColors {
    // 背景
    val backgroundLight = Color(0xFFF3F3F3)
    val backgroundDark = Color(0xFF1A1A1A)

    // 表面层（卡片、列表项、面板）
    val surfaceLight = Color(0xFFFFFFFF)
    val surfaceDark = Color(0xFF2D2D2D)
    val surfaceVariantLight = Color(0xFFECECEC)
    val surfaceVariantDark = Color(0xFF3A3A3A)

    // 主强调色
    val accent = Color(0xFF7C4DFF)
    val onAccentLight = Color(0xFFFFFFFF)
    val onAccentDark = Color(0xFFFFFFFF)

    // 错误/危险
    val errorLight = Color(0xFFB3261E)
    val errorDark = Color(0xFFF2B8B5)

    // 文字
    val onSurfaceLight = Color(0xFF1A1A1A)
    val onSurfaceDark = Color(0xFFFFFFFF)
    val onSurfaceVariantLight = Color(0xFF666666)
    val onSurfaceVariantDark = Color(0xFFB0B0B0)

    // 遮罩
    val scrimLight = Color(0xFF000000)
    val scrimDark = Color(0xFF000000)

    // 选中/播放态
    val playingIndicator = accent

    // 分隔线
    val dividerLight = Color(0x1F000000)
    val dividerDark = Color(0x1FFFFFFF)
}
