package com.example.weatherapp

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject

import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherApi(
    private val mainActivity: MainActivity,
    private val viewModel: WeatherViewModel,
    private val unit: String,
    private val city: String,
    private val favouriteManager: FavouriteManager
) : AsyncTask<String, Void, String>() {


    val API : String = "94ef87a9d23828a17b8a8202eb185d1b"

    override fun doInBackground(vararg params: String?): String? {
        var response1:String?
        var response2:String?
        try{
            response1 = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=$unit&appid=$API").readText(Charsets.UTF_8)
            response2 = URL("https://api.openweathermap.org/data/2.5/forecast?q=$city&units=$unit&appid=$API").readText(Charsets.UTF_8)


        }catch (e: Exception){
            response1 = null
            response2 = null
        }
        val combinedJson = if (response1 != null && response2 != null) {
            "{ \"weather\": $response1, \"forecast\": $response2 }"
        } else {
            return null
        }
        return combinedJson
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        try {
            if (result != null) {
                val weatherData = parseWeatherData(result)
                Log.d("city", weatherData.city)
                Log.d("citues", favouriteManager.getFavoriteCities().toString())
                if(favouriteManager.isCityFavorite(weatherData.city)){
                    favouriteManager.saveWeatherData(weatherData.city,weatherData)
                    Log.d("Xdd",favouriteManager.getWeatherData(weatherData.city).toString())
                }
                viewModel.setWeatherData(weatherData)
                mainActivity.city = weatherData.city
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun parseWeatherData(jsonString: String): WeatherData {
        val weatherJsonObject = JSONObject(jsonString).getJSONObject("weather")

        val city = weatherJsonObject.getString("name")
        val coord = weatherJsonObject.getJSONObject("coord")
        val latitude = coord.getDouble("lat")
        val longitude = coord.getDouble("lon")
        val weatherArray = weatherJsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val description = weatherObject.getString("description")
        val main = weatherJsonObject.getJSONObject("main")
        val temperature = main.getDouble("temp")
        val pressure = main.getInt("pressure")
        val humidity = main.getInt("humidity")
        val wind = weatherJsonObject.getJSONObject("wind")
        val windSpeed = wind.getDouble("speed")
        val windDeg = wind.getInt("deg")
        val clouds = weatherJsonObject.getJSONObject("clouds")
        val cloudiness = clouds.getInt("all")
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = LocalDateTime.now().format(dateTimeFormat)

        val forecastJsonObject = JSONObject(jsonString).getJSONObject("forecast")
        val forecastList = forecastJsonObject.getJSONArray("list")

        val firstDay = forecastList.getJSONObject(0)
        val forecastDataList = mutableListOf<ForecastData>()
        forecastDataList.add(ForecastData(firstDay.getJSONObject("main").getDouble("temp"), unit, firstDay.getString("dt_txt").substring(0,10)))

        for (i in 1 until forecastList.length()) {
            val forecastObject = forecastList.getJSONObject(i)
            val dateTime = forecastObject.getString("dt_txt")
            if (dateTime.endsWith("12:00:00") && !dateTime.startsWith(firstDay.getString("dt_txt").substring(0,10))) {
                val temp = forecastObject.getJSONObject("main").getDouble("temp")
                val forecastData = ForecastData(temp, unit, dateTime.substring(0,10))
                forecastDataList.add(forecastData)
            }
        }


        return WeatherData(city, latitude, longitude, formattedDateTime, temperature, unit, pressure, description, humidity, windSpeed, windDeg, cloudiness)
    }

}