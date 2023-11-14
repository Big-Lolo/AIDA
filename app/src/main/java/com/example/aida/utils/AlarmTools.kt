package com.example.aida.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import com.example.aida.AlarmReceiver

class AlarmTools {





    /*fun getAlarms(context: Context): List<Alarm> {
        val alarms = mutableListOf<Alarm>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarms.add(Alarm(pendingIntent))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val upcomingAlarms = alarmManager.nextAlarmClock
                if (upcomingAlarms != null) {
                    alarms.add(Alarm(upcomingAlarms.triggerTime, upcomingAlarms.showIntent))
                }
            }
        }
        return alarms
    }

    data class Alarm(val triggerTime: Long, val intent: PendingIntent) {
        constructor(pendingIntent: PendingIntent) : this(
            pendingIntent.nextAlarmClock?.triggerTime ?: 0L,
            pendingIntent.intent
        )

        val name: String? by lazy { intent.getStringExtra("name") }
    }*/
    companion object {
        fun setAlarm(context: Context, year: Int, month: Int, day: Int, hour: Int, minute: Int, alarmName: String, toneUri: Uri, volumenLevel: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1) // Los meses comienzan desde 0 (enero es 0)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("alarmName", alarmName) // Agregar el nombre de la alarma
            intent.putExtra("toneUri", toneUri.toString())
            intent.putExtra("volumeLevel", volumenLevel)


            val requestCode = 0 // Puedes usar un código único aquí
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                println("Error try catch $e")
            }
        }
    }
}