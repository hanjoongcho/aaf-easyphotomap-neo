package io.github.hanjoongcho.easyphotomap.activities

import android.app.Activity
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
import android.content.Intent
import io.github.hanjoongcho.commons.utils.CommonUtils


/**
 * Created by CHO HANJOONG on 2017-09-05.
 */
class GroupSearchActivity : AppCompatActivity() {

    private var mListRecommendationOrigin: ArrayList<Recommendation>? = arrayListOf()
    private var mListRecommendation: ArrayList<Recommendation>? = arrayListOf()
    private var mListPhotoMapItem: ArrayList<PhotoMapItem>? = arrayListOf()
    private var mRecommendMap: HashMap<String, Int>? = hashMapOf()
    private var mAdapter: ArrayAdapter<Recommendation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_search)

        parseMetadata()
        for (item in mListPhotoMapItem!!.iterator()) {
            val arr = StringUtils.split(item.info, " ")
            for (keyword in arr.filter { it ->  !(Pattern.matches("^(([0-9]{1,9})-([0-9]{1,9}))|(([0-9]{1,9}))$", it) || it.length < 2) }) {
                if (mRecommendMap?.containsKey(keyword)!!) {
                    mRecommendMap?.put(keyword, (mRecommendMap?.get(keyword) as Int) + 1)
                } else {
                    mRecommendMap?.put(keyword, 1)
                }
            }
        }

        val listOfSortEntry = CommonUtils.entriesSortedByValues(mRecommendMap as Map<String, Int>)
        mListRecommendationOrigin?.clear()
        mListRecommendation?.clear()
        for ((key, value) in listOfSortEntry) {
            mListRecommendationOrigin?.add(Recommendation(key, value))
        }
        mListRecommendation?.addAll(mListRecommendationOrigin as Collection<Recommendation>)
        mAdapter = ArrayAdapter(this, R.layout.item_group_search, mListRecommendation);
        searchListView.adapter = mAdapter

        groupSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mListRecommendation?.clear()
                for (recommendation in mListRecommendationOrigin!!.iterator()) {
                    if (StringUtils.contains(recommendation.keyWord, newText)) {
                        mListRecommendation?.add(recommendation)
                    }
                }
                mAdapter?.notifyDataSetChanged()
                return false
            }
        })

        searchListView.setOnItemClickListener { adapterView, _, position, _ ->
            val recommendation = adapterView.adapter.getItem(position) as Recommendation
            val intent = Intent()
            intent.putExtra("keyword", recommendation.keyWord)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        finishButton.setOnClickListener{ finish() }
    }

    private inner class Recommendation(internal var keyWord: String, internal var count: Int) {

        override fun toString(): String {
            val cases = getString(R.string.recommendation_group_count_case)
            return "$keyWord [$count $cases]"
        }
    }

    private fun parseMetadata() {
        mListPhotoMapItem?.clear()
        mRecommendMap?.clear()
        mListPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll()
        Collections.sort(mListPhotoMapItem)
    }

}