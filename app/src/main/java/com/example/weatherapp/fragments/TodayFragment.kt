package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel

class TodayFragment : Fragment() {

    private lateinit var locationTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var preassureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var temperatureTextView: TextView

    private lateinit var viewModel: WeatherViewModel

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
        preassureTextView = view.findViewById(R.id.pressure)
        descriptionTextView = view.findViewById(R.id.description)

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
            preassureTextView.text = "${weatherData.pressure}"
            descriptionTextView.text = weatherData.description
        }

        return view
    }
}