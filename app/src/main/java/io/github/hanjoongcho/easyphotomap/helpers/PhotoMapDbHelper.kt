package io.github.hanjoongcho.easyphotomap.helpers

import io.github.hanjoongcho.easyphotomap.models.PhotoMapItem
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort

/**
 * Created by CHO HANJOONG on 2017-09-03.
 */
object PhotoMapDbHelper {

    private @Volatile var diaryConfig: RealmConfiguration? = null

    private fun getRealmInstance(): Realm {
        if (diaryConfig == null) {
            diaryConfig = RealmConfiguration.Builder()
                    .name("easyphotomap.realm")
                    .schemaVersion(1)
                    .migration(PhotoMapMigration())
                    .modules(Realm.getDefaultModule())
                    .build()

        }
        return Realm.getInstance(diaryConfig)
    }

    fun insertPhotoMapItem(photoMapItem: PhotoMapItem) {
        val realm = getRealmInstance()
        var item = photoMapItem
        // async call
//        realm.executeTransaction { realmTransaction ->
//            var sequence = 1
//            if (realmTransaction.where(PhotoMapItem::class.java).count() > 0L) {
//                val number = realmTransaction.where(PhotoMapItem::class.java).max("sequence")
//                sequence = number.toInt() + 1
//            }
//            item.sequence = sequence
//            realmTransaction.insert(item)
//        }
        // sync call
        realm.beginTransaction()
        var sequence = 1
        if (realm.where(PhotoMapItem::class.java).count() > 0L) {
            val number = realm.where(PhotoMapItem::class.java).max("sequence")
            sequence = number.toInt() + 1
        }
        item.sequence = sequence
        realm.insert(item)
        realm.commitTransaction()
    }

    fun selectPhotoMapItemAll(): ArrayList<PhotoMapItem> {
        val realmResults = getRealmInstance().where(PhotoMapItem::class.java).findAllSorted("sequence", Sort.DESCENDING)
        val list = arrayListOf<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

//    fun selectPhotoMapItemBy(keyword: String): ArrayList<PhotoMapItem> {
//        val realmResults = getRealmInstance().where(PhotoMapItem::class.java).equalTo("dateString", dateString).findAllSorted("sequence", Sort.DESCENDING)
//        val list = arrayListOf<PhotoMapItem>()
//        list.addAll(realmResults.subList(0, realmResults.size))
//        return list
//    }

}
