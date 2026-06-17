package cn.lemondrop.clover

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Clover Design 主题辅助
 *
 * 目前先跟随系统，后续可以接入设置里的颜色模式。
 */
@Composable
fun isCloverDark(): Boolean = isSystemInDarkTheme()
