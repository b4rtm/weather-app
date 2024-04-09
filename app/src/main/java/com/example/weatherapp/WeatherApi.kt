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

//    override fun onPreExecute() {
//        super.onPreExecute()
//        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
//        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
//        findViewById<TextView>(R.id.errorText).visibility = View.GONE
//    }

    override fun doInBackground(vararg params: String?): String? {
        var response:String?
        try{
            response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=$unit&appid=$API").readText(
                Charsets.UTF_8
            )
            Log.d("asd", response.toString())
        }catch (e: Exception){
            response = null
        }
        return response
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
        val jsonObject = JSONObject(jsonString)

        val city = jsonObject.getString("name")

        val coord = jsonObject.getJSONObject("coord")
        val latitude = coord.getDouble("lat")
        val longitude = coord.getDouble("lon")

        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val description = weatherObject.getString("description")

        val main = jsonObject.getJSONObject("main")
        val temperature = main.getDouble("temp")
        val pressure = main.getInt("pressure")
        val humidity = main.getInt("humidity")

        val wind = jsonObject.getJSONObject("wind")
        val windSpeed = wind.getDouble("speed")
        val windDeg = wind.getInt("deg")

        val clouds = jsonObject.getJSONObject("clouds")
        val cloudiness = clouds.getInt("all")

        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = LocalDateTime.now().format(dateTimeFormat)

        return WeatherData(city, latitude, longitude, formattedDateTime, temperature, unit, pressure, description, humidity, windSpeed, windDeg, cloudiness)
    }

}