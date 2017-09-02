package io.github.hanjoongcho.commons.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

/**
 * Created by CHO HANJOONG on 2017-09-03.
 */

object GpsUtils {

    private val MAX_RETRY = 5

    @Throws(Exception::class)
    fun getFromLocation(context: Context, latitude: Double, longitude: Double, maxResults: Int, retryCount: Int): List<Address>? {
        var latitude = latitude
        var longitude = longitude
        var retryCount = retryCount
        latitude = java.lang.Double.parseDouble(String.format("%.6f", latitude))
        longitude = java.lang.Double.parseDouble(String.format("%.7f", longitude))
        var listAddress: List<Address>? = null
        val locale = Locale.getDefault()
        val geoCoder = Geocoder(context, locale)
        try {
            listAddress = geoCoder.getFromLocation(latitude, longitude, maxResults)
        } catch (e: Exception) {
            if (retryCount < MAX_RETRY) {
                return getFromLocation(context, latitude, longitude, maxResults, ++retryCount)
            }
            throw Exception(e.message)
        }
        return listAddress
    }

    fun fullAddress(address: Address): String {
        val sb = StringBuilder()
        if (address.countryName != null) sb.append(address.countryName).append(" ")
        if (address.adminArea != null) sb.append(address.adminArea).append(" ")
        if (address.locality != null) sb.append(address.locality).append(" ")
        if (address.subLocality != null) sb.append(address.subLocality).append(" ")
        if (address.thoroughfare != null) sb.append(address.thoroughfare).append(" ")
        if (address.featureName != null) sb.append(address.featureName).append(" ")
        return sb.toString()
    }

}
