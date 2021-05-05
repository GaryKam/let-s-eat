package io.github.garykam.letseat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import io.github.garykam.letseat.R
import io.github.garykam.letseat.databinding.ActivityMainBinding
import io.github.garykam.letseat.repository.PlacesRepository
import io.github.garykam.letseat.util.LocationHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PlacesRepository)
        ).get(MainViewModel::class.java)

        locationHelper = LocationHelper(this)

        binding.buttonEat.setOnClickListener { showPlace() }

        // Display an image and name of the location.
        viewModel.currentPlace.observe(this) { place ->
            Picasso.get()
                .load(viewModel.getImageUrl(place.photos[0].reference))
                .into(binding.imageLocation)

            binding.textLocation.text = place.name
        }
    }

    /**
     * Location may be null if the provider has none cached.
     * Calls [LocationHelper.updateLocation]
     * to ensure we have a location to fallback to.
     */
    override fun onStart() {
        super.onStart()

        locationHelper.updateLocation()
    }

    /**
     * Checks if location permission was granted.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LocationHelper.LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "Location request was interrupted.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    showPlace()
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
     * Displays the next location nearby.
     */
    private fun showPlace() {
        MainScope().launch {
            if (locationHelper.isAvailable()) {
                viewModel.getPlace(locationHelper)
            } else {
                Toast.makeText(this@MainActivity, R.string.error_location_service, Toast.LENGTH_SHORT).show()
            }
        }
    }
}