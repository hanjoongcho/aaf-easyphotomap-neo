package io.github.hanjoongcho.commons.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.github.hanjoongcho.easyphotomap.R

/**
 * Created by CHO HANJOONG on 2016-10-09.
 */

object PermissionUtils {

    fun confirmPermission(context: Context, activity: Activity, permissions: Array<String>, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.confirmPermission_message))
                    .setTitle(context.getString(R.string.confirmPermission_title))
                    .setPositiveButton(context.getString(R.string.common_confirm)) { _, _ -> ActivityCompat.requestPermissions(activity, permissions, requestCode) }
                    .show()
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun checkPermission(context: Context, permissions: Array<String>): Boolean {
        return permissions.filter { it -> ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED }.isEmpty()
    }

}
