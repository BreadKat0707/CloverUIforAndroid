package cn.lemondrop.clover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.Text

/**
 * Clover Design 下拉菜单容器
 *
 * @param expanded 是否展开
 * @param onDismissRequest 关闭回调
 * @param modifier 外部 modifier
 * @param offset 相对锚点的偏移
 * @param content 菜单内容
 */
@Composable
fun CloverDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = LocalCloverColorScheme.current.surface

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = containerColor,
            surfaceContainerHighest = containerColor,
            surfaceContainer = containerColor,
            surfaceContainerLow = containerColor,
            surfaceContainerLowest = containerColor,
            surfaceContainerHigh = containerColor
        )
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .background(Color.Transparent)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor),
            offset = offset,
            properties = androidx.compose.ui.window.PopupProperties(focusable = true),
            content = content
        )
    }
}

/**
 * Clover Design 下拉菜单项
 *
 * @param text 菜单文字
 * @param onClick 点击回调
 * @param modifier 外部 modifier
 * @param leadingIcon 左侧图标
 * @param trailingIcon 右侧图标
 * @param enabled 是否可用
 */
@Composable
fun CloverDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = { Text(text = text, style = CloverTypography.itemTitle) },
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = MenuDefaults.itemColors(
            textColor = LocalCloverColorScheme.current.onSurface,
            disabledTextColor = LocalCloverColorScheme.current.onSurfaceVariant
        )
    )
}
