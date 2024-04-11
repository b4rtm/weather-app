package com.example.weatherapp.data

data class WeatherData(

    val city: String,
    val latitude : Double,
    val longitude : Double,
    val time : String,
    val temperature : Double,
    val unit : String,
    val pressure : Int,
    val description : String,
    val icon : String,

    val humidity : Int,
    val windSpeed : Double,
    val windDeg : Int,
    val clouds : Int,
    val forecastData: List<ForecastData>

)