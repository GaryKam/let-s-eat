package io.github.garykam.letseat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 12
private lateinit var placesService: PlacesService

class MainActivity : AppCompatActivity() {
    /**
     * @return The API key to make Places API requests
     */
    private external fun getApiKey(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        placesService = retrofit.create(PlacesService::class.java)

        // Display the name of a location when the button is clicked.
        findViewById<Button>(R.id.button_eat).setOnClickListener {
            val location = getLocation()

            if (location == null) {
                findViewById<TextView>(R.id.text_location).setText(R.string.error_location)
            } else {
                getNearbyPlace(location.first, location.second)
            }
        }
    }

    /**
     * Checks if location permission was granted.
     */
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
                            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        }
    }

    /**
     * @return Your device's location, or null if permission is not granted
     */
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
        criteria.accuracy = Criteria.ACCURACY_FINE
        val provider =
            locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
        val location = locationManager.getLastKnownLocation(provider)

        return if (location == null) null else Pair(location.latitude, location.longitude)
    }

    /**
     * Finds a location nearby using the Places API.
     */
    private fun getNearbyPlace(latitude: Double, longitude: Double) {
        val options = mapOf(
            "location" to "$latitude,$longitude",
            "radius" to "16093",
            "type" to "restaurant",
            "keyword" to "food",
            "key" to getApiKey()
        )

        placesService.searchNearby(options).enqueue(object : retrofit2.Callback<Response> {
            override fun onResponse(
                call: retrofit2.Call<Response>, response: retrofit2.Response<Response>
            ) {
                val text = if (response.isSuccessful) {
                    val results = response.body()!!.results

                    if (results.isEmpty()) {
                        getString(R.string.missing_location)
                    } else {
                        results.random().name
                    }
                } else {
                    getString(R.string.error_location)
                }

                runOnUiThread {
                    findViewById<TextView>(R.id.text_location).text = text
                }
            }

            override fun onFailure(call: retrofit2.Call<Response>, t: Throwable) {
                runOnUiThread {
                    findViewById<TextView>(R.id.text_location).setText(R.string.error_location)
                }
            }
        })
    }

    companion object {
        init {
            // Allow API key to be read from cpp file.
            System.loadLibrary("native-lib")
        }
    }
}