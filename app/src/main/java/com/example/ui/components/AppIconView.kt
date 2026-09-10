package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val iconBitmapCache = object : LruCache<Int, Bitmap>(150) {}

@Composable
fun AppIconView(
    drawable: Drawable?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val bitmap = remember(drawable) {
        if (drawable == null) null
        else {
            val key = System.identityHashCode(drawable)
            val cached = iconBitmapCache.get(key)
            if (cached != null) {
                cached
            } else {
                try {
                    val bmp = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                        drawable.bitmap
                    } else {
                        val width = (drawable.intrinsicWidth.takeIf { it > 0 } ?: 96).coerceIn(48, 192)
                        val height = (drawable.intrinsicHeight.takeIf { it > 0 } ?: 96).coerceIn(48, 192)
                        val createdBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(createdBmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        createdBmp
                    }
                    if (bmp != null) {
                        iconBitmapCache.put(key, bmp)
                    }
                    bmp
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
