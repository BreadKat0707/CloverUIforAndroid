package cn.lemondrop.clover

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Clover Design 形状 Token
 *
 * 目前使用圆角矩形；后续可在这里接入 Squircle（连续曲率圆角）。
 */
object CloverShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(24.dp)
    val circle: Shape = CircleShape

    // 预留：Squircle 形状，后续可用 SDF / shader 实现
    // val squircle: Shape = SquircleShape(...)
}
