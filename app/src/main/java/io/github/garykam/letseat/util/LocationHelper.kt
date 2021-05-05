package io.github.garykam.letseat.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationHelper(private val activity: Activity) {
    /* A location service from Google Play services. */
    private val locationProvider = LocationServices.getFusedLocationProviderClient(activity)

    /**
     * @return Your device's location, or null if permission is not granted
     */
    suspend fun getLocation(): Pair<Double, Double>? {
        if (!checkPermission()) {
            return null
        }

        return suspendCoroutine<Pair<Double, Double>> { continuation ->
            locationProvider.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    continuation.resume(Pair(task.result.latitude, task.result.longitude))
                } else {
                    task.exception?.let { continuation.resumeWithException(it) }
                }
            }
        }
    }

    /**
     * Makes a request to find the device's current location.
     */
    fun updateLocation() {
        if (!checkPermission()) {
            return
        }

        LocationRequest.create().apply {
            interval = 60 * 1000 // milliseconds
            fastestInterval = 5 * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }.run {
            val locationCallback = object : LocationCallback() {}

            locationProvider.requestLocationUpdates(
                this,
                locationCallback,
                activity.mainLooper
            )

            // Continuous location updates are not necessary.
            locationProvider.removeLocationUpdates(locationCallback)
        }
    }

    suspend fun isAvailable(): Boolean {
        // Check if we have Google Play services.
        when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)) {
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_DISABLED,
            ConnectionResult.SERVICE_INVALID -> return false
        }

        // Check if we have a valid location service.
        return suspendCoroutine { continuation ->
            GoogleApiAvailability.getInstance()
                .checkApiAvailability(locationProvider)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }
    }

    /**
     * @return True if location permissions have been granted
     */
    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSIONS_REQUEST_CODE
            )

            false
        }
    }

    companion object {
        const val LOCATION_PERMISSIONS_REQUEST_CODE = 12
    }
}