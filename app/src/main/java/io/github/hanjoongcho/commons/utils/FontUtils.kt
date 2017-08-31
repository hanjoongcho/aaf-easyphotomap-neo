package io.github.hanjoongcho.commons.utils

import android.content.res.AssetManager
import android.graphics.Typeface

/**
 * Created by hanjoong on 2017-08-31.
 */

object FontUtils {

    private var typeface: Typeface? = null

    fun getTypeface(assetManager: AssetManager, fontFileName: String): Typeface? {
        return typeface.let { setTypeface(assetManager, fontFileName) } ?: typeface
    }

    private fun setTypeface(assetManager: AssetManager, fontFileName: String): Typeface? {
        typeface = Typeface.createFromAsset(assetManager, "fonts/" + fontFileName)
        return typeface
    }

}
