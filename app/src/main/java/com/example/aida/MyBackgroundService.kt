package com.example.aida

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log


//Objeto de Información:











class MyBackgroundService : Service() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null




    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BakcgroundServicesss", "Se inicio el servicio backgrounddd")
        val toneUri = intent?.extras?.getParcelable<Uri>("toneUri")
        val volumeLevel = intent?.getIntExtra("volumeLevel", 0)
        val vibration = intent?.getBooleanExtra("vibrate", false)

        ringtone = RingtoneManager.getRingtone(this, toneUri)
        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (volumeLevel != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeLevel, 0)
        }
        ringtone?.play()

        if(vibration == true){
            vibrateDevice()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ringtone?.stop()
        ringtone = null
        stopVibration()
        Log.d("BakcgroundServicesss", "Se ejecuto el stop y destroy del servicio background")
        super.onDestroy()

    }


    private fun vibrateDevice() {
        if (vibrator != null && vibrator?.hasVibrator() == true) {
            // Comprobar si la versión de Android es compatible con el nuevo sistema de vibración
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 200, 300), -1))
            } else {
                // Versiones anteriores a Android Oreo
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 100, 200, 300), -1)
            }
        }
    }

    // Método para detener la vibración
    private fun stopVibration() {
        vibrator?.cancel()
    }
}
