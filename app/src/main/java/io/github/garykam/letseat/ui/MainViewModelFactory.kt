package io.github.garykam.letseat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.garykam.letseat.repository.PlacesRepository

class MainViewModelFactory(
    private val placesRepository: PlacesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(placesRepository) as T
        } else {
            throw IllegalArgumentException("Unknown view model class")
        }
    }
}