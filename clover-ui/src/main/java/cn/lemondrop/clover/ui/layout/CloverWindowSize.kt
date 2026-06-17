package cn.lemondrop.clover

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 获取当前窗口宽度尺寸类
 *
 * - Compact：竖屏手机，使用底部布局
 * - Medium / Expanded：小平板、折叠屏展开、大平板、桌面，使用侧边/顶部布局
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun cloverWindowWidthSizeClass(): WindowWidthSizeClass {
    return calculateWindowSizeClass(LocalContext.current as Activity).widthSizeClass
}

/**
 * 判断当前是否为紧凑宽度（手机竖屏）
 */
@Composable
fun cloverIsCompactWidth(): Boolean = cloverWindowWidthSizeClass() == WindowWidthSizeClass.Compact
