package io.github.garykam.letseat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.garykam.letseat.model.Place
import io.github.garykam.letseat.utils.ApiHelper
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    /* The location being displayed. */
    private val _currentPlace: MutableLiveData<Place> = MutableLiveData()
    val currentPlace: LiveData<Place> = _currentPlace

    /* List of locations returned from the Places API. */
    private val places = mutableListOf<Place>()

    /**
     * Finds locations near a point using the Places API.
     */
    fun loadNewPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val response = ApiHelper.getNearbyPlaces(latitude, longitude)

            if (response.isSuccessful) {
                places.clear()
                places.addAll(response.body()!!.results)

                nextPlace()
            }
        }
    }

    /**
     * Get the next available location, and remove the previous one.
     */
    fun nextPlace() {
        _currentPlace.value = places[1]
        places.removeAt(0)
    }

    /**
     * If this returns False, then [loadNewPlaces] should be called.
     *
     * @return True if there are more locations ready to be displayed
     */
    fun hasPlaces() = places.size > 1
}