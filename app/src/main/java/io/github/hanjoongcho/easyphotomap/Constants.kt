package io.github.hanjoongcho.easyphotomap

import android.Manifest
import android.os.Environment

/**
 * Created by hanjoong on 2017-08-31.
 */
class Constants {
    companion object {

        // activity status
        val START_MAIN_ACTIVITY = 0

        // path
        val WORKING_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/AAFactory/EasyPhotoMapNeo/"

        // permissions
        val EXTERNAL_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

        // startActivityForResult Request Code: Permission
        val REQUEST_CODE_EXTERNAL_STORAGE = 1

        // etc
        val GOOGLE_MAP_DEFAULT_ZOOM_VALUE = 13.0f
        val GOOGLE_MAP_DEFAULT_LATITUDE = 37.3997208
        val GOOGLE_MAP_DEFAULT_LONGITUDE = 127.1000782


    }
}
