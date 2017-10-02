package io.github.hanjoongcho.easyphotomap.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.FragmentActivity
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import io.github.hanjoongcho.commons.utils.BitmapUtils
import io.github.hanjoongcho.commons.utils.CommonUtils
import io.github.hanjoongcho.commons.utils.DialogUtils
import io.github.hanjoongcho.commons.utils.PermissionUtils
import io.github.hanjoongcho.easyphotomap.Constants
import io.github.hanjoongcho.easyphotomap.R
import io.github.hanjoongcho.easyphotomap.helpers.PhotoMapDbHelper
import io.github.hanjoongcho.easyphotomap.models.PhotoMapItem
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the mMap is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the mMap once available.
     * This callback is triggered when the mMap is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(Constants.GOOGLE_MAP_DEFAULT_LATITUDE, Constants.GOOGLE_MAP_DEFAULT_LONGITUDE),
                Constants.GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
    }

    fun onItemClick(view: View) {
        when (view.id) {
            R.id.camera -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_CAMERA)
            R.id.gallery -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_GALLERY)
            R.id.explorer -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_EXPLORER)
            R.id.groupSearch -> runAfterPermissionCheck(Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_GROUP_SEARCH)
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

    private fun startExplorerActivity() {
        initWorkingDirectory()
        startActivity(Intent(this, FileExplorerActivity::class.java))
    }

    private fun startGroupSearchActivity() {
        initWorkingDirectory()
        startActivityForResult(Intent(this, GroupSearchActivity::class.java), Constants.REQUEST_CODE_GROUP_SEARCH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode == Activity.RESULT_OK) {
            true -> {
                overlayIcons(data?.getStringExtra("keyword")!!, false)
            }
            false -> {}
        }
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
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_EXPLORER -> {
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    startExplorerActivity()
                } else {
                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, requestCode)
                }
            }
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_GROUP_SEARCH -> {
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    startGroupSearchActivity()
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
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_EXPLORER -> {
                startExplorerActivity()
            }
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS_FOR_GROUP_SEARCH -> {
                startGroupSearchActivity()
            }
            else -> {
            }
        }
    }

    private fun overlayIcons(keyword: String, applyFilter: Boolean) {
        val overlayThread = OverlayThread(keyword, applyFilter)
        parseMetadata()
        overlayThread.start()
    }

    private var listPhotoMapItem: ArrayList<PhotoMapItem> = arrayListOf()
    private var listPhotoMapItemOverlay: ArrayList<PhotoMapItem> = arrayListOf()
    internal var listLatLng: ArrayList<LatLng> = arrayListOf()
    internal var listMarkerOptions: ArrayList<MarkerOptions> = arrayListOf()

    inner class OverlayThread(private var keyword: String, var applyFilter: Boolean) : Thread() {

        override fun run() {
            super.run()
            // FIXME Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.
            listPhotoMapItem.clear()
            listPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll()
            Collections.sort(listPhotoMapItem)

            listLatLng.clear()
            listMarkerOptions.clear()
            listPhotoMapItemOverlay.clear()
            for (photoMapItem in listPhotoMapItem) {
                if (!photoMapItem.info!!.contains(keyword)) continue
                var image: BitmapDescriptor? = null
                val options = MarkerOptions()
                val latLng = LatLng(photoMapItem.latitude, photoMapItem.longitude)
                options.position(latLng)
                val fileName = FilenameUtils.getName(photoMapItem.imagePath)
                val bm = BitmapUtils.decodeFile(this@MapsActivity, Constants.WORKING_DIRECTORY + FilenameUtils.getBaseName(fileName) + ".thumb")
                val point = Point(bm!!.width, bm.height)
                val fixedWidthHeight = .8F
                val bm2 = BitmapUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight)
                image = BitmapDescriptorFactory.fromBitmap(BitmapUtils.addWhiteBorder(bm2!!, CommonUtils.dpToPixel(this@MapsActivity, 3)))
                options.icon(image)
                listMarkerOptions.add(options)
                listPhotoMapItemOverlay.add(photoMapItem)
            }

            android.os.Handler(Looper.getMainLooper()).post {
                setUpCluster()

                for (i in listMarkerOptions.indices) {
                    val item = MyItem(listMarkerOptions[i], listPhotoMapItem[i])
                    clusterManager?.addItem(item)
                    listLatLng.add(listMarkerOptions[i].position)
                }

                mMap?.setOnMarkerClickListener(clusterManager)
                mMap?.setOnCameraChangeListener(clusterManager)

                val clusterRenderer = MyClusterRenderer(this@MapsActivity, mMap!!, clusterManager!!)
                clusterManager?.setRenderer(clusterRenderer)
                clusterManager?.setOnClusterClickListener { _ ->
                    mMap?.setInfoWindowAdapter(null)
                    false
                }
//                clusterManager?.setOnClusterItemClickListener {item ->
//                    mMap?.setInfoWindowAdapter(InfoWindow(
//                            item.photoMapItem.info,
//                            item.photoMapItem.imagePath,
//                            item.photoMapItem.latitude,
//                            item.photoMapItem.longitude,
//                            item.photoMapItem.date
//                    ))
//                    val fImagePath = item.getPhotoEntity().imagePath
//                    mMap?.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener {
//                        val imageViewIntent = Intent(this@MapsActivity, PopupImageActivity::class.java)
//                        imageViewIntent.putExtra("imagePath", fImagePath)
//                        startActivity(imageViewIntent)
//                    })
//                    false
//                }

                val builder = LatLngBounds.Builder()
                for (latLng in listLatLng) {
                    builder.include(latLng)
                }
                val bounds = builder.build()
                mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
            }
        }
    }

    private var clusterManager: ClusterManager<MyItem>? = null
    private fun setUpCluster() {
        clusterManager = clusterManager ?: ClusterManager<MyItem>(this, mMap)
        clusterManager?.clearItems()
    }

    internal inner class MyItem(val markerOptions: MarkerOptions, val photoMapItem: PhotoMapItem) : ClusterItem {
        override fun getPosition(): LatLng {
            return markerOptions.position
        }
    }

    internal inner class MyClusterRenderer(context: Context, map: GoogleMap,
                                           clusterManager: ClusterManager<MyItem>) : DefaultClusterRenderer<MyItem>(context, map, clusterManager), GoogleMap.OnCameraChangeListener {
        private var mClusterIconGenerator: IconGenerator? = null
        private var mapZoom: Float = 0.toFloat()

        init {
            mClusterIconGenerator = IconGenerator(applicationContext)
        }

        override fun getCluster(marker: Marker): Cluster<MyItem> {
            return super.getCluster(marker)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<MyItem>, markerOptions: MarkerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions)
        }

        override fun onBeforeClusterItemRendered(item: MyItem?,
                                                 markerOptions: MarkerOptions?) {
            super.onBeforeClusterItemRendered(item, markerOptions)
            markerOptions!!.icon(item!!.markerOptions.icon)
        }

        override fun onClusterItemRendered(clusterItem: MyItem?, marker: Marker?) {
            super.onClusterItemRendered(clusterItem, marker)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
            return if (mapZoom > Constants.GOOGLE_MAP_MAX_ZOOM_IN_VALUE - 1) {
                false
            } else {
                cluster.size > 50
            }

        }

        override fun onCameraChange(cameraPosition: CameraPosition) {
            if (cameraPosition.zoom > Constants.GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(Constants.GOOGLE_MAP_MAX_ZOOM_IN_VALUE))
            }
            mapZoom = cameraPosition.zoom
        }
    }

    private fun parseMetadata() {
        listPhotoMapItem?.clear()
        listPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll()
        Collections.sort(listPhotoMapItem)
    }

}
