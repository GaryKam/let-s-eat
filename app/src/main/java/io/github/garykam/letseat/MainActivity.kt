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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

private const val TAG = "MainActivity"
private const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private external fun getApiKey(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_eat).setOnClickListener {
            val location = getLocation()

            if (location == null) {
                findViewById<TextView>(R.id.text_location).setText(R.string.error_location)
            } else {
                getNearbyPlace(location.first, location.second)
            }
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
                            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
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

    private fun getNearbyPlace(latitude: Double, longitude: Double) {
        val placeUrl = HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .addPathSegments("maps/api/place/findplacefromtext/json")
            .addQueryParameter("key", getApiKey())
            .addQueryParameter("input", "restaurant")
            .addQueryParameter("inputtype", "textquery")
            .addQueryParameter("fields", "name")
            .addQueryParameter("locationbias", "circle:16093@$latitude,$longitude")
            .build()

        val request = Request.Builder()
            .url(placeUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    findViewById<TextView>(R.id.text_location).setText(R.string.error_location)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val candidates = json.getJSONArray("candidates")

                if (candidates.length() > 0) {
                    val location = candidates.getJSONObject(0)

                    if (location.has("name")) {
                        runOnUiThread {
                            findViewById<TextView>(R.id.text_location).text =
                                location.getString("name")
                        }
                    }
                }
            }
        })
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}