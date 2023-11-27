package com.example.aida

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        startForeground(5, createNotification())
        speechRecognizerManager = SpeechRecognizerManager(this)
        speechRecognizerManager.setSpeechObserver(this)
        speechRecognizerManager.initialize()
        speechRecognizerManager.startListening()

        Log.d("VoiceRecognitionService", "Iniciado el servicio.")
        handler.postDelayed({
            val notificationManager = NotificationManagerCompat.from(this)
            val intent = Intent(this, DummyActivity::class.java)
            val builder = NotificationCompat.Builder(this, "channelid")
                .setSmallIcon(android.R.drawable.arrow_up_float)
                .setContentTitle("Asistente AIDA")
                .setContentText("Asistente AIDA")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE), true)
                notificationManager.notify(6, builder.build())

            val intenter = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intenter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intenter)





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

    private fun openGoogleAssistantFragment() {
        Log.d("AIDAASSISTANT", "MostrandoFragmento de Asistente")
        val intent = Intent(applicationContext, DummyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("fragment_data", "hola")
        startActivity(intent)
    }

}







