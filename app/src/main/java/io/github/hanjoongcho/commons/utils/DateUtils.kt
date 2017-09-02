package io.github.hanjoongcho.commons.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-09-03.
 */
object DateUtils {

    val TIME_HM_PATTERN_COLON = "HH:mm"

    fun getFullPatternDateWithTime(date: Date): String {
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
        val hourFormat = SimpleDateFormat(TIME_HM_PATTERN_COLON)
        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
    }

    fun getFullPatternDateWithTime(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
        val hourFormat = SimpleDateFormat(TIME_HM_PATTERN_COLON)
        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
    }
}