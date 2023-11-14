package com.example.aida

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(contexte: Context?, intent: Intent?) {
        val alarmName = intent?.getStringExtra("alarmName")
        val toneUriString = intent?.getStringExtra("toneUri")
        val volumeLevel = intent?.getIntExtra("volumeLevel", 0) ?: 0

        Log.d("BroadcastReciver", "Context is null: ${contexte == null}")

        if (intent?.action == "TU_ACCION_DESACTIVAR") {
            val intent = Intent(contexte, AlarmFragment::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                contexte, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = contexte?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
        /*if (contexte != null) {
            Log.d("AlarmReciver", "Llamando al checkAlarmSTATE")
            MyBackgroundService.checkAlarmState(contexte)
            Log.d("AlarmReciver", "Llamada finalizada")

        }
        if (contexte != null) {
            val i = Intent(contexte, AlarmActDisable::class.java)
            //i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.d("AlarmReciver2", "StartActivity Realizado")
            contexte.startActivity(i)
            Log.d("AlarmReciver2", "StartActivity Finalizado")

        } else {
            Log.e("CONTEXT_NULL", "El contexto es nulo")
        }*/


        /*val desactivarIntent = Intent(context, AlarmReceiver::class.java)
        desactivarIntent.action = "TU_ACCION_DESACTIVAR" // Definir una acción única
        val desactivarPendingIntent = PendingIntent.getBroadcast(context, 0, desactivarIntent,
            PendingIntent.FLAG_IMMUTABLE)
        val desactivarAction = NotificationCompat.Action.Builder(
            R.drawable.ic_plus, "Desactivar", desactivarPendingIntent
        ).build() */

        val i = Intent(contexte, AlarmActDisable::class.java)
        val pendingIntent = PendingIntent.getActivity(contexte, 0, i, PendingIntent.FLAG_IMMUTABLE)
        val packageName = contexte?.packageName
        val notificationLayout = RemoteViews(packageName, R.layout.activity_alarm_disable)
        val builder = NotificationCompat.Builder(contexte!!, "channelid")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Alarma")
            .setContentText("Esta sonando la alarma $alarmName")
            .setAutoCancel(false)
            //.setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            //.setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout)
            .setFullScreenIntent(pendingIntent, true)

        //.addAction(desactivarAction)


        val toneUri = toneUriString?.let { Uri.parse(it) }
        val audioManager = contexte.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (toneUri != null) {
            val ringtone = RingtoneManager.getRingtone(contexte, toneUri)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeLevel, 0)
            ringtone.play()
        }

        contexte.let { NotificationManagerCompat.from(it) }.notify(123, builder.build())


    }
}
