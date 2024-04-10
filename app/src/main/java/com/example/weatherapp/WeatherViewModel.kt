package com.example.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.data.WeatherData

class WeatherViewModel : ViewModel() {

    private val weatherData = MutableLiveData<WeatherData>()

    fun getWeatherData(): LiveData<WeatherData> {
        return weatherData
    }

    fun setWeatherData(data: WeatherData) {
        weatherData.value = data
    }

}