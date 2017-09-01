package io.github.hanjoongcho.commons.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by hanjoong on 2017-09-01.
 */

object PreferenceUtils {

    fun loadBooleanPreference(context: Context, key: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, false)
    }

    fun saveBooleanPreference(context: Context, key: String, isEnable: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putBoolean(key, isEnable)
        edit.commit()
    }

    fun loadIntPreference(context: Context, key: String, defaultValue: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, defaultValue)
    }

    fun loadStringPreference(context: Context, key: String, defaultValue: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, defaultValue)
    }

    fun saveStringPreference(context: Context, key: String, value: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putString(key, value)
        edit.commit()
    }
}
