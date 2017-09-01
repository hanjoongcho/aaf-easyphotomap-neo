package io.github.hanjoongcho.easyphotomap.models

import org.apache.commons.io.FilenameUtils


/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
class ExplorerItem : Comparable<ExplorerItem> {

    var imagePath: String? = null
    var fileName: String? = null
    var isDirectory: Boolean = false


    fun setImagePathAndFileName(imagePath: String) {
        this.imagePath = imagePath
        this.fileName = FilenameUtils.getName(imagePath)
    }

    override fun toString(): String {
        return fileName as String
    }

    override fun compareTo(entity: ExplorerItem): Int {
        return (fileName?.compareTo(entity.fileName as String)) as Int
    }
}
