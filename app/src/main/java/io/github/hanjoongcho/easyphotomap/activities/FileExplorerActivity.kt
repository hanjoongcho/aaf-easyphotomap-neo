package io.github.hanjoongcho.easyphotomap.activities

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
import io.github.hanjoongcho.commons.utils.PreferenceUtils
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.adapters.ExplorerItemAdapter
import io.github.hanjoongcho.easyphotomap.models.ExplorerItem
import kotlinx.android.synthetic.main.activity_file_explorer.*
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

/**
 * Created by hanjoong on 2017-09-01.
 */
class FileExplorerActivity : AppCompatActivity() {

    private var currentPath: String? = null
    private var listExplorerFile: ArrayList<ExplorerItem>? = null
    private var listExplorerDirectory: ArrayList<ExplorerItem>? = null
    private var explorerAdapter: ArrayAdapter<ExplorerItem>? = null

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        currentPath = Environment.getExternalStorageDirectory().absolutePath + "/DCIM";
        listExplorerFile = arrayListOf<ExplorerItem>()
        listExplorerDirectory = arrayListOf<ExplorerItem>()
        explorerAdapter = ExplorerItemAdapter(this, this, R.layout.item_file_explorer, listExplorerFile as ArrayList<ExplorerItem>);
        fileListView.adapter = explorerAdapter
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
            listExplorerFile?.clear()
            listExplorerDirectory?.clear()
//            val current = File(currentPath)
            val fileNames = File(currentPath).list()
            for (fileName in fileNames) {
                val explorerFile = ExplorerItem()
                val path = currentPath + "/" + fileName
                var name = ""
                val file = File(path)
                if (file.isDirectory) {
                    name = "[$fileName]"
                    explorerFile.setImagePathAndFileName(name)
                    explorerFile.isDirectory = true
                    listExplorerDirectory?.add(explorerFile)
                } else {
                    name = fileName
                    val extension = FilenameUtils.getExtension(name).toLowerCase()
                    if (!extension.matches("jpg|jpeg".toRegex())) continue
                    explorerFile.imagePath = path
                    listExplorerFile?.add(explorerFile)
                }
            }

            if (PreferenceUtils.loadBooleanPreference(this@FileExplorerActivity, "enable_reverse_order")) {
                Collections.sort(listExplorerDirectory, Collections.reverseOrder<Any>())
                Collections.sort(listExplorerFile, Collections.reverseOrder<Any>())
            } else {
                Collections.sort(listExplorerDirectory)
                Collections.sort(listExplorerFile)
            }
            listExplorerFile?.addAll(0, listExplorerDirectory as Collection<ExplorerItem>)
            android.os.Handler(Looper.getMainLooper()).post { explorerAdapter?.notifyDataSetInvalidated() }
        }
    }

}