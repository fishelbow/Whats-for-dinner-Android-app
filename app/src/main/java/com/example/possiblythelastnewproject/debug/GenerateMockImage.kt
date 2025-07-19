package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.random.Random
import androidx.core.graphics.createBitmap

fun generateMockImage(context: Context, label: String): Uri {
    val safeName = label
        .replace(" ", "_")
        .replace(Regex("[^A-Za-z0-9_]"), "")
        .take(60)

    val filename = "gallery_${safeName}_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)

    val bitmap = createMockBitmap(label)

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out) // Slightly more compression
    }

    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private fun createMockBitmap(label: String): Bitmap {
    val width = 1080
    val height = 720

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    // 🎨 Background
    canvas.drawColor(generateBackgroundColor(label))

    // 🖋️ Text rendering
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    val lines = label.chunked(22)
    lines.forEachIndexed { i, line ->
        canvas.drawText(line, 40f, 100f + i * 60f, paint)
    }

    // 🌫️ White noise layer
    val rand = Random(label.hashCode())
    val noisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 255, 255, 255)
    }

    repeat(100) {
        canvas.drawCircle(
            rand.nextInt(width).toFloat(),
            rand.nextInt(height).toFloat(),
            rand.nextInt(15, 35).toFloat(),
            noisePaint
        )
    }

    return bitmap
}

private fun generateBackgroundColor(name: String): Int {
    val hue = abs(name.hashCode() % 360).toFloat()
    val hsv = floatArrayOf(hue, 0.5f, 0.9f)
    return Color.HSVToColor(hsv)
}