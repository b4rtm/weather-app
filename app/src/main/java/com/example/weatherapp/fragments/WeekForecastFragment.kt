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

    private lateinit var locationTextView: TextView
    private lateinit var viewModel: WeatherViewModel

    data class DayViews(
        val iconImageView: ImageView,
        val dayTextView: TextView,
        val temperatureTextView: TextView
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_week_forecast, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)

        locationTextView = view.findViewById(R.id.location)

        val dayViews = arrayOf(
            DayViews(
                view.findViewById(R.id.day1IconImageView),
                view.findViewById(R.id.day1TextView),
                view.findViewById(R.id.day1TemperatureTextView)
            ),
            DayViews(
                view.findViewById(R.id.day2IconImageView),
                view.findViewById(R.id.day2TextView),
                view.findViewById(R.id.day2TemperatureTextView)
            ),
            DayViews(
                view.findViewById(R.id.day3IconImageView),
                view.findViewById(R.id.day3TextView),
                view.findViewById(R.id.day3TemperatureTextView)
            ),
            DayViews(
                view.findViewById(R.id.day4IconImageView),
                view.findViewById(R.id.day4TextView),
                view.findViewById(R.id.day4TemperatureTextView)
            ),
            DayViews(
                view.findViewById(R.id.day5IconImageView),
                view.findViewById(R.id.day5TextView),
                view.findViewById(R.id.day5TemperatureTextView)
            )
        )

        viewModel.getWeatherData().observe(viewLifecycleOwner) { weatherData ->
            locationTextView.text = weatherData.city

            dayViews.forEachIndexed { index, (iconImageView, dayTextView, temperatureTextView) ->
                val dayForecast = weatherData.forecastData[index]

                dayTextView.text = dayForecast.date
                when (weatherData.unit) {
                    "metric" -> temperatureTextView.text = "${dayForecast.temperature}°C"
                    "standard" -> temperatureTextView.text = "${dayForecast.temperature} K"
                    "imperial" -> temperatureTextView.text = "${dayForecast.temperature}°F"
                }

                Glide.with(this)
                    .load(baseIconUrl + dayForecast.icon + "@2x.png")
                    .override(200, 200)
                    .into(iconImageView)
            }
        }

        return view
    }

}