package com.example.weatherapp

data class WeatherData(

    val city: String,
    val latitude : Double,
    val longitude : Double,
    val time : String,
    val temperature : Double,
    val pressure : Int,
    val description : String,

    val humidity : Int,
    val windSpeed : Double,
    val windDeg : Int,
    val clouds : Int

)