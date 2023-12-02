package com.example.aida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class PowerOffReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.example.aida.ACTION_POWER_OFF") {
            //Apagar dispositivo
            val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)

        }else if (intent?.action == "com.example.aida.ACTION_RESTART_IT"){
            //Reiniciar dispositivo
            val intent = Intent(Intent.ACTION_REBOOT)
            intent.putExtra("nowait", 1)
            intent.putExtra("interval", 1)
            intent.putExtra("window", 0)
            context?.sendBroadcast(intent)
        }
    }
}