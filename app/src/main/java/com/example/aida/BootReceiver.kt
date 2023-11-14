package com.example.aida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("BOOTRECIVER", "BOOT RECIVE DETECTADO")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, MyBackgroundService::class.java)
            context?.startService(serviceIntent)
        }
    }
}