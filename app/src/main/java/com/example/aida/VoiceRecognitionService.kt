package com.example.aida

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat

class VoiceRecognitionService : Service() {
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        Log.d("VoiceRecognitionService", "onStartCommand has been called") // Agrega esta línea

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceRecognitionService", "onReadyForSpeech has been called") // Agrega esta línea

                println("Listo para escuchar pana windonguii")

                // Inicia la escucha activa del reconocimiento de voz
                speechRecognizer.startListening(speechRecognizerIntent)
            }

            override fun onBeginningOfSpeech() {
                println("Escuchando conversaciones")
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
            }

            override fun onResults(results: Bundle?) {
                val voiceText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                println(voiceText)
                if (voiceText != null) {
                    if (voiceText.contains("Hola") || voiceText.contains("Estas Aida?")
                        || voiceText.contains("Estas disponible Aida?") || voiceText.contains("Yo te invoco")) {
                        println("Se ha activado la interpretacion!")
                        // Se ha detectado el comando de activación, realiza acciones adicionales
                        // Puedes iniciar otro proceso de reconocimiento de voz aquí
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

            // Implementa otros métodos de RecognitionListener según sea necesario
        })

        val channel = NotificationChannel(
            "running_channel", "Running Notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Aida Is Here")
            .setContentText("Dispuesta a ayudarte!")
            .build()
        notificationManager.createNotificationChannel(channel)
        startForeground(1, notification)




        return START_STICKY
    }
}