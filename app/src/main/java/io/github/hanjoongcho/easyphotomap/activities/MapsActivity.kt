package io.github.hanjoongcho.easyphotomap.activities

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
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

        checkPermissions();
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

    private fun initWorkingDirectory() {
        if (!File(Constants.WORKING_DIRECTORY).exists()) {
            File(Constants.WORKING_DIRECTORY).mkdirs()
        }
    }

    private fun checkPermissions() {
        if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
            // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
            initWorkingDirectory();
        } else {
            // API Level 23 이상이면서 권한취득 안한경우
            PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQUEST_CODE_EXTERNAL_STORAGE -> if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                // 권한이 있는경우
                initWorkingDirectory();
            } else {
                // 권한이 없는경우
//                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {
            }
        }
    }
}
