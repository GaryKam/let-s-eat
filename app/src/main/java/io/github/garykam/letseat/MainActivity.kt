package io.github.garykam.letseat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.*
import java.io.IOException

private const val TAG = "MainActivity"
private const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private external fun getApiKey(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getLocation()?.let {
            getNearbyPlaces(it.first, it.second)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "Request was interrupted.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    Log.d(TAG, "Request was granted.")
                else -> {
                    Log.d(TAG, "Request was denied.")

                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.permission_location)
                            .setMessage(R.string.permission_location_rationale)
                            .setNeutralButton("Ok") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        }
    }

    private fun getLocation(): Pair<Double, Double>? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE
            )
            return null
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        val provider =
            locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
        val location = locationManager.getLastKnownLocation(provider)

        return if (location == null) null else Pair(location.latitude, location.longitude)
    }

    private fun getNearbyPlaces(latitude: Double, longitude: Double) {
        val placesUrl = HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .addPathSegments("maps/api/place/findplacefromtext/json")
            .addQueryParameter("key", getApiKey())
            .addQueryParameter("input", "food")
            .addQueryParameter("inputtype", "textquery")
            .addQueryParameter("fields", "name")
            .addQueryParameter("locationbias", "circle:100@$latitude,$longitude")
            .build()

        val request = Request.Builder()
            .url(placesUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, response.body!!.string())
            }
        })
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}