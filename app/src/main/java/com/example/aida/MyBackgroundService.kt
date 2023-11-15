package com.example.aida

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.util.Log


//Objeto de Información:











class MyBackgroundService : Service() {
    private var ringtone: Ringtone? = null




    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BakcgroundServicesss", "Se inicio el servicio backgrounddd")
        val toneUri = intent?.extras?.getParcelable<Uri>("toneUri")
        val volumeLevel = intent?.getIntExtra("volumeLevel", 0)
        ringtone = RingtoneManager.getRingtone(this, toneUri)
        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (volumeLevel != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeLevel, 0)
        }
        ringtone?.play()


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ringtone?.stop()
        ringtone = null
        Log.d("BakcgroundServicesss", "Se ejecuto el stop y destroy del servicio background")
        super.onDestroy()

    }
}