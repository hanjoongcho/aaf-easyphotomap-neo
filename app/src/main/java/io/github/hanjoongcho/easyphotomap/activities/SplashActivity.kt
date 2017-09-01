package io.github.hanjoongcho.easyphotomap.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import io.github.hanjoongcho.commons.utils.FontUtils
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.Constants
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by CHO HANJOONG on 2017-08-31.
 */
class SplashActivity : Activity(), Handler.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeFace = FontUtils.getTypeface(assets, "NanumGothic.ttf")
        companyName.setTypeface(typeFace, Typeface.BOLD)
        appName.setTypeface(typeFace, Typeface.BOLD)

        Handler(this).sendEmptyMessageDelayed(Constants.START_MAIN_ACTIVITY, 1000)
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            Constants.START_MAIN_ACTIVITY -> {
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            }
            else -> {
            }
        }
        return false
    }
}
