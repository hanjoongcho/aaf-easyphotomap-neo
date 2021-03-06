package io.github.hanjoongcho.easyphotomap.activities

import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import io.github.hanjoongcho.commons.utils.*
import io.github.hanjoongcho.easyphotomap.Constants
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.adapters.ExplorerItemAdapter
import io.github.hanjoongcho.easyphotomap.helpers.PhotoMapDbHelper
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

    private var mCurrentPath: String? = null
    private var mListFileExplorerFile: ArrayList<FileExplorerItem>? = null
    private var mListFileExplorerDirectory: ArrayList<FileExplorerItem>? = null
    private var mFileExplorerAdapter: ArrayAdapter<FileExplorerItem>? = null
    private val mPrevious: FileExplorerItem = FileExplorerItem()

    init {
        mPrevious.isDirectory = true
        mPrevious.fileName = Constants.PREVIOUS_DIRECTORY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        mCurrentPath = Environment.getExternalStorageDirectory().absolutePath + "/DCIM";
        mListFileExplorerFile = arrayListOf<FileExplorerItem>()
        mListFileExplorerDirectory = arrayListOf<FileExplorerItem>()
        mFileExplorerAdapter = ExplorerItemAdapter(this, this, R.layout.item_file_explorer, mListFileExplorerFile as ArrayList<FileExplorerItem>);
        fileListView.adapter = mFileExplorerAdapter
        fileListView.setOnItemClickListener { adapterView, _, position, _ ->
            val item = adapterView.adapter.getItem(position) as FileExplorerItem
            var fileName = item.fileName as String
            if (fileName.startsWith("[") && fileName.endsWith("]")) {
                fileName = fileName.substring(1, fileName.length - 1)
            }
            val path = mCurrentPath + "/" + fileName
            val file = File(path)
            if (file.isDirectory) {
                mCurrentPath = if (fileName == Constants.PREVIOUS_DIRECTORY) mCurrentPath?.substring(0, mCurrentPath!!.lastIndexOf("/")) else path
                this@FileExplorerActivity.refreshList()
            } else {
                // TODO register photo map
                registerPhotoMap(this, item.fileName, item.imagePath)
            }
        }
        finishButton.setOnClickListener{ finish() }
        refreshList()
    }

    private fun refreshList() {

        val paths = (mCurrentPath?.split("/") as List<String?>).filter { it -> !it.isNullOrEmpty() }
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
                mCurrentPath = targetPath
                this@FileExplorerActivity.refreshList()
            }
            navigation?.addView(textView)
        }
        navigationContainer.postDelayed(Runnable { navigationContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
        RefreshThread().start()
    }

    internal inner class RefreshThread : Thread() {

        override fun run() {
            mListFileExplorerFile?.clear()
            mListFileExplorerDirectory?.clear()
            val fileNames: Array<String>? = File(mCurrentPath).list()
            fileNames?.let {
                for (fileName in fileNames) {
                    val explorerFile = FileExplorerItem()
                    val path = mCurrentPath + "/" + fileName
                    var name = ""
                    val file = File(path)
                    if (file.isDirectory) {
                        name = "[$fileName]"
                        explorerFile.setImagePathAndFileName(name)
                        explorerFile.isDirectory = true
                        mListFileExplorerDirectory?.add(explorerFile)
                    } else {
                        name = fileName
                        val extension = FilenameUtils.getExtension(name).toLowerCase()
                        if (!extension.matches("jpg|jpeg".toRegex())) continue
                        explorerFile.setImagePathAndFileName(path)
                        mListFileExplorerFile?.add(explorerFile)
                    }
                }
            }

//            if (PreferenceUtils.loadBooleanPreference(this@FileExplorerActivity, Constants.SETTING_FILE_EXPLORER_ENABLE_REVERSE_ORDER)) {
                Collections.sort(mListFileExplorerDirectory, Collections.reverseOrder<Any>())
                Collections.sort(mListFileExplorerFile, Collections.reverseOrder<Any>())
//            } else {
//                Collections.sort(mListFileExplorerDirectory)
//                Collections.sort(mListFileExplorerFile)
//            }
            mListFileExplorerFile?.addAll(0, mListFileExplorerDirectory as Collection<FileExplorerItem>)
            if (StringUtils.split(mCurrentPath, "/").size > 1) mListFileExplorerFile?.add(0, mPrevious)
            android.os.Handler(Looper.getMainLooper()).post { mFileExplorerAdapter?.notifyDataSetChanged() }
        }
    }

    private fun registerPhotoMap (context: Context, fileName: String?, path: String?) {
        if (fileName != null && path != null) {
            val registerThread = RegisterThread(context, fileName, path)
            registerThread.start()
        }
    }

    inner class RegisterThread(var context: Context, private var fileName: String, private var path: String) : Thread() {

        private fun registerSingleFile() {
            var resultMessage: String? = null
            try {

                var targetFile: File? = null
                if (PreferenceUtils.loadBooleanPreference(this@FileExplorerActivity, Constants.SETTING_ENABLE_CREATE_COPY)) {
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

                // check duplication
                val tempList = PhotoMapDbHelper.selectPhotoMapItemBy(photoMapItem, "imagePath", photoMapItem.imagePath!!)
                if (tempList.size > 0) throw Exception(getString(R.string.file_explorer_register_error_message2))

                val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
                photoMapItem.date = if(date != null) DateUtils.getFullPatternDateWithTime(date) else getString(R.string.file_explorer_register_error_message)

                val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                    photoMapItem.longitude = gpsDirectory.geoLocation.longitude
                    photoMapItem.latitude = gpsDirectory.geoLocation.latitude
                    val listAddress = GpsUtils.getFromLocation(this@FileExplorerActivity, photoMapItem.latitude, photoMapItem.longitude, 1, 0)
                    listAddress?.let {
                        photoMapItem.info = GpsUtils.fullAddress(listAddress[0])
                    }

                    val result = BitmapUtils.createScaledBitmap(targetFile.absolutePath, Constants.WORKING_DIRECTORY + fileName + ".thumb", 200)
                    if (result) PhotoMapDbHelper.insertPhotoMapItem(photoMapItem)
                    resultMessage = getString(R.string.file_explorer_register_complete)
                }
            } catch (e: Exception) {
                val errorMessage = e.message
                resultMessage = "ERROR: $errorMessage"
            }

            resultMessage?.let {
                android.os.Handler(Looper.getMainLooper()).post {
                    DialogUtils.showAlertDialog(this@FileExplorerActivity, resultMessage!!, DialogInterface.OnClickListener { _, _ ->  } )
                }
            }
        }

        override fun run() {
            registerSingleFile()
        }
    }

}