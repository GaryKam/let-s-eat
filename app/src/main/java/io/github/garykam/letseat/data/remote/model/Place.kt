package io.github.garykam.letseat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A POJO to map a location from the Places API.
 */
data class Place(
    @SerializedName("place_id")
    val id: String,
    val name: String,
    val photos: ArrayList<Photo>
)