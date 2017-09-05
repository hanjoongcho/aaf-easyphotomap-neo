package io.github.hanjoongcho.commons.utils

import android.app.Activity
import android.graphics.*
import android.util.LruCache
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by hanjoong on 2017-09-01.
 */
object BitmapUtils {

    private @Volatile var memoryCache: LruCache<String, Bitmap>? = null

    init {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) memoryCache?.put(key, bitmap)
    }

    fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache?.get(key)
    }

    fun createScaledBitmap(srcPath: String, destPath: String, fixedWidthHeight: Int): Boolean {
        var result = true
        var outputStream: OutputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 20
            val bitmap = BitmapFactory.decodeFile(srcPath, options)
            val height = bitmap.height
            val width = bitmap.width
            val downSampleHeight = height / width.toFloat() * fixedWidthHeight
            val downSampleWidth = width / height.toFloat() * fixedWidthHeight
            var thumbNail: Bitmap? = null
            thumbNail = when (width > height) {
                true -> Bitmap.createScaledBitmap(bitmap, fixedWidthHeight, downSampleHeight.toInt(), false)
                false -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedWidthHeight, false)
            }
            outputStream = FileOutputStream(destPath)
            thumbNail!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            result = false
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return result
    }

    fun createScaledBitmap(bitmap: Bitmap, point: Point, scaleFactorX: Float, scaleFactorY: Float): Bitmap? {
        var downscaledBitmap: Bitmap? = null
        val fixedWidth = point.x * scaleFactorX
        val fixedHeight = point.y * scaleFactorY
        try {
            val height = bitmap.height
            val width = bitmap.width
            val downSampleWidth = width / height.toFloat() * fixedHeight
            val downSampleHeight = height / width.toFloat() * fixedWidth
            downscaledBitmap = when {
                // 가로이미지 & 세로보기 화면에서는 width값에 맞춰 고정함
                (width > height && point.x < point.y) -> Bitmap.createScaledBitmap(bitmap, fixedWidth.toInt(), downSampleHeight.toInt(), false)
                // 가로이미지 & 가로보기 화면에서는 height값에 맞춰 고정함
                (width > height && point.x > point.y) -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
                (width < height) -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
                (width == height) -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
                else -> { bitmap }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return downscaledBitmap
    }

    fun addWhiteBorder(bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(bmp.width + borderSize * 2, bmp.height + borderSize * 2, bmp.config)
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    fun decodeFile(activity: Activity, imagePath: String): Bitmap? {
        return decodeFile(activity, imagePath, null)
    }

    fun decodeFile(activity: Activity, imagePath: String, options: BitmapFactory.Options?): Bitmap? {
        return when (imagePath != null && File(imagePath).exists()) {
            true -> {
                if (options == null) {
                    BitmapFactory.decodeFile(imagePath)
                } else {
                    BitmapFactory.decodeFile(imagePath, options)
                }
            }
            false -> BitmapFactory.decodeResource(activity.resources, android.R.drawable.ic_menu_gallery)
        }
    }
}
