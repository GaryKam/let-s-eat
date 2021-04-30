package io.github.garykam.letseat.ui

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
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import io.github.garykam.letseat.R
import io.github.garykam.letseat.databinding.ActivityMainBinding
import io.github.garykam.letseat.repository.PlacesRepository

private const val TAG = "MainActivity"
private const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PlacesRepository)
        ).get(MainViewModel::class.java)

        binding.buttonEat.setOnClickListener { showPlace() }

        // Display an image and name of the location.
        viewModel.currentPlace.observe(this) { place ->
            if (place.photos != null) {
                Picasso.get()
                    .load(viewModel.getImageUrl(place.photos[0].reference))
                    .into(binding.imageLocation)
            }

            binding.textLocation.text = place.name
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
     * Displays the next available location.
     * If no location is available,
     * then [MainViewModel.loadNewPlaces] should be called.
     */
    private fun showPlace() {
        if (viewModel.hasPlaces()) {
            viewModel.nextPlace()
        } else {
            val location = getLocation()

            if (location == null) {
                binding.imageLocation.setImageDrawable(null)
                binding.textLocation.setText(R.string.error_location)
            } else {
                viewModel.loadNewPlaces(location.first, location.second)
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
        criteria.accuracy = Criteria.ACCURACY_COARSE
        val provider =
            locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
        val location = locationManager.getLastKnownLocation(provider)

        return if (location == null) null else Pair(location.latitude, location.longitude)
    }
}