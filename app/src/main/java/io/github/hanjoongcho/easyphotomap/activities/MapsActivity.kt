package io.github.hanjoongcho.easyphotomap.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.github.hanjoongcho.commons.utils.DialogUtils
import io.github.hanjoongcho.commons.utils.PermissionUtils
import io.github.hanjoongcho.easyphotomap.Constants
import io.github.hanjoongcho.easyphotomap.R
import java.io.File

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(Constants.GOOGLE_MAP_DEFAULT_LATITUDE, Constants.GOOGLE_MAP_DEFAULT_LONGITUDE),
                Constants.GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
    }

    fun onItemClick(view: View) {
        when (view.id) {
            R.id.camera -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_CAMERA)
            R.id.gallery -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_GALLERY)
            else -> DialogUtils.makeSnackBar(findViewById(android.R.id.content), "no match")
        }

    }

    private fun initWorkingDirectory() {
        if (!File(Constants.WORKING_DIRECTORY).exists()) {
            File(Constants.WORKING_DIRECTORY).mkdirs()
        }
    }

    private fun startCameraActivity() {
        initWorkingDirectory()
    }

    private fun runAfterPermissionCheck(requestCode: Int) {
        when (requestCode) {
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_CAMERA -> {
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    startCameraActivity()
                } else {
                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, requestCode)
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ((grantResults.filter { it -> it == PackageManager.PERMISSION_DENIED }).isNotEmpty()) {
            DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.common_no_permission))
            return
        }

        when (requestCode) {
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_CAMERA -> {
                startCameraActivity()
            }
            else -> {
            }
        }
    }


}
