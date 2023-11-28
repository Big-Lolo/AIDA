package com.example.aida

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationCompat
class VoiceRecognitionService : Service(), SpeechObserver {
    private lateinit var speechRecognizerManager: SpeechRecognizerManager
    private val handler = Handler(Looper.getMainLooper())


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Aquí es donde se ejecutará la tarea en segundo plano
        return START_STICKY
    }

    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    override fun onCreate() {

        super.onCreate()
        //startForeground(5, createNotification())
        speechRecognizerManager = SpeechRecognizerManager(this)
        speechRecognizerManager.setSpeechObserver(this)
        speechRecognizerManager.initialize()
        speechRecognizerManager.startListening()

        Log.d("VoiceRecognitionService", "Iniciado el servicio.")
        handler.postDelayed({









        }, 10000)

    }

    private fun createNotification(): Notification {
        val channelId = "AssistantAida"
        val channelName = "My Channel"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Assistant Service")
            .setContentText("Iniciado el servicio de Asiséncia de Aida.")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return notification
    }

    override fun onSpeechDetected() {
        openGoogleAssistantFragment()
    }

    @SuppressLint("MissingPermission", "InflateParams")
    private fun openGoogleAssistantFragment() {
        Log.d("AIDAASSISTANT", "Mostrando WindowManager del Asistente")

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null)
        windowManager.addView(view, layoutParams)


    }

}







