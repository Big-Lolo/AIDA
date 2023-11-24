package com.example.aida

import android.app.Service
import android.content.Intent
import android.os.IBinder



class ServiceReciver : Service(), SpeechObserver {
    private lateinit var speechRecognizerManager: SpeechRecognizerManager

    override fun onCreate() {
        super.onCreate()
        speechRecognizerManager = SpeechRecognizerManager(this)
        speechRecognizerManager.setSpeechObserver(this)
        speechRecognizerManager.initialize()
        speechRecognizerManager.startListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        speechRecognizerManager.stopListening()
        super.onDestroy()
    }

    override fun onSpeechDetected() {
        openGoogleAssistantFragment()
    }

    private fun openGoogleAssistantFragment() {
        //val fragmentManager = fragmentManager
        //val assistantFragment = AssistantFragment()
        //assistantFragment.show(fragmentManager, "AssistantFragment")
    }
}