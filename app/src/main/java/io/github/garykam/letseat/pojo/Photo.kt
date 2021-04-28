package io.github.garykam.letseat.pojo

import com.google.gson.annotations.SerializedName

/**
 * A POJO to map a photo from the Places API.
 */
data class Photo(
    @SerializedName("photo_reference")
    val reference: String
)