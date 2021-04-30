package io.github.garykam.letseat.data.remote.model

/**
 * A POJO to map the JSON response from the Places API.
 */
data class Places(
    val results: ArrayList<Place>
)