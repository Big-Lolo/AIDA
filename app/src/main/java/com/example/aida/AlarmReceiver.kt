package com.example.aida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_DEACTIVATE_ALARM") {
            // Lógica para desactivar la alarma
            Toast.makeText(context, "Alarma desactivada", Toast.LENGTH_SHORT).show()
        }
    }
}
