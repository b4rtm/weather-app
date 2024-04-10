package com.example.weatherapp

import android.content.Context
import android.widget.ImageButton
import org.json.JSONArray
import org.json.JSONObject


class FavouriteManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("FavoriteCities", Context.MODE_PRIVATE)

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

        val jsonArray = JSONArray()

        weatherData.forecastData.forEach { forecastData ->
            val jsonObject1 = JSONObject()
            jsonObject1.put("temperature", forecastData.temperature)
            jsonObject1.put("unit", forecastData.unit)
            jsonObject1.put("date", forecastData.date)
            jsonArray.put(jsonObject1)
        }

        jsonObject.put("forecastData", jsonArray)

        sharedPreferences.edit().putString(city, jsonObject.toString()).apply()
    }

    fun getWeatherData(city: String): WeatherData? {
        val jsonString = sharedPreferences.getString(city, null)
        return if (jsonString != null) {
            val jsonObject = JSONObject(jsonString)
            val forecastDataList = getForecastDataList(jsonObject)

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
                jsonObject.getInt("clouds"),
                forecastDataList
            )
        } else {
            null
        }
    }

    private fun getForecastDataList(jsonObject: JSONObject): MutableList<ForecastData> {
        val jsonArray =
            jsonObject.getJSONArray("forecastData") // Podaj nazwę tablicy z twojego JSON
        val forecastDataList = mutableListOf<ForecastData>()

        for (i in 0 until jsonArray.length()) {
            val forecastObject = jsonArray.getJSONObject(i)

            val temperature = forecastObject.getDouble("temperature") // Pobierasz temperaturę
            val unit = forecastObject.getString("unit") // Pobierasz jednostkę
            val date = forecastObject.getString("date") // Pobierasz datę

            val forecastData = ForecastData(temperature, unit, date) // Tworzysz obiekt ForecastData
            forecastDataList.add(forecastData) // Dodajesz obiekt do listy
        }
        return forecastDataList
    }

    fun setFavourite(
        favouriteManager: FavouriteManager,
        city: String,
        buttonAddFavorites: ImageButton
    ) {
        val isFavorite = favouriteManager.isCityFavorite(city)
        if (isFavorite) {
            buttonAddFavorites.setImageResource(R.drawable.favorite_gold)
        } else {
            buttonAddFavorites.setImageResource(R.drawable.favorite)
        }
    }

    public fun saveWeatherDataForFavouriteCities(
        favouriteManager: FavouriteManager,
        mainActivity: MainActivity
    ) {
        val favouriteCities = favouriteManager.getFavoriteCities()
        for (cityItem in favouriteCities) {
            mainActivity.fetchDataFromApi("metric", cityItem)
        }
    }
}