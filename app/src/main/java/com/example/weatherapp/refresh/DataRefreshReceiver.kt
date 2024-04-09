package com.example.weatherapp.refresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weatherapp.MainActivity

class DataRefreshReceiver(private val mainActivity: MainActivity) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        mainActivity.fetchDataFromApi("metric", mainActivity.city)
    }
}
