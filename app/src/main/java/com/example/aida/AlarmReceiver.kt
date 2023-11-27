package com.example.aida

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aida.utils.AlarmTools.Companion.getAllAlarms


class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(contexte: Context?, intent: Intent?) {




        val allAlarms = contexte?.let { getAllAlarms(it) }

        if (allAlarms != null) {
            for (alarm in allAlarms) {
                println("Alarm ID: ${alarm.id}")
                println("Name: ${alarm.alarmName}")
                println("Tone URI: ${alarm.toneUri}")
                println("Volume Level: ${alarm.volumeLevel}")
                println("-------------")
            }
        }


        val alarmName = intent?.getStringExtra("alarmName")
        val toneUriString = intent?.getStringExtra("toneUri")
        val volumeLevel = intent?.getIntExtra("volumeLevel", 0) ?: 0
        val vibrate = intent?.getBooleanExtra("vibrate", false)
        Log.d("BroadcastReciver", "Context is null: ${contexte == null}")
        val keyguardManager = contexte?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isScreenLocked = keyguardManager.isKeyguardLocked
        val powerManager = contexte.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive
        Log.d("BroadcastReciver", "La pantalla esta $isScreenOn")
        if(isScreenLocked or !isScreenOn) {

            if (vibrate != null) {
                contexte.showNotificationWithFullScreenIntent(true, contextex = contexte, toneUriString = toneUriString.toString(), volumeLevel = volumeLevel, vibrate = vibrate)
            }

        } else {
            if (vibrate != null) {
                contexte.showNotificationWithFullScreenIntent(contextex = contexte, toneUriString = toneUriString.toString(), volumeLevel = volumeLevel, vibrate = vibrate)
            }
        }

        if (intent?.action == "TU_ACCION_DESACTIVAR") {
            val intent = Intent(contexte, AlarmFragment::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                contexte, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )


            val alarmManager = contexte.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val serviceIntent = Intent(contexte, MyBackgroundService::class.java)
            contexte.stopService(serviceIntent)
            alarmManager.cancel(pendingIntent)
            val notificationManager = contexte.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(123)

        }

    }


    private fun Context.getFullScreenIntent(isLockScreen: Boolean): PendingIntent {
        Log.d("GetFullScreed", "El valor de locked es $isLockScreen")

        val destination = if (isLockScreen)
            AlarmActDisable::class.java

        else
            AlarmActDisable::class.java
        val intent = Intent(this, destination)

        // flags and request code are 0 for the purpose of demonstration
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }



    private val LOCK_SCREEN_KEY = "lockScreenKey"


    @SuppressLint("MissingPermission")
    private fun Context.showNotificationWithFullScreenIntent(
        isLockScreen: Boolean = false,
        title: String = "Title",
        description: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        contextex : Context,
        toneUriString: String,
        volumeLevel:Int,
        vibrate:Boolean


    ) {

        //Botones de als noitificaciones:
        val action1 = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_share,
            "Desactivar",
            desactivarAlarma(contextex)
        ).build()

// Crear una acción para el segundo botón
        val action2 = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Aplazar",
            aplazarAlarma(contextex)
        ).build()





        val builder = NotificationCompat.Builder(this, "channelid")
            .setSmallIcon(android.R.drawable.arrow_up_float)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(getFullScreenIntent(isLockScreen), true)
        if (!isLockScreen) {
            builder.addAction(action1)
            builder.addAction(action2)
        }


        contextex.let { NotificationManagerCompat.from(it) }.notify(123, builder.build())

        val toneUri = toneUriString.let { Uri.parse(it) }

        if (toneUri != null) {
            //ringtone = RingtoneManager.getRingtone(contextex, toneUri)
            //val audioManager = contextex.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            //audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeLevel, 0)
            //ringtone?.play()
            val serviceIntent = Intent(contextex, MyBackgroundService::class.java)
            serviceIntent.putExtra("toneUri", toneUri)
            serviceIntent.putExtra("volumeLevel", volumeLevel)
            serviceIntent.putExtra("vibrate", vibrate)

            contextex.startService(serviceIntent)

        }

    }

    private fun desactivarAlarma(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "TU_ACCION_DESACTIVAR"

        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun aplazarAlarma(context: Context): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "TU_ACCION_APLAZARR"

        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }


    companion object {



    }







}












