package io.github.hanjoongcho.commons.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.Snackbar
import android.view.View
import io.github.hanjoongcho.easyphotomap.R

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
object DialogUtils {

    fun makeSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener,
                        negativeListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setNegativeButton(context.getString(R.string.common_cancel), negativeListener)
        builder.setPositiveButton(context.getString(R.string.common_confirm), positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.common_confirm), positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        title: String,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.common_confirm), positiveListener)
        val alert = builder.create()
        alert.show()
    }

}
