package io.github.hanjoongcho.easyphotomap.helpers

import android.app.Application
import io.realm.Realm

/**
 * Created by CHO HANJOONG on 2017-09-03.
 */
class EasyPhotoMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}