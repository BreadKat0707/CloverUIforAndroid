package cn.lemondrop.clover

import androidx.compose.ui.unit.dp

/**
 * Clover Design 尺寸 / 圆角 / 间距 Token
 */
object CloverSizes {
    // 列表项
    val listItemHeight = 56.dp
    val listItemHeightLarge = 64.dp
    val listItemHorizontalPadding = 4.dp
    val listItemVerticalPadding = 8.dp
    val listItemCornerRadius = 12.dp

    // 列表容器到屏幕边缘的边距
    val listOuterHorizontalPadding = 16.dp

    // 封面
    val coverSmall = 48.dp
    val coverMedium = 56.dp
    val coverCornerRadius = 8.dp
    val avatarCornerRadius = 24.dp

    // 图标
    val iconSmall = 20.dp
    val iconMedium = 24.dp

    // 底部栏 / 顶部栏 / 侧边栏
    val titleBarHeight = 52.dp
    val bottomNavHeight = 64.dp
    val topAppBarHeight = 64.dp
    val navigationRailWidth = 80.dp
    val navigationViewWidth = 280.dp

    // 弹窗/菜单
    val sheetCornerRadius = 24.dp
    val menuCornerRadius = 20.dp
    val dialogCornerRadius = 24.dp
    val sheetHorizontalPadding = 12.dp
    val sheetVerticalPadding = 24.dp

    // 按钮
    val buttonHeight = 40.dp
    val iconButtonSize = 40.dp
}

object CloverSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object CloverElevation {
    // 占位：Compose 阴影用 dp，可根据需要扩展
    val none = 0.dp
    val sm = 2.dp
    val md = 6.dp
    val lg = 12.dp
}
