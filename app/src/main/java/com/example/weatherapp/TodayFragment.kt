package com.example.weatherapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

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
            temperatureTextView.text = "${weatherData.temperature}°C"
            latitudeTextView.text = "${weatherData.latitude}"
            longitudeTextView.text = "${weatherData.longitude}"
            timeTextView.text = weatherData.time
            preassureTextView.text = "${weatherData.pressure}"
            descriptionTextView.text = weatherData.description
        }

        return view
    }
}
