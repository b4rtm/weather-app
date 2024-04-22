package com.example.weatherapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel

class WeekForecastFragment : Fragment() {

    private val baseIconUrl = "https://openweathermap.org/img/wn/"

    private lateinit var descriptionImage : ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var viewModel: WeatherViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_week_forecast, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        descriptionImage = view.findViewById(R.id.day1IconImageView)
        temperatureTextView = view.findViewById(R.id.day1TemperatureTextView)
        locationTextView = view.findViewById(R.id.location)

        viewModel.getWeatherData().observe(viewLifecycleOwner) { weatherData ->
            locationTextView.text = weatherData.city


            when (weatherData.unit) {
                "metric" -> temperatureTextView.text = "${weatherData.forecastData.get(0).temperature}°C"
                "standard" -> temperatureTextView.text = "${weatherData.forecastData.get(0).temperature} K"
                "imperial" -> temperatureTextView.text = "${weatherData.forecastData.get(0).temperature}°F"
        }

        Glide.with(this)
            .load(baseIconUrl + weatherData.icon + "@2x.png")
            .override(200, 200)
            .into(descriptionImage)
    }



        return view
    }

}