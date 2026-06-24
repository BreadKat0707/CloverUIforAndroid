package cn.lemondrop.clover

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * 壁纸加载策略
 */
enum class WallpaperLoadStrategy {
    Drawable,
    Peek,
    Fast
}

/**
 * 云母（Mica）背景材质
 *
 * 参考微软 Mica / Mica Alt：
 * - 以当前桌面壁纸为基底，配合高斯模糊呈现壁纸色调，而不暴露壁纸细节
 * - 叠加深/浅主题色微 tint（Mica Alt 更强）
 * - 覆盖一层低透明度噪点，模拟云母颗粒感
 * - 取不到壁纸时回退到主题 surface 色
 *
 * @param isAlt 是否使用 Mica Alt 变体
 * @param modifier 外部 modifier
 * @param content 内容
 */
@Composable
fun CloverMica(
    isAlt: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var wallpaper by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(context) {
        wallpaper = withContext(Dispatchers.IO) {
            loadWallpaperBitmap(context)
        }
    }

    val tintColor = if (isAlt) colorScheme.primary else colorScheme.secondary
    val tintAlpha = if (isAlt) 0.16f else 0.08f
    val noiseAlpha = if (isAlt) 0.10f else 0.06f
    val noiseBitmap = remember(isAlt) { createNoiseBitmap(size = 256, seed = if (isAlt) 2 else 1) }

    Box(modifier = modifier) {
        // 1. 模糊壁纸基底
        if (wallpaper != null) {
            Image(
                bitmap = wallpaper!!,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(120.dp),
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low
            )
        } else {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = colorScheme.surface)
            }
        }

        // 2. 主题色 tint + 噪点
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = tintColor.copy(alpha = tintAlpha))
            drawImage(
                image = noiseBitmap,
                dstSize = androidx.compose.ui.unit.IntSize(
                    size.width.toInt(),
                    size.height.toInt()
                ),
                alpha = noiseAlpha,
                filterQuality = FilterQuality.None,
                blendMode = BlendMode.Overlay
            )
        }

        content()
    }
}

/**
 * 云母 Alt（Mica Alt）变体
 *
 * 比默认 Mica 色调更浓、噪点更重，适合需要更强背景层次的页面。
 */
@Composable
fun CloverMicaAlt(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CloverMica(isAlt = true, modifier = modifier, content = content)
}

/**
 * 按指定策略获取当前桌面壁纸（未模糊）
 *
 * 返回 null 表示无法获取。
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
 * 综合获取壁纸：依次尝试 Drawable / Peek / Fast / 文件描述符
 */
private fun loadWallpaperBitmap(context: android.content.Context): ImageBitmap? {
    return loadWallpaperBitmap(context, WallpaperLoadStrategy.Drawable)
        ?: loadWallpaperBitmap(context, WallpaperLoadStrategy.Peek)
        ?: loadWallpaperBitmap(context, WallpaperLoadStrategy.Fast)
        ?: loadWallpaperFromFile(context)
}

private fun loadWallpaperBitmap(
    context: android.content.Context,
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

private fun loadWallpaperFromFile(context: android.content.Context): ImageBitmap? {
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
    for (x in 0 until size) {
        for (y in 0 until size) {
            val value = random.nextInt(256)
            bitmap.setPixel(x, y, Color(value, value, value, 255).toArgb())
        }
    }
    return bitmap.asImageBitmap()
}
