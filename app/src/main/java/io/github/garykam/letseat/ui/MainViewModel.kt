package io.github.garykam.letseat.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.garykam.letseat.data.remote.model.Place
import io.github.garykam.letseat.repository.PlacesRepository
import io.github.garykam.letseat.util.ApiHelper
import kotlinx.coroutines.launch

class MainViewModel(
    private val placesRepository: PlacesRepository
) : ViewModel() {
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
            val response = placesRepository.getNearbyPlaces(latitude, longitude)

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
        if (hasPlaces()) {
            _currentPlace.value = places[1]
            places.removeAt(0)
        }
    }

    /**
     * @return True if there are more locations ready to be displayed
     */
    fun hasPlaces() = places.size > 1

    /**
     * @param photoReference Used in the photo endpoint in Places API
     * @return The photo URL
     */
    fun getImageUrl(photoReference: String): String {
        return "${placesRepository.PLACES_BASE_URL}photo?maxwidth=1600" +
                "&photoreference=$photoReference&key=${ApiHelper.getApiKey()}"
    }
}