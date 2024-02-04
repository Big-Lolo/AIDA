package com.example.aida

import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import java.util.Locale

class MyInCallService : InCallService() {

    private var textToSpeech: TextToSpeech? = null

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("callInService", "Llamada en ejecución")
        // Iniciar el Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                // Establecer el idioma del Text-to-Speech
                textToSpeech?.language = Locale.getDefault()

                // Leer un texto al interlocutor
                textToSpeech?.speak("Tu mensaje aquí", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("callInService", "Llamada FINALIZADA")

        // Detener el Text-to-Speech y liberar los recursos
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}