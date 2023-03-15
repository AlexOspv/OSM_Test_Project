package com.alexos.osm_task

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.alexos.osm_task.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class MainActivity : AppCompatActivity() {

    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityMainBinding
    private lateinit var icon: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsOsm()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerPermissions()
        checkLocationPermission()
        icon = ResourcesCompat.getDrawable(resources, R.mipmap.ic_pizza_icon, null)!!
        binding.moveToPointBButton.setOnClickListener {
            if (checkPointB()){
                moveToBOsm()
            }
        }
        binding.navigateFromAToB.setOnClickListener {
            if (checkPointA() && checkPointB()){
                navigateFromAToB()
            }
        }
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            this,
            this.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = org.osmdroid.library.BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {
        mapView.controller.setZoom(15.0)
        mapView.controller.animateTo(GeoPoint( 55.7520, 37.6174))
    }

    private fun moveToBOsm() = with(binding) {
        mapView.overlays.clear()
        val marker = Marker(mapView)
        val pointB = GeoPoint(binding.latitudeB.text.toString().toDouble(), binding.longitudeB.text.toString().toDouble())
        marker.position = pointB
        marker.setInfoWindow(null)
        mapView.controller.setCenter(pointB)
        marker.icon = icon
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mapView.controller.setZoom(15.0)
        mapView.controller.animateTo(pointB)
        mapView.overlays.add(marker)
        refreshCoordinates()
    }

    private fun navigateFromAToB() = with(binding) {
        val markerA = Marker(mapView)
        val markerB = Marker(mapView)
        val pointA = GeoPoint(binding.latitudeA.text.toString().toDouble(), binding.longitudeA.text.toString().toDouble())
        val pointB = GeoPoint(binding.latitudeB.text.toString().toDouble(), binding.longitudeB.text.toString().toDouble())
        val geoPoints: MutableList<GeoPoint> = ArrayList()
        val line = Polyline()
        geoPoints.add(pointA)
        geoPoints.add(pointB)
        line.setPoints(geoPoints)
        line.outlinePaint.color = Color.RED
        markerA.position = pointA
        markerA.setInfoWindow(null)
        markerB.position = pointB
        markerB.setInfoWindow(null)
        markerA.icon = icon
        markerB.icon = icon
        markerA.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        markerB.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mapView.controller.setZoom(15.0)
        mapView.overlays.add(markerA)
        mapView.overlays.add(markerB)
        mapView.overlayManager.add(line)
        refreshCoordinates()

    }

    private fun refreshCoordinates() {
        val newLatA = binding.latitudeB.text as CharSequence
        binding.latitudeA.setText(newLatA)
        val newLongA = binding.longitudeB.text as CharSequence
        binding.longitudeA.setText(newLongA)
        binding.latitudeB.setText("")
        binding.longitudeB.setText("")
    }

    private fun checkPointA():Boolean {
        return if (binding.latitudeA.text.isEmpty()){
            Toast.makeText(this, "Введите Широту точки А!", Toast.LENGTH_SHORT).show()
            false
        } else if (binding.longitudeA.text.isEmpty()){
            Toast.makeText(this, "Введите Долготу точки А!", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun checkPointB():Boolean {
        return if (binding.latitudeB.text.isEmpty()){
            Toast.makeText(this, "Введите Широту точки Б!", Toast.LENGTH_SHORT).show()
            false
        } else if (binding.longitudeB.text.isEmpty()){
            Toast.makeText(this, "Введите Долготу точки Б!", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun checkPermission(p: String): Boolean{
        return when(PackageManager.PERMISSION_GRANTED){
            ContextCompat.checkSelfPermission(this@MainActivity, p) -> true
            else -> false
        }
    }

    private fun registerPermissions() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOsm()
            } else {
                Toast.makeText(this, "Вы не дали разрешение на использование местоположения!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationPermission() {
        if(checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsm()
        }else{
            pLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

}