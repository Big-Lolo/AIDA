package com.example.aida.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import com.example.aida.AlarmReceiver

class AlarmTools {

    companion object {

        private fun obtenerCalendarDay(nombreDia: String): Int? {
            return when (nombreDia) {
                "Lunes" -> Calendar.MONDAY
                "Martes" -> Calendar.TUESDAY
                "Miércoles" -> Calendar.WEDNESDAY
                "Jueves" -> Calendar.THURSDAY
                "Viernes" -> Calendar.FRIDAY
                "Sábado" -> Calendar.SATURDAY
                "Domingo" -> Calendar.SUNDAY
                else -> null
            }
        }

        @SuppressLint("ScheduleExactAlarm")
        fun setAlarm(context: Context,
                     year: Int, month: Int,
                     day: Int,
                     hour: Int,
                     minute: Int,
                     alarmName: String,
                     toneUri: Uri,
                     volumenLevel: Int,
                     diasRepetirMap: Map<String, Boolean>,
                     dayList:Boolean
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            //new
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = 0 // Puedes usar un código único aquí
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)


            val calendar = Calendar.getInstance().apply {
                //set(Calendar.YEAR, year)
                //set(Calendar.MONTH, month - 1) // Los meses comienzan desde 0 (enero es 0)
                //set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (dayList){
                for ((dia, seleccionado) in diasRepetirMap) {
                    if (seleccionado) {
                        val calendarDay = obtenerCalendarDay(dia)

                        if (calendarDay != null) {
                            calendar.set(Calendar.DAY_OF_WEEK, calendarDay)
                            val timeInMillis = calendar.timeInMillis

                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                }


            }else{
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1) // Los meses comienzan desde 0 (enero es 0)
                calendar.set(Calendar.DAY_OF_MONTH, day)
            }


            intent.putExtra("alarmName", alarmName) // Agregar el nombre de la alarma
            intent.putExtra("toneUri", toneUri.toString())
            intent.putExtra("volumeLevel", volumenLevel)



            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } catch (e: SecurityException) {
                println("Error try catch $e")
            }
        }
    }


}