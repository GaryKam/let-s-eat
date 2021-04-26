package io.github.garykam.letseat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.*
import java.io.IOException

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private external fun getApiKey(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                200
            )
        }

        getNearbyPlaces()
    }

    private fun getNearbyPlaces() {
        val placesUrl = HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .addPathSegments("maps/api/place/findplacefromtext/json")
            .addQueryParameter("key", getApiKey())
            .addQueryParameter("input", "food")
            .addQueryParameter("inputtype", "textquery")
            .addQueryParameter("fields", "name")
            .addQueryParameter("locationbias", "ipbias")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "permissions: ${grantResults.joinToString()}")
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}