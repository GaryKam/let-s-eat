package io.github.garykam.letseat.repository

import io.github.garykam.letseat.data.remote.PlacesApi
import io.github.garykam.letseat.data.remote.model.Places
import io.github.garykam.letseat.util.ApiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PlacesRepository {
    const val PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/"

    private val placesApi = Retrofit.Builder()
        .baseUrl(PLACES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlacesApi::class.java)

    /**
     * Finds locations near a point using the Places API.
     *
     * @param radius Distance in meters
     */
    suspend fun getNearbyPlaces(
        latitude: Double, longitude: Double, radius: Int
    ): Response<Places> {
        val options = mapOf(
            "location" to "$latitude,$longitude",
            "radius" to "$radius",
            "type" to "restaurant",
            "keyword" to "food",
            "key" to ApiHelper.getApiKey()
        )

        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                placesApi.searchNearby(options).enqueue(object : Callback<Places> {
                    override fun onResponse(
                        call: Call<Places>, response: Response<Places>
                    ) {
                        continuation.resume(response)
                    }

                    override fun onFailure(call: Call<Places>, throwable: Throwable) {
                        continuation.resumeWithException(throwable)
                    }
                })
            }
        }
    }
}