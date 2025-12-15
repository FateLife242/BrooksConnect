package com.example.brooksconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import java.util.Locale

class LocationPickerActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var btnConfirm: Button
    private var selectedPoint: GeoPoint? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                setupMap()
            } else {
                Toast.makeText(this, "Location permission needed to find you", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Important for OSMDroid
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        
        setContentView(R.layout.activity_location_picker)

        map = findViewById(R.id.map)
        btnConfirm = findViewById(R.id.btn_confirm_location)
        
        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setBuiltInZoomControls(true)
        map.controller.setZoom(18.0)
        
        // Default to Brooks Point, Baguio City
        val startPoint = GeoPoint(16.4023, 120.5960) // Baguio City Coordinates
        map.controller.setCenter(startPoint)

        if (checkPermissions()) {
            setupMap()
        } else {
            requestPermissions()
        }

        btnConfirm.setOnClickListener {
            val center = map.mapCenter as GeoPoint
            confirmLocation(center)
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupMap() {
        val locationOverlay = org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay(org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        map.overlays.add(locationOverlay)
        map.controller.setZoom(18.0)
    }

    private fun confirmLocation(point: GeoPoint) {
        val returnIntent = Intent()
        
        // Try reverse geocoding
        val geocoder = Geocoder(this, Locale.getDefault())
        var addressString = "${point.latitude}, ${point.longitude}"
        
        try {
            val addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare ?: ""
                val feature = address.featureName ?: ""
                
                if (street.isNotEmpty()) {
                    addressString = "$street, $feature"
                } else if (feature.isNotEmpty()) {
                    addressString = feature
                }
            }
        } catch (e: Exception) {
            // Geocoding failed, fallback to coords
        }

        returnIntent.putExtra("location_string", addressString)
        returnIntent.putExtra("lat", point.latitude)
        returnIntent.putExtra("lon", point.longitude)
        
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
