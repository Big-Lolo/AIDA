package com.example.aida

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class AlarmActDisable : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ACTIVIDAD_DISABLE", "Actividad ejecutandose11111")
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_alarm_disable) // Asocia el layout XML a esta actividad

        Log.d("ACTIVIDAD_DISABLE", "Actividad ejecutandose")

    }
}
