package com.mockify.app.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mockify.app.extensions.equalTo
import com.mockify.app.service.LocationHelper
import com.mockify.app.ui.models.LocationEntry
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StorageManager {
    private const val PREF = "gpsprefs"
    private const val KEY_HISTORY = "history"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_LOGIN = "islogin"

    private lateinit var pref: SharedPreferences

    /**
     * Initializes the SharedPreferences instance.
     * Must be called before accessing any stored data.
     *
     * @param context The application context.
     */
    fun initialise(context: Context) {
        pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the login status from SharedPreferences.
     *
     * @return True if the user is logged in, false otherwise.
     */
    fun getLogin(): Boolean {
        return pref.getBoolean(KEY_LOGIN, false) // Default to false if not set
    }

    /**
     * Sets the login status in SharedPreferences.
     *
     * @param isLoggedIn True to mark the user as logged in, false otherwise.
     */
    fun setLogin(isLoggedIn: Boolean) {
        pref.edit {
            putBoolean(KEY_LOGIN, isLoggedIn)
            commit()
        }
    }

    /**
     * Retrieves the latest known location from the history.
     *
     * @return The last saved LatLng or the default location if the history is empty.
     */
    fun getLatestLocation(): LatLng {
        return locationHistory.lastOrNull() ?: LocationHelper.DEFAULT_LOCATION
    }

    /**
     * Adds a new location to the history if it is not already present.
     *
     * @param latLng The location to add.
     */
    fun addLocationToHistory(latLng: LatLng) {
        val tempHistory = locationHistory
        if (!locationHistory.contains(latLng)) {
            tempHistory.add(latLng)
            locationHistory = tempHistory
        }
    }

    /**
     * Toggles the favorite status for a specific location entry.
     * If the entry is a favorite, it will be removed. Otherwise, it will be added.
     *
     * @param locationEntry The location entry to toggle.
     */
    fun toggleFavoriteForPosition(locationEntry: LocationEntry) {
        if (containsFavoriteEntry(locationEntry))
            removeFavorite(locationEntry)
        else
            addLocationToFavorites(locationEntry)
    }

    /**
     * Adds a location entry to the favorites list.
     *
     * @param locationEntry The location entry to add.
     */
    private fun addLocationToFavorites(locationEntry: LocationEntry) {
        val tempFavorites = favorites
        tempFavorites.add(locationEntry)
        favorites = tempFavorites
    }

    /**
     * Removes a location entry from the favorites list.
     *
     * @param locationEntry The location entry to remove.
     */
    private fun removeFavorite(locationEntry: LocationEntry) {
        val tempFavorites = favorites
        tempFavorites.removeIf { it.latLng.equalTo(locationEntry.latLng) }
        favorites = tempFavorites
    }

    /**
     * Checks if a location entry is in the favorites list.
     *
     * @param locationEntry The location entry to check.
     * @return True if the entry is a favorite, false otherwise.
     */
    fun containsFavoriteEntry(locationEntry: LocationEntry): Boolean {
        return favorites.any { it.latLng.equalTo(locationEntry.latLng) }
    }

    // Private property for location history
    private var locationHistory: MutableList<LatLng>
        get() {
            val json = pref.getString(KEY_HISTORY, "[]")
            val typeToken = object : TypeToken<MutableList<LatLng>>() {}.type
            return Gson().fromJson(json, typeToken)
        }
        private set(value) {
            val json = Gson().toJson(value)
            pref.edit {
                putString(KEY_HISTORY, json)
                commit()
            }
        }

    // Public property for favorites list
    var favorites: MutableList<LocationEntry>
        get() {
            val json = pref.getString(KEY_FAVORITES, "[]")
            val typeToken = object : TypeToken<MutableList<LocationEntry>>() {}.type
            return Gson().fromJson(json, typeToken)
        }
        private set(value) {
            val json = Gson().toJson(value)
            pref.edit {
                putString(KEY_FAVORITES, json)
                commit()
            }
        }

}