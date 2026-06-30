package cn.lemondrop.clover.material

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import cn.lemondrop.clover.LocalCloverColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * 壁纸加载策略。
 *
 * 用于 [CloverMica] / [Modifier.cloverMica] 取桌面壁纸，以及 [rememberWallpaperBitmap]。
 */
enum class WallpaperLoadStrategy {
    Drawable,
    Peek,
    Fast
}

/**
 * 为 [Modifier] 应用 Clover Mica（云母）材质背景。
 *
 * 参考微软 Mica / Mica Alt：
 * - 以当前桌面壁纸为基底，重度模糊后只保留色调、不暴露细节
 * - 叠加深/浅主题色微 tint（[isAlt] 变体更强）
 * - 覆盖一层低透明度噪点，模拟云母颗粒感
 * - 取不到壁纸时回退到 [CloverMaterial.Mica.fallbackColor]
 *
 * 与 [Modifier.cloverAcrylic] 对称：Mica 绘制在内容之下（[drawBehind]），
 * 模糊通过离屏降采样近似实现，因此不会模糊到内容层。
 *
 * @param isAlt 是否使用 Mica Alt 变体（tint 与噪点更强）
 * @param strategy 壁纸加载策略
 * @param shape 可选裁剪形状
 * @param tintColor 叠色，未指定时按 [isAlt] 取 primary / secondary
 * @param tintAlpha 叠色透明度
 * @param noiseAlpha 噪点透明度
 * @param blurRadius 壁纸模糊强度（离屏降采样近似）
 */
fun Modifier.cloverMica(
    isAlt: Boolean = false,
    strategy: WallpaperLoadStrategy = WallpaperLoadStrategy.Drawable,
    shape: Shape? = null,
    tintColor: Color? = null,
    tintAlpha: Float = if (isAlt) CloverMaterial.Mica.tintAlphaAlt else CloverMaterial.Mica.tintAlpha,
    noiseAlpha: Float = if (isAlt) CloverMaterial.Mica.noiseAlphaAlt else CloverMaterial.Mica.noiseAlpha,
    blurRadius: Dp = CloverMaterial.Mica.blurRadius,
): Modifier = composed {
    val resolvedTint = tintColor ?: CloverMaterial.Mica.tintColor(isAlt)
    val fallback = CloverMaterial.Mica.fallbackColor()
    val blurredWallpaper = rememberBlurredWallpaper(strategy, blurRadius)
    val noiseBitmap = remember(isAlt) {
        createNoiseBitmap(CloverMaterial.Mica.noiseTextureSize, seed = if (isAlt) 2 else 1)
    }

    this
        .then(if (shape != null) Modifier.clip(shape) else Modifier)
        .drawBehind {
            val fullSize = IntSize(size.width.toInt(), size.height.toInt())

            // 1. 模糊壁纸基底（取不到时回退底色）
            if (blurredWallpaper != null) {
                drawImage(
                    image = blurredWallpaper,
                    dstSize = fullSize,
                    filterQuality = FilterQuality.Low
                )
            } else {
                drawRect(color = fallback)
            }

            // 2. 主题色 tint
            drawRect(color = resolvedTint.copy(alpha = tintAlpha))

            // 3. 噪点（Overlay 混合，模拟云母颗粒）
            drawImage(
                image = noiseBitmap,
                dstSize = fullSize,
                alpha = noiseAlpha,
                filterQuality = FilterQuality.None,
                blendMode = BlendMode.Overlay
            )
        }
}

/**
 * Clover Mica 表面容器。
 *
 * 等价于对 [Box] 应用 [cloverMica]，方便直接包裹内容。与 [CloverAcrylicSurface] 对称。
 */
@Composable
fun CloverMicaSurface(
    modifier: Modifier = Modifier,
    isAlt: Boolean = false,
    strategy: WallpaperLoadStrategy = WallpaperLoadStrategy.Drawable,
    shape: Shape? = null,
    tintColor: Color? = null,
    tintAlpha: Float = if (isAlt) CloverMaterial.Mica.tintAlphaAlt else CloverMaterial.Mica.tintAlpha,
    noiseAlpha: Float = if (isAlt) CloverMaterial.Mica.noiseAlphaAlt else CloverMaterial.Mica.noiseAlpha,
    blurRadius: Dp = CloverMaterial.Mica.blurRadius,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.cloverMica(
            isAlt = isAlt,
            strategy = strategy,
            shape = shape,
            tintColor = tintColor,
            tintAlpha = tintAlpha,
            noiseAlpha = noiseAlpha,
            blurRadius = blurRadius
        )
    ) {
        content()
    }
}

/**
 * 按指定策略获取当前桌面壁纸（未模糊）。
 *
 * 返回 null 表示无法获取。常用于壁纸预览等场景；Mica 背景请直接使用 [cloverMica]。
 */
@Composable
fun rememberWallpaperBitmap(
    strategy: WallpaperLoadStrategy = WallpaperLoadStrategy.Drawable
): ImageBitmap? {
    val context = LocalContext.current
    var wallpaper by remember(context, strategy) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(context, strategy) {
        wallpaper = withContext(Dispatchers.IO) {
            loadWallpaperBitmap(context, strategy)
        }
    }

    return wallpaper
}

/**
 * 加载壁纸并降采样为「重度模糊」近似图，用于 Mica 背景。
 */
@Composable
private fun rememberBlurredWallpaper(
    strategy: WallpaperLoadStrategy,
    blurRadius: Dp
): ImageBitmap? {
    val context = LocalContext.current
    var result by remember(context, strategy, blurRadius) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(context, strategy, blurRadius) {
        result = withContext(Dispatchers.IO) {
            runCatching {
                val raw = loadWallpaperBitmap(context, strategy) ?: loadWallpaperBitmap(context)
                raw?.let { downscaleForBlur(it, blurRadius) }
            }.getOrNull()
        }
    }

    return result
}

/**
 * 以极小尺寸降采样再放大铺满，模拟重度高斯模糊（只保留色调、不暴露细节）。
 * 模糊半径越大，降采样越狠。
 */
private fun downscaleForBlur(source: ImageBitmap, blurRadius: Dp): ImageBitmap {
    val src = source.asAndroidBitmap()
    val maxDimension = (2400f / blurRadius.value.coerceAtLeast(1f)).toInt().coerceIn(16, 240)
    val width: Int
    val height: Int
    if (src.width >= src.height) {
        width = maxDimension
        height = (maxDimension.toFloat() * src.height / src.width).toInt().coerceAtLeast(1)
    } else {
        height = maxDimension
        width = (maxDimension.toFloat() * src.width / src.height).toInt().coerceAtLeast(1)
    }
    return Bitmap.createScaledBitmap(src, width, height, true).asImageBitmap()
}

/**
 * 综合获取壁纸：依次尝试 Drawable / Peek / Fast / 文件描述符。
 */
private fun loadWallpaperBitmap(context: Context): ImageBitmap? {
    return loadWallpaperBitmap(context, WallpaperLoadStrategy.Drawable)
        ?: loadWallpaperBitmap(context, WallpaperLoadStrategy.Peek)
        ?: loadWallpaperBitmap(context, WallpaperLoadStrategy.Fast)
        ?: loadWallpaperFromFile(context)
}

private fun loadWallpaperBitmap(
    context: Context,
    strategy: WallpaperLoadStrategy
): ImageBitmap? {
    val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
    val displayMetrics = context.applicationContext.resources.displayMetrics
    val width = displayMetrics.widthPixels.coerceAtLeast(1)
    val height = displayMetrics.heightPixels.coerceAtLeast(1)

    val drawable = try {
        when (strategy) {
            WallpaperLoadStrategy.Drawable -> wallpaperManager.drawable
            WallpaperLoadStrategy.Peek -> wallpaperManager.peekDrawable()
            WallpaperLoadStrategy.Fast -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.fastDrawable
                } else {
                    null
                }
            }
        }
    } catch (e: Exception) {
        null
    } ?: return null

    return try {
        drawable.toBitmap(width = width, height = height, config = Bitmap.Config.ARGB_8888)
            .asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

private fun loadWallpaperFromFile(context: Context): ImageBitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
    val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
    val displayMetrics = context.applicationContext.resources.displayMetrics
    val width = displayMetrics.widthPixels.coerceAtLeast(1)
    val height = displayMetrics.heightPixels.coerceAtLeast(1)

    return try {
        wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)?.use { pfd ->
            BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                ?.let { Bitmap.createScaledBitmap(it, width, height, true) }
                ?.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}

private fun createNoiseBitmap(size: Int, seed: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val random = Random(seed)
    // 围绕中灰的窄幅抖动：Overlay 混合下中灰(128)不改变底色，
    // 偏离越小颗粒越细腻；用窄幅替代全幅度黑白噪点，避免过于抢眼。
    val center = 128
    val amplitude = 18
    for (x in 0 until size) {
        for (y in 0 until size) {
            val value = (center + random.nextInt(-amplitude, amplitude + 1)).coerceIn(0, 255)
            bitmap.setPixel(x, y, Color(value, value, value, 255).toArgb())
        }
    }
    return bitmap.asImageBitmap()
}


/**
 * 系统壁纸实时 Mica（基于 windowShowWallpaper + Android 12 跨窗口模糊）。
 *
 * 与 [CloverMicaSurface]（自绘 bitmap）不同，这里直接让系统把桌面壁纸（含动态壁纸）
 * 合成到窗口之后，再用 [WindowManager.LayoutParams.FLAG_BLUR_BEHIND] 让系统模糊它，
 * 最后在上层叠加主题 tint + 噪点（+ 可选 scrim）凑出 Mica 质感。
 * 全程不读取壁纸像素，无需任何壁纸权限。
 *
 * **宿主前提（窗口级，组件内部无法可靠开启）**：承载本组件的 Activity 主题必须满足
 * `android:windowShowWallpaper=true` 且 `android:windowBackground` 为透明，否则壁纸不会透出。
 *
 * **限制**：
 * - `FLAG_BLUR_BEHIND` 模糊的是整个窗口之后的内容，故本组件适合作整页背景使用。
 * - 模糊是否真正生效取决于 [WindowManager.isCrossWindowBlurEnabled]（GPU / 省电模式 / 系统设置）。
 * - 拿不到壁纸像素，无法做封面取色，tint 用主题色近似。
 *
 * @param isAlt Mica Alt 变体（tint / 噪点更强）
 * @param enabled 为 false 时移除窗口模糊，仅保留叠层
 * @param blurRadius 壁纸模糊半径
 * @param tintColor 叠色，未指定时按 [isAlt] 取 primary / secondary
 * @param scrimColor 额外遮罩色（压暗壁纸、保证前景可读），默认透明
 */
@Composable
fun CloverWallpaperMica(
    modifier: Modifier = Modifier,
    isAlt: Boolean = false,
    enabled: Boolean = true,
    blurRadius: Dp = CloverMaterial.Mica.blurRadius,
    tintColor: Color? = null,
    tintAlpha: Float = if (isAlt) CloverMaterial.Mica.tintAlphaAlt else CloverMaterial.Mica.tintAlpha,
    noiseAlpha: Float = if (isAlt) CloverMaterial.Mica.noiseAlphaAlt else CloverMaterial.Mica.noiseAlpha,
    scrimColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val resolvedTint = tintColor ?: CloverMaterial.Mica.tintColor(isAlt)
    val noiseBitmap = remember(isAlt) {
        createNoiseBitmap(CloverMaterial.Mica.noiseTextureSize, seed = if (isAlt) 2 else 1)
    }
    val blurPx = with(density) { blurRadius.toPx() }.toInt()

    DisposableEffect(view, enabled, blurPx) {
        val window = (view.context as? Activity)?.window
        if (window != null && enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = blurPx }
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply { blurBehindRadius = 0 }
            }
        }
    }

    Box(
        modifier = modifier.drawBehind {
            val full = IntSize(size.width.toInt(), size.height.toInt())
            // 模糊后的壁纸由系统合成在窗口之后；此处叠加 scrim + tint + 噪点
            if (scrimColor != Color.Transparent) {
                drawRect(color = scrimColor)
            }
            drawRect(color = resolvedTint.copy(alpha = tintAlpha))
            drawImage(
                image = noiseBitmap,
                dstSize = full,
                alpha = noiseAlpha,
                filterQuality = FilterQuality.None,
                blendMode = BlendMode.Overlay
            )
        },
        content = content
    )
}
