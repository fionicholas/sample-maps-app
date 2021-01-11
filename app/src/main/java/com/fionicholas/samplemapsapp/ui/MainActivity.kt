package com.fionicholas.samplemapsapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fionicholas.samplemapsapp.R
import com.fionicholas.samplemapsapp.data.model.BookmarkView
import com.fionicholas.samplemapsapp.data.model.PlaceInfo
import com.fionicholas.samplemapsapp.ui.adapter.BookmarkListAdapter
import com.fionicholas.samplemapsapp.ui.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val EXTRA_BOOKMARK_ID =
            "com.fionicholas.samplemapsapp.EXTRA_BOOKMARK_ID"
        private const val TAG = "MapsActivity"
    }

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var bookmarkListAdapter: BookmarkListAdapter
    private var markers = HashMap<Long, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupToolbar()
        setupLocationClient()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        addSampleMarker(map)

        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()
    }

    private fun addSampleMarker(googleMap: GoogleMap?) {
        //setup permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        val location = LatLng(-6.172405749412412, 106.826122830873)
        googleMap?.apply {
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Marker Monas")
                        //Custom Marker
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.ic_custom_marker)))
            )
            moveCamera(CameraUpdateFactory.newLatLng(location))
            //set zoom
            animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))
            //choose map type
            mapType = GoogleMap.MAP_TYPE_TERRAIN
            //enabled gps location user
            isMyLocationEnabled = true
        }
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer)
        toggle.syncState()
    }

    private fun setupMapListeners() {
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        map.setOnMapLongClickListener { latLng ->
            newBookmark(latLng)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null) {
                    GlobalScope.launch {
                        mainViewModel.addBookmarkFromPlace(placeInfo.place,
                            placeInfo.image)
                    }
                }
                marker.remove()
            }
            is BookmarkView -> {
                val bookmarkMarkerView = (marker.tag as
                        BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun createBookmarkObserver() {
        mainViewModel.getBookmarkViews()?.observe(
            this, {

                map.clear()
                markers.clear()

                it?.let {
                    displayAllBookmarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            })
    }

    private fun displayAllBookmarks(
        bookmarks: List<BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun addPlaceMarker(
        bookmark: BookmarkView): Marker? {
        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f))
        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }
        return marker
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        rvBookmark.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        rvBookmark.adapter = bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    fun moveToBookmark(bookmark: BookmarkView) {

        drawerLayout.closeDrawer(drawerView)

        val marker = markers[bookmark.id]

        marker?.showInfoWindow()

        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun newBookmark(latLng: LatLng) {
        GlobalScope.launch {
            val bookmarkId = mainViewModel.addBookmark(latLng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }
}