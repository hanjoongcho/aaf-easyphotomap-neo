package io.github.hanjoongcho.easyphotomap.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by hanjoong on 2017-09-02.
 */
open class PhotoMapItem : RealmObject(), Comparable<PhotoMapItem> {

    @PrimaryKey
    var sequence: Int = 0
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    var info: String? = null
    var imagePath: String? = null
    var date: String? = null
    var originDate: String? = null
    var sortFlag = 0

    override fun toString(): String {
        var info: String? = null
        if (sortFlag == 0) {
            info = this.info
        } else if (sortFlag == 1) {
            info = date
        }
        return info as String
    }

    override operator fun compareTo(item: PhotoMapItem): Int {
        var result: Int = when (sortFlag) {
            0 -> (info as String).compareTo(item.info as String)
            1 -> (originDate as String).compareTo(item.originDate as String)
            else -> -1
        }
        return result
    }
}
