package io.github.garykam.letseat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A POJO to map a photo from the Places API.
 */
data class Photo(
    @SerializedName("photo_reference")
    val reference: String
)