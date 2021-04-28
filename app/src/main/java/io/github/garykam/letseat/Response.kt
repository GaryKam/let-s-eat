package io.github.garykam.letseat

/**
 * A POJO to map the JSON response from the Places API.
 */
data class Response(
    val results: ArrayList<Place>
)
