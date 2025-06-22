package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.graphics.Matrix

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun compressImageFromUri(
    context: Context,
    imageUri: Uri,
    reqWidth: Int = 300,
    reqHeight: Int = 300,
    quality: Int = 80
): ByteArray? {
    return try {
        // Step 1: Decode bounds to compute sample size.
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        // Step 2: Apply downsampling.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        // Step 3: Decode and rotate the bitmap if needed.
        val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }?.let { bmp ->
            correctBitmapRotation(context, imageUri, bmp)
        }

        // Step 4: Compress and return byte array.
        bitmap?.let {
            ByteArrayOutputStream().use { baos ->
                it.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                baos.toByteArray()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun correctBitmapRotation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
    return try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val exif = inputStream?.use { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (angle != 0f) {
            val matrix = Matrix().apply { postRotate(angle) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        bitmap
    }
}