package io.github.garykam.letseat

import io.github.garykam.letseat.pojo.PlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface PlacesService {
    @GET("nearbysearch/json")
    fun searchNearby(
        @QueryMap options: Map<String, String>
    ): Call<PlacesResponse>
}