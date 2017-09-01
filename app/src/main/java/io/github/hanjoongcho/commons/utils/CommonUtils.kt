package io.github.hanjoongcho.commons.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.TypedValue
import android.view.Display

import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.imaging.jpeg.JpegProcessingException
import com.drew.metadata.Metadata
import com.drew.metadata.exif.GpsDirectory

import java.io.File
import java.io.IOException

/**
 * Created by hanjoong on 2017-09-01.
 */

object CommonUtils {

    fun getDefaultDisplay(activity: Activity): Point {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    fun decodeFile(activity: Activity, imagePath: String?, options: BitmapFactory.Options?): Bitmap? {
        var bitmap: Bitmap? = null
        if (imagePath != null && File(imagePath).exists()) {
            if (options == null) {
                bitmap = BitmapFactory.decodeFile(imagePath)
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath, options)
            }
        } else {
            bitmap = BitmapFactory.decodeResource(activity.resources, android.R.drawable.ic_menu_gallery)
        }
        return bitmap
    }

    fun getGPSDirectory(filePath: String): GpsDirectory? {
        var gpsDirectory: GpsDirectory? = null
        try {
            val metadata = JpegMetadataReader.readMetadata(File(filePath))
            gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
        } catch (e: JpegProcessingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return gpsDirectory
    }

    fun dpToPixel(context: Context, dp: Int): Int {
        return dpToPixel(context, dp, 0)
    }

    fun dpToPixel(context: Context, dp: Int, policy: Int): Int {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)
        var pixel = 0
        when (policy) {
            0 -> pixel = px.toInt()
            1 -> pixel = Math.round(px)
        }
        return pixel
    }

}
