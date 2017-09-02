package io.github.hanjoongcho.easyphotomap.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import io.github.hanjoongcho.commons.utils.CommonUtils
import io.github.hanjoongcho.commons.utils.PreferenceUtils
import io.github.hanjoongcho.easyphotomap.Constants
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.adapters.ExplorerItemAdapter
import io.github.hanjoongcho.easyphotomap.models.FileExplorerItem
import io.github.hanjoongcho.easyphotomap.models.PhotoMapItem
import kotlinx.android.synthetic.main.activity_file_explorer.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.util.*

/**
 * Created by hanjoong on 2017-09-01.
 */
class FileExplorerActivity : AppCompatActivity() {

    private var currentPath: String? = null
    private var listFileExplorerFile: ArrayList<FileExplorerItem>? = null
    private var listFileExplorerDirectory: ArrayList<FileExplorerItem>? = null
    private var fileExplorerAdapter: ArrayAdapter<FileExplorerItem>? = null
    private val previous: FileExplorerItem = FileExplorerItem()
    init {
        previous.isDirectory = true
        previous.fileName = ".."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        currentPath = Environment.getExternalStorageDirectory().absolutePath + "/DCIM";
        listFileExplorerFile = arrayListOf<FileExplorerItem>()
        listFileExplorerDirectory = arrayListOf<FileExplorerItem>()
        fileExplorerAdapter = ExplorerItemAdapter(this, this, R.layout.item_file_explorer, listFileExplorerFile as ArrayList<FileExplorerItem>);
        fileListView.adapter = fileExplorerAdapter
        fileListView.setOnItemClickListener { adapterView, _, position, _ ->
            val item = adapterView.adapter.getItem(position) as FileExplorerItem
            var fileName = item.fileName as String
            if (fileName.startsWith("[") && fileName.endsWith("]")) {
                fileName = fileName.substring(1, fileName.length - 1)
            }
            val path = currentPath + "/" + fileName
            val file = File(path)
            if (file.isDirectory) {
                currentPath = if (fileName == "..") currentPath?.substring(0, currentPath!!.lastIndexOf("/")) else path
                this@FileExplorerActivity.refreshList()
            } else {
                // TODO register photo map
            }
        }
        finishButton.setOnClickListener{ finish() }
        refreshList()
    }

    private fun refreshList() {

        val paths = (currentPath?.split("/") as List<String?>).filter { it -> !it.isNullOrEmpty() }
        navigation?.removeViews(0, navigation.childCount)
        var tempPath = ""

        for ((index, value) in paths.withIndex()) {
            tempPath += "/" + value
            val targetPath = tempPath
            val textView = TextView(this@FileExplorerActivity)
            if (index < paths.size - 1) {
                textView.text = value + "  >  "
            } else {
                textView.text = value
            }

            if (paths[paths.size - 1].equals(value)) {
                textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.color_primary))
            } else {
                textView.typeface = Typeface.DEFAULT
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.file_explorer_text))
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16F)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setOnClickListener {
                currentPath = targetPath
                this@FileExplorerActivity.refreshList()
            }
            navigation?.addView(textView)
        }
        navigationContainer.postDelayed(Runnable { navigationContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
        RefreshThread().start()
    }

    internal inner class RefreshThread : Thread() {

        override fun run() {
            listFileExplorerFile?.clear()
            listFileExplorerDirectory?.clear()
            val fileNames: Array<String>? = File(currentPath).list()
            fileNames?.let {
                for (fileName in fileNames) {
                    val explorerFile = FileExplorerItem()
                    val path = currentPath + "/" + fileName
                    var name = ""
                    val file = File(path)
                    if (file.isDirectory) {
                        name = "[$fileName]"
                        explorerFile.setImagePathAndFileName(name)
                        explorerFile.isDirectory = true
                        listFileExplorerDirectory?.add(explorerFile)
                    } else {
                        name = fileName
                        val extension = FilenameUtils.getExtension(name).toLowerCase()
                        if (!extension.matches("jpg|jpeg".toRegex())) continue
                        explorerFile.setImagePathAndFileName(path)
                        listFileExplorerFile?.add(explorerFile)
                    }
                }
            }

            if (PreferenceUtils.loadBooleanPreference(this@FileExplorerActivity, "enable_reverse_order")) {
                Collections.sort(listFileExplorerDirectory, Collections.reverseOrder<Any>())
                Collections.sort(listFileExplorerFile, Collections.reverseOrder<Any>())
            } else {
                Collections.sort(listFileExplorerDirectory)
                Collections.sort(listFileExplorerFile)
            }
            listFileExplorerFile?.addAll(0, listFileExplorerDirectory as Collection<FileExplorerItem>)
            if (StringUtils.split(currentPath, "/").size > 1) listFileExplorerFile?.add(0, previous)
            android.os.Handler(Looper.getMainLooper()).post { fileExplorerAdapter?.notifyDataSetInvalidated() }
        }
    }

    fun registerPhotoMap (context: Context, fileName: String?, path: String?) {
        if (fileName != null && path != null) {
            val registerThread = RegisterThread(context, fileName, path)
            registerThread.start()
        }
    }

    inner class RegisterThread(var context: Context, var fileName: String, var path: String) : Thread() {

        private fun registerSingleFile() {
            try {

                var targetFile: File? = null
                if (PreferenceUtils.loadBooleanPreference(this@FileExplorerActivity, "enable_create_copy")) {
                    targetFile = File(Constants.WORKING_DIRECTORY + fileName)
                    if (!targetFile!!.exists()) {
                        FileUtils.copyFile(File(path), targetFile)
                    }
                } else {
                    targetFile = File(path)
                    fileName = FilenameUtils.getBaseName(fileName)
                }

                val metadata = JpegMetadataReader.readMetadata(targetFile)
                val photoMapItem = PhotoMapItem()
                photoMapItem.imagePath = targetFile.absolutePath
                val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
                photoMapItem.date = if(date != null) CommonUtils.DATE_TIME_PATTERN.format(date) else getString(R.string.file_explorer_message2)

                val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                    entity.longitude = gpsDirectory.geoLocation.longitude
                    entity.latitude = gpsDirectory.geoLocation.latitude
                    val listAddress = CommonUtils.getFromLocation(this@FileExplorerActivity, entity.latitude, entity.longitude, 1, 0)
                    if (listAddress.size > 0) {
                        entity.info = CommonUtils.fullAddress(listAddress.get(0))
                    }
                    val sb = StringBuilder()
                    sb.append(entity.imagePath + "|")
                    sb.append(entity.info + "|")
                    sb.append(entity.latitude + "|")
                    sb.append(entity.longitude + "|")
                    sb.append(entity.date + "\n")
                    if (CommonUtils.isMatchLine(Constant.PHOTO_DATA_PATH, sb.toString())) {
                        message.obj = getString(R.string.file_explorer_message3)
                        registerHandler.sendMessage(message)
                    } else {
                        CommonUtils.writeDataFile(sb.toString(), Constant.PHOTO_DATA_PATH, true)
                        //                        if (!new File(Constant.WORKING_DIRECTORY + fileName + ".thumb").exists()) {
                        CommonUtils.createScaledBitmap(targetFile.absolutePath, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200)
                        //                        }
                        message.obj = getString(R.string.file_explorer_message4)
                        registerHandler.sendMessage(message)
                    }
                } else {
                    message.obj = entity
                    registerHandler.sendMessage(message)
                }
            } catch (e: Exception) {
                AAFLogger.info("FileExplorerActivity-run INFO: " + e.message, javaClass)
                AAFLogger.info("RegisterThread-run INFO: exception is " + e, javaClass)
                message.obj = e.message
                registerHandler.sendMessage(message)
            }

        }

        override fun run() {
            registerSingleFile()
        }
    }

}