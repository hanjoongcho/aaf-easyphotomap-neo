package io.github.hanjoongcho.easyphotomap.helpers

import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 * Created by CHO HANJOONG on 2017-09-03.
 */
class PhotoMapMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        var currentVersion = oldVersion as Int
        if (currentVersion == 1) {
            val diarySchema = schema.get("PhotoMapItem")
            currentVersion++
        }

    }
}