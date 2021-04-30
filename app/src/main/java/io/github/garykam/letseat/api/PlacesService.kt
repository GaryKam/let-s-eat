package io.github.garykam.letseat.api

import io.github.garykam.letseat.model.Places
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface PlacesService {
    @GET("nearbysearch/json")
    fun searchNearby(
        @QueryMap options: Map<String, String>
    ): Call<Places>
}