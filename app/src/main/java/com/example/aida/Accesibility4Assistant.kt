package com.example.aida
import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent



class Accesibility4Assistant : AccessibilityService() {
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(this, MyGestureListener())
    }
    private var tapCount = 0
    private var lastTapTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("ETIQUETA_ACCESIBILITY", "tag es ${event.eventType}")
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val view = event.source
            if (view != null && view.className == "android.widget.ImageButton" && view.contentDescription == "Botón Central") {
                // Se detectó el clic en el botón central del Bottom Navigation Bar
                // Realiza la acción deseada aquí, como abrir el asistente de AIDA
                Log.d("assistantButton", "Abrir el asistente de AIDA")
            }
        }
    }


    override fun onInterrupt() {
        // Implementación opcional si se interrumpe el servicio de accesibilidad
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private var lastTapTime: Long = 0

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastTapTime

            if (elapsedTime < 500) {
                // Se detectó una doble pulsación rápida
                // Realizar la acción deseada aquí
                // Por ejemplo, iniciar una llamada telefónica


                //ABRIR EL ASISTENTE DE AIDA

                // Reiniciar el contador de tiempo
                lastTapTime = 0
            } else {
                // Es una doble pulsación, pero no es lo suficientemente rápida
                lastTapTime = currentTime
            }

            return super.onDoubleTap(e)
        }
    }
}