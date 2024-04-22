package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel

class TodayFragment : Fragment() {

    private lateinit var locationTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionImage : ImageView
    private lateinit var viewModel: WeatherViewModel

    private val baseIconUrl = "https://openweathermap.org/img/wn/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        locationTextView = view.findViewById(R.id.location)
        temperatureTextView = view.findViewById(R.id.temp)
        latitudeTextView = view.findViewById(R.id.latitude)
        longitudeTextView = view.findViewById(R.id.longitude)
        timeTextView = view.findViewById(R.id.time)
        pressureTextView = view.findViewById(R.id.pressure)
        descriptionTextView = view.findViewById(R.id.description)
        descriptionImage = view.findViewById(R.id.description_image)

        viewModel.getWeatherData().observe(viewLifecycleOwner) { weatherData ->
            locationTextView.text = weatherData.city
            when (weatherData.unit) {
                "metric" -> temperatureTextView.text = "${weatherData.temperature}°C"
                "standard" -> temperatureTextView.text = "${weatherData.temperature} K"
                "imperial" -> temperatureTextView.text = "${weatherData.temperature}°F"
            }
            latitudeTextView.text = "${weatherData.latitude}"
            longitudeTextView.text = "${weatherData.longitude}"
            timeTextView.text = weatherData.time
            pressureTextView.text = "${weatherData.pressure} hPa"
            descriptionTextView.text = weatherData.description

            Glide.with(this)
                .load(baseIconUrl + weatherData.icon + "@2x.png")
                .override(400, 400)
                .into(descriptionImage)
        }

        return view
    }
}
