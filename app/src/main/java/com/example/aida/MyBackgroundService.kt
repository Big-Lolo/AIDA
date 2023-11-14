package com.example.aida

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log





//Objeto de Información:











class MyBackgroundService : Service() {

    companion object {
        fun checkAlarmState(context: Context) {
            Log.d("checkAlarmState", "Llamada recibida")

            val i = Intent(context, AlarmActDisable::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            Log.d("checkAlarmState", "START REALIZADO")

        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BackgroundService", "Service BACKGROUND INICIADO")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Realiza cualquier limpieza necesaria aquí
    }
}