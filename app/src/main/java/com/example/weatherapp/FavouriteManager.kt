package com.example.weatherapp

import android.content.Context

class FavouriteManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("FavoriteCities", Context.MODE_PRIVATE)

    fun addFavoriteCity(city: String) {
        val cities = getFavoriteCities().toMutableSet()
        cities.add(city)
        sharedPreferences.edit().putStringSet("favoriteCities", cities).apply()
    }

    fun removeFavoriteCity(city: String) {
        val cities = getFavoriteCities().toMutableSet()
        cities.remove(city)
        sharedPreferences.edit().putStringSet("favoriteCities", cities).apply()
    }

    fun getFavoriteCities(): Set<String> {
        return sharedPreferences.getStringSet("favoriteCities", setOf()) ?: setOf()
    }
}