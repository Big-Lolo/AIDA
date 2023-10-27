package com.example.aida

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceRecognitionService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // El servicio está listo para escuchar
            }

            override fun onBeginningOfSpeech() {
                TODO("Not yet implemented")
            }

            override fun onRmsChanged(rmsdB: Float) {
                TODO("Not yet implemented")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                TODO("Not yet implemented")
            }

            override fun onEndOfSpeech() {
                TODO("Not yet implemented")
            }

            override fun onError(error: Int) {
                TODO("Not yet implemented")
            }

            override fun onResults(results: Bundle?) {
                val voiceText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                println(voiceText)
                if (voiceText != null) {
                    if (voiceText.contains("Hola Aida") || voiceText.contains("Estas Aida?")
                        || voiceText.contains("Estas disponible Aida?") || voiceText.contains("Yo te invoco")) {
                        println("Se ha activado la interpretacion!")
                        // Se ha detectado el comando de activación, realiza acciones adicionales
                        // Puedes iniciar otro proceso de reconocimiento de voz aquí
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }

            // Implementa otros métodos de RecognitionListener según sea necesario
        })

        // Resto del código...

        return START_STICKY
    }

}