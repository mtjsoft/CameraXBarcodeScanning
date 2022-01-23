package cn.mtjsoft.barcodescanning.utils

import android.graphics.*
import android.media.Image
import android.util.Log
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat

/**
 * @author mtj
 * @date 2022/1/20
 * @desc
 * @email mtjsoft3@gmail.com
 */
object ScanUtil {

    /**
     * image.format to see if it is ImageFormat.YUV_420_888
     */
    fun Image.toBitmap(): Bitmap? {
        try {
            if (format != ImageFormat.YUV_420_888) {
                return null
            }
            val yBuffer = planes[0].buffer // Y
            val vuBuffer = planes[2].buffer // VU
            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()
            val nv21 = ByteArray(ySize + vuSize)
            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 90, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * 是否是弱光环境
     * 与黑色值进行比值，范围在 [0.8,1] 就认为是弱光环境，可以提示开启闪光灯
     */
    fun isLowLight(bitmap: Bitmap): Boolean {
        try {
            val color = getAverageColor(bitmap)
            if (color == Color.BLACK) {
                return true
            }
            val decimalFormat = DecimalFormat("0.00")
            val percent: String = decimalFormat.format(color.toFloat() / Color.BLACK)
            val floatPercent = percent.toFloat()
            return floatPercent >= 0.8
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * 获取位图RGB平均值
     */
    private fun getAverageColor(bitmap: Bitmap): Int {
        var redBucket = 0
        var greenBucket = 0
        var blueBucket = 0
        var pixelCount = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val c = bitmap.getPixel(x, y)
                pixelCount++
                redBucket += Color.red(c)
                greenBucket += Color.green(c)
                blueBucket += Color.blue(c)
            }
        }
        return Color.rgb(
            redBucket / pixelCount, greenBucket
                    / pixelCount, blueBucket / pixelCount
        )
    }
}