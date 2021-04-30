package io.github.garykam.letseat.data.remote

import io.github.garykam.letseat.data.remote.model.Places
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface PlacesApi {
    @GET("nearbysearch/json")
    fun searchNearby(
        @QueryMap options: Map<String, String>
    ): Call<Places>
}