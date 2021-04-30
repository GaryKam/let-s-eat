package io.github.garykam.letseat.model

import com.google.gson.annotations.SerializedName

/**
 * A POJO to map a photo from the Places API.
 */
data class Photo(
    @SerializedName("photo_reference")
    val reference: String
)