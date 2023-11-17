package com.example.aida.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import com.example.aida.AlarmReceiver
import com.google.gson.Gson

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
                     year: Int,
                     month: Int,
                     day: Int,
                     hour: Int,
                     minute: Int,
                     alarmName: String,
                     toneUri: Uri,
                     volumenLevel: Int,
                     diasRepetirMap: Map<String, Boolean>,
                     dayList:Boolean,
                     vibrate:Boolean,
                     aplazarTime:Int,
                     edit: Boolean = false,
                     identifier : Int = 0
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            //new
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = if(edit){
                identifier
            }else{
                generateUniqueAlarmId(context)
            } //ID UNICA




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
                val alarmDetails = AlarmDetails(
                    requestCode,  // Utilizando el requestCode como ID único
                    alarmName,
                    toneUri.toString(),
                    volumenLevel,
                    vibrate,
                    aplazarTime,
                    year,
                    month,
                    day,
                    dayList,
                    diasRepetirMap,
                    hour,
                    minute
                )
                val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("AlarmID_${requestCode}", alarmDetails.toJson())
                editor.apply()
            } catch (e: SecurityException) {
                println("Error try catch $e")
            }
        }


        fun getAllAlarms(context: Context): List<AlarmDetails> {
            val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
            val alarmKeys = sharedPreferences.all.keys

            val alarmList = mutableListOf<AlarmDetails>()

            for (key in alarmKeys) {
                val alarmJson = sharedPreferences.getString(key, null)
                if (alarmJson != null) {
                    val alarmDetails = AlarmDetails.fromJson(alarmJson)
                    alarmList.add(alarmDetails)
                }
            }

            return alarmList
        }

        fun deactivateAlarm(context: Context, requestCode: Int) {
            val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Obtener la clave asociada con el requestCode
            val key = "alarm_$requestCode"

            // Obtener el JSON almacenado para esa alarma
            val alarmJson = sharedPreferences.getString(key, null)

            // Verificar si la alarma existe
            if (alarmJson != null) {
                // Convertir el JSON a un objeto AlarmDetails
                val alarmDetails = AlarmDetails.fromJson(alarmJson)

                // Modificar la propiedad active
                alarmDetails.active = false

                // Convertir el objeto modificado de nuevo a JSON
                val updatedAlarmJson = alarmDetails?.toJson()

                // Guardar el JSON actualizado de nuevo en SharedPreferences
                editor.putString(key, updatedAlarmJson)
                editor.apply()
            }
        }

        fun activateAlarm(context: Context, requestCode: Int) {
            val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Obtener la clave asociada con el requestCode
            val key = "alarm_$requestCode"

            // Obtener el JSON almacenado para esa alarma
            val alarmJson = sharedPreferences.getString(key, null)

            // Verificar si la alarma existe
            if (alarmJson != null) {
                // Convertir el JSON a un objeto AlarmDetails
                val alarmDetails = AlarmDetails.fromJson(alarmJson)

                // Modificar la propiedad active
                alarmDetails.active = true

                // Convertir el objeto modificado de nuevo a JSON
                val updatedAlarmJson = alarmDetails.toJson()

                // Guardar el JSON actualizado de nuevo en SharedPreferences
                editor.putString(key, updatedAlarmJson)
                editor.apply()
            }
        }

        fun deleteAlarm(context: Context, requestCode: Int) {
            // Desactivar la alarma en el AlarmManager
            cancelAlarm(context, requestCode)

            // Eliminar la entrada de SharedPreferences
            val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Obtener la clave asociada con el requestCode
            val key = "alarm_$requestCode"

            // Borrar la alarma de SharedPreferences
            editor.remove(key)
            editor.apply()
        }

        private fun cancelAlarm(context: Context, requestCode: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE)

            // Cancelar la alarma asociada al requestCode
            alarmManager.cancel(pendingIntent)
        }




    }


}



fun generateUniqueAlarmId(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences("IdPreferences", Context.MODE_PRIVATE)
    val lastGeneratedId = sharedPreferences.getInt("LastGeneratedId", 0)

    // Genera un nuevo ID único
    val newId = lastGeneratedId + 1

    // Guarda el nuevo ID en SharedPreferences
    sharedPreferences.edit().putInt("LastGeneratedId", newId).apply()

    return newId
}

data class AlarmDetails(
    val id: Int,
    val alarmName: String,
    val toneUri: String,
    val volumeLevel: Int,
    val vibrate:Boolean,
    val aplazamiento: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val dayList:Boolean,
    val diasRepetirMap: Map<String, Boolean>,
    val hour: Int,
    val minute: Int,
    var active: Boolean = true
    // Otros detalles de la alarma que necesites
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): AlarmDetails {
            return Gson().fromJson(json, AlarmDetails::class.java)
        }
    }
}

