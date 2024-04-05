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
    private lateinit var temperatureTextView: TextView
    private lateinit var viewModel: WeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_today, container, false)

        // Inicjalizacja ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        // Znajdź TextView w widoku
        locationTextView = view.findViewById(R.id.location)
        temperatureTextView = view.findViewById(R.id.temp)

        viewModel.getWeatherData().observe(viewLifecycleOwner) { weatherData ->
            locationTextView.text = weatherData.city
            temperatureTextView.text = "${weatherData.temperature}°C"
        }

        return view
    }
}
