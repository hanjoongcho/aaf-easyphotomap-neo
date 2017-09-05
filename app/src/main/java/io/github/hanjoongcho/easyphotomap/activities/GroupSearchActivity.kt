package io.github.hanjoongcho.easyphotomap.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.widget.ArrayAdapter
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.helpers.PhotoMapDbHelper
import io.github.hanjoongcho.easyphotomap.models.PhotoMapItem
import kotlinx.android.synthetic.main.activity_group_search.*
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern

/**
 * Created by CHO HANJOONG on 2017-09-05.
 */
class GroupSearchActivity : AppCompatActivity() {

    private var listRecommendationOrigin: ArrayList<Recommendation>? = arrayListOf()
    private var listRecommendation: ArrayList<Recommendation>? = arrayListOf()
    private var listPhotoMapItem: ArrayList<PhotoMapItem>? = arrayListOf()
    private var recommendMap: HashMap<String, Int>? = hashMapOf()
    private var adapter: ArrayAdapter<Recommendation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_search)

        parseMetadata()
        for (item in listPhotoMapItem!!.iterator()) {
            val arr = StringUtils.split(item.info, " ")
            for (keyword in arr.filter { it ->  !(Pattern.matches("^(([0-9]{1,9})-([0-9]{1,9}))|(([0-9]{1,9}))$", it) || it.length < 2) }) {
                if (recommendMap?.containsKey(keyword)!!) {
                    recommendMap?.put(keyword, (recommendMap?.get(keyword) as Int) + 1)
                } else {
                    recommendMap?.put(keyword, 1)
                }
            }
        }

//        val listOfSortEntry = CommonUtils.entriesSortedByValues(recommendMap)
        listRecommendationOrigin?.clear()
        listRecommendation?.clear()
        for ((k, v) in recommendMap!!) {
            listRecommendationOrigin?.add(Recommendation(k, v))
        }
        listRecommendation?.addAll(listRecommendationOrigin as Collection<Recommendation>)
        adapter = ArrayAdapter(this, R.layout.item_group_search, listRecommendation);
        searchListView.adapter = adapter

        groupSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                listRecommendation?.clear()
                for (recommendation in listRecommendationOrigin!!.iterator()) {
                    if (StringUtils.contains(recommendation.keyWord, newText)) {
                        listRecommendation?.add(recommendation)
                    }
                }
                adapter?.notifyDataSetChanged()
                return false
            }
        })

        finishButton.setOnClickListener{ finish() }
    }

    private inner class Recommendation(internal var keyWord: String, internal var count: Int) {

        override fun toString(): String {
            val cases = getString(R.string.recommendation_group_count_case)
            return "$keyWord [$count $cases]"
        }
    }

    private fun parseMetadata() {
        listPhotoMapItem?.clear()
        recommendMap?.clear()
        listPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll()
        Collections.sort(listPhotoMapItem)
    }

}