package io.github.garykam.letseat.utils

import io.github.garykam.letseat.api.PlacesService
import io.github.garykam.letseat.model.Places
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ApiHelper {
    const val PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    private val placesService: PlacesService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(PLACES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create Places API service.
        placesService = retrofit.create(PlacesService::class.java)

        // Allow API key to be read from cpp file.
        System.loadLibrary("native-lib")
    }

    /**
     * Finds locations near a point using the Places API.
     */
    suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double
    ): Response<Places> {
        val options = mapOf(
            "location" to "$latitude,$longitude",
            "radius" to "16093",
            "type" to "restaurant",
            "keyword" to "food",
            "key" to getApiKey()
        )

        return suspendCoroutine { continuation ->
            placesService.searchNearby(options).enqueue(object : Callback<Places> {
                override fun onResponse(
                    call: Call<Places>,
                    response: Response<Places>
                ) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call<Places>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    /**
     * @return The API key to make Places API requests
     */
    external fun getApiKey(): String
}