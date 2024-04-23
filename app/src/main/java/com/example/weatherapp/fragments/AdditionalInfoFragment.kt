package com.example.weatherapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel


class AdditionalInfoFragment : Fragment() {

    private lateinit var locationTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var windDegreeTextView: TextView
    private lateinit var cloudsTextView: TextView
    private lateinit var viewModel: WeatherViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_additional_info, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        locationTextView = view.findViewById(R.id.location)
        humidityTextView = view.findViewById(R.id.humidity)
        windSpeedTextView = view.findViewById(R.id.windSpeed)
        windDegreeTextView = view.findViewById(R.id.windDegree)
        cloudsTextView = view.findViewById(R.id.clouds)

        viewModel.getWeatherData().observe(viewLifecycleOwner) { weatherData ->
            locationTextView.text = weatherData.city
            humidityTextView.text = weatherData.humidity.toString() + "%"
            windSpeedTextView.text = weatherData.windSpeed.toString() + " km/h"
            windDegreeTextView.text = weatherData.windDeg.toString() + "°"
            cloudsTextView.text = weatherData.clouds.toString() + "%"
        }


        return view
    }


}