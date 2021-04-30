package io.github.garykam.letseat.util

object ApiHelper {
    init {
        // Allow API key to be read from cpp file.
        System.loadLibrary("native-lib")
    }

    /**
     * @return The API key to make Places API requests
     */
    external fun getApiKey(): String
}