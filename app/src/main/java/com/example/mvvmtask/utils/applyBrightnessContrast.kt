package com.example.mvvmtask.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

fun applyBrightnessContrast(bitmap: Bitmap, brightness: Float, contrast: Float): Bitmap {
    val brightnessMatrix = ColorMatrix()
    brightnessMatrix.set(
        floatArrayOf(
            brightness, 0f, 0f, 0f, 0f,
            0f, brightness, 0f, 0f, 0f,
            0f, 0f, brightness, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    val contrastMatrix = ColorMatrix()
    contrastMatrix.set(
        floatArrayOf(
            contrast, 0f, 0f, 0f, 128f * (1 - contrast),
            0f, contrast, 0f, 0f, 128f * (1 - contrast),
            0f, 0f, contrast, 0f, 128f * (1 - contrast),
            0f, 0f, 0f, 1f, 0f
        )
    )

    brightnessMatrix.postConcat(contrastMatrix)

    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(brightnessMatrix)
    }

    val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = Canvas(resultBitmap)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return resultBitmap
}