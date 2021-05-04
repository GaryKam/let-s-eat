package io.github.garykam.letseat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import io.github.garykam.letseat.R
import io.github.garykam.letseat.databinding.ActivityMainBinding
import io.github.garykam.letseat.repository.PlacesRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "MainActivity"
private const val REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationProvider: FusedLocationProviderClient

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
            Picasso.get()
                .load(viewModel.getImageUrl(place.photos[0].reference))
                .into(binding.imageLocation)

            binding.textLocation.text = place.name
        }

        // Check if we have Google Play services.
        when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_DISABLED,
            ConnectionResult.SERVICE_INVALID -> disableEatButton()
        }

        // Get a location service from Google Play services.
        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        // Check if we have a location service.
        GoogleApiAvailability.getInstance()
            .checkApiAvailability(locationProvider)
            .addOnFailureListener { disableEatButton() }
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
     * Displays the next available location.
     * If no location is available,
     * then [MainViewModel.loadNewPlaces] should be called.
     */
    private fun showPlace() {
        if (viewModel.hasPlaces()) {
            viewModel.nextPlace()
        } else {
            MainScope().launch {
                val location = getLocation()

                if (location == null) {
                    binding.imageLocation.setImageDrawable(null)
                    binding.textLocation.setText(R.string.error_location)
                } else {
                    viewModel.loadNewPlaces(location.first, location.second)
                }
            }
        }
    }

    /**
     * @return Your device's location, or null if permission is not granted
     */
    private suspend fun getLocation(): Pair<Double, Double>? {
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
     * Changes the functionality of the button to display an error message.
     */
    private fun disableEatButton() {
        binding.buttonEat.setOnClickListener {
            Toast.makeText(this, R.string.error_location_service, Toast.LENGTH_SHORT).show()
        }
    }
}