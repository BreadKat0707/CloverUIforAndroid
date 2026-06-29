package cn.lemondrop.clover

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Clover Design 设置项组件
 *
 * 基于 [CloverBasicItem]，用于设置页面的一行条目。
 *
 * @param title 标题
 * @param subtitle 副标题/说明
 * @param icon 左侧图标
 * @param trailing 右侧控件
 * @param enabled 是否可用
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 */
@Composable
fun CloverSettingItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val isDark = isCloverDark()
    val iconTint = LocalCloverColorScheme.current.onSurfaceVariant

    CloverBasicItem(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        leading = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(CloverSizes.iconMedium),
                    tint = iconTint
                )
            }
        },
        trailing = trailing,
        enabled = enabled,
        onClick = onClick
    )
}
