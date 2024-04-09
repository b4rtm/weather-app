package com.example.weatherapp

import android.content.Context
import org.json.JSONObject


class FavouriteManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("FavoriteCities", Context.MODE_PRIVATE)

    fun addFavoriteCity(city: String) {
        val cities = getFavoriteCities().toMutableSet()
        cities.add(city)

        sharedPreferences.edit().putStringSet("favoriteCities", cities).apply()
    }

    fun isCityFavorite(city: String): Boolean {
        return getFavoriteCities().contains(city)
    }

    fun removeFavoriteCity(city: String) {
        val cities = getFavoriteCities().toMutableSet()
        cities.remove(city)
        sharedPreferences.edit().putStringSet("favoriteCities", cities).apply()
    }

    fun getFavoriteCities(): Set<String> {
        return sharedPreferences.getStringSet("favoriteCities", setOf()) ?: setOf()
    }

    fun saveWeatherData(city: String, weatherData: WeatherData) {
        val jsonObject = JSONObject()
        jsonObject.put("city", weatherData.city)
        jsonObject.put("latitude", weatherData.latitude)
        jsonObject.put("longitude", weatherData.longitude)
        jsonObject.put("time", weatherData.time)
        jsonObject.put("temperature", weatherData.temperature)
        jsonObject.put("unit", weatherData.unit)
        jsonObject.put("pressure", weatherData.pressure)
        jsonObject.put("description", weatherData.description)
        jsonObject.put("humidity", weatherData.humidity)
        jsonObject.put("windSpeed", weatherData.windSpeed)
        jsonObject.put("windDeg", weatherData.windDeg)
        jsonObject.put("clouds", weatherData.clouds)

        sharedPreferences.edit().putString(city, jsonObject.toString()).apply()
    }

    fun getWeatherData(city: String): WeatherData? {
        val jsonString = sharedPreferences.getString(city, null)
        return if (jsonString != null) {
            val jsonObject = JSONObject(jsonString)
            WeatherData(
                jsonObject.getString("city"),
                jsonObject.getDouble("latitude"),
                jsonObject.getDouble("longitude"),
                jsonObject.getString("time"),
                jsonObject.getDouble("temperature"),
                jsonObject.getString("unit"),
                jsonObject.getInt("pressure"),
                jsonObject.getString("description"),
                jsonObject.getInt("humidity"),
                jsonObject.getDouble("windSpeed"),
                jsonObject.getInt("windDeg"),
                jsonObject.getInt("clouds")
            )
        } else {
            null
        }
    }
}