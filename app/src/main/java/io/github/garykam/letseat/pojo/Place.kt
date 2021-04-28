package io.github.garykam.letseat.pojo

import com.google.gson.annotations.SerializedName

/**
 * A POJO to map a location from the Places API.
 */
data class Place(
    @SerializedName("place_id")
    val id: String,
    val name: String,
    val icon: String,
    val photos: ArrayList<Photo>
)