package io.github.garykam.letseat.pojo

/**
 * A POJO to map the JSON response from the Places API.
 */
data class PlacesResponse(
    val results: ArrayList<Place>
)