package io.github.garykam.letseat.model

/**
 * A POJO to map the JSON response from the Places API.
 */
data class Places(
    val results: ArrayList<Place>
)