package com.example.weatherapp.refresh

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.weatherapp.MainActivity

class DataRefreshScheduler(private val context: Context, private val mainActivity: MainActivity) {

    fun scheduleDataRefresh() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DataRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Harmonogramujemy odświeżanie co 15 minut
        val intervalMillis = 15 * 60 * 1000 // 15 minut w milisekundach
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            intervalMillis.toLong(),
            pendingIntent
        )
    }
}
