package com.fionicholas.samplemapsapp

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        setUpMap(googleMap)
    }

    private fun setUpMap(googleMap: GoogleMap?) {
        //setup permission
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
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
}