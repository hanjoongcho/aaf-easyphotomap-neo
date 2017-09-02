package io.github.hanjoongcho.easyphotomap.models

/**
 * Created by hanjoong on 2017-09-02.
 */
class PhotoMapItem : Comparable<PhotoMapItem> {
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

    override fun compareTo(imageEntity: PhotoMapItem): Int {
        var result = 0
        if (sortFlag == 0) {
            result = info!!.compareTo(imageEntity.info!!)
        } else if (sortFlag == 1) {
            result = originDate!!.compareTo(imageEntity.originDate!!)
        }
        return result
    }
}
