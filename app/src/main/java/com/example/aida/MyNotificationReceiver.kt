package com.example.aida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NOTIFICATIONRECIVER", "RECIBIDA LA NOTIFICACIÓN")
        // Aquí puedes realizar acciones cuando se recibe la notificación
        // Por ejemplo, abrir tu aplicación

        val launchIntent = Intent(context, MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(launchIntent)
    }
}