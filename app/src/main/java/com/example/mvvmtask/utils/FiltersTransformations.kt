package com.example.mvvmtask.utils

import coil.size.Size
import coil.transform.Transformation
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

// Extension function to apply a ColorMatrix to a Bitmap
fun Bitmap.applyColorMatrix(colorMatrix: ColorMatrix): Bitmap {
    val result = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(this, 0f, 0f, paint)
    return result
}
class DefaultTransformation() : Transformation {
    override val cacheKey: String = "defaultTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // Return the original bitmap without any changes
        return input
    }
}
// GrayScale Filter
class GrayScaleTransformation : Transformation {
    override val cacheKey: String = "grayScaleTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        return input.applyColorMatrix(matrix)
    }
}

// Sepia Filter
class SepiaTransformation : Transformation {
    override val cacheKey: String = "sepiaTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply {
            val sepiaMatrix = ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            postConcat(sepiaMatrix)
        }
        return input.applyColorMatrix(matrix)
    }
}

// Invert Colors Filter
class InvertColorsTransformation : Transformation {
    override val cacheKey: String = "invertColorsTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return input.applyColorMatrix(matrix)
    }
}

// Black and White Filter
class BlackWhiteTransformation : Transformation {
    override val cacheKey: String = "blackWhiteTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        return input.applyColorMatrix(matrix)
    }
}

// Contrast Filter
class ContrastTransformation(private val contrast: Float = 1.5f) : Transformation {
    override val cacheKey: String = "contrastTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, 0f,
                    0f, contrast, 0f, 0f, 0f,
                    0f, 0f, contrast, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return input.applyColorMatrix(matrix)
    }
}

// Cool Filter
class CoolTransformation : Transformation {
    override val cacheKey: String = "coolTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    0.9f, 0f, 0.1f, 0f, 0f,
                    0f, 0.9f, 0.1f, 0f, 0f,
                    0.1f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return input.applyColorMatrix(matrix)
    }
}

// Brightness Filter
class BrightnessTransformation(private val brightness: Float = 1.2f) : Transformation {
    override val cacheKey: String = "brightnessTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    brightness, 0f, 0f, 0f, 0f,
                    0f, brightness, 0f, 0f, 0f,
                    0f, 0f, brightness, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        return input.applyColorMatrix(matrix)
    }
}

// Negative Filter
class NegativeTransformation : Transformation {
    override val cacheKey: String = "negativeTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return input.applyColorMatrix(matrix)
    }
}

// Saturation Filter
class SaturationTransformation(private val saturation: Float = 1.5f) : Transformation {
    override val cacheKey: String = "saturationTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix().apply { setSaturation(saturation) }
        return input.applyColorMatrix(matrix)
    }
}

// Vintage Filter
class VintageTransformation : Transformation {
    override val cacheKey: String = "vintageTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 0.7f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return input.applyColorMatrix(matrix)
    }
}

// Warmth Filter
class WarmthTransformation : Transformation {
    override val cacheKey: String = "warmthTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 0f,
                0f, 1.1f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return input.applyColorMatrix(matrix)
    }
}
