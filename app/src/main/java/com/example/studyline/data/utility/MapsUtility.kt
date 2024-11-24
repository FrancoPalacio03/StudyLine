package com.example.studyline.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MapsUtility(private val context: Context, private val lifecycleScope: LifecycleCoroutineScope) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtener la ubicación actual del usuario.
     */
    fun getCurrentLocation(onLocationReceived: (latitude: Double, longitude: Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Convertir coordenadas (latitud y longitud) en una dirección legible.
     */
    fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double,
        onAddressReceived: (address: String?) -> Unit
    ) {
        lifecycleScope.launchWhenStarted {
            val address = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.getOrNull(0)?.getAddressLine(0)
                } catch (e: Exception) {
                    null
                }
            }
            onAddressReceived(address)
        }
    }

    /**
     * Configurar el mapa y añadir un marcador en la ubicación especificada.
     */
    fun setupMap(
        googleMap: GoogleMap,
        latitude: Double,
        longitude: Double,
        markerTitle: String = "Ubicación actual"
    ) {
        val latLng = LatLng(latitude, longitude)
        googleMap.apply {
            clear() // Limpia cualquier marcador previo
            addMarker(MarkerOptions().position(latLng).title(markerTitle))
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }
}
