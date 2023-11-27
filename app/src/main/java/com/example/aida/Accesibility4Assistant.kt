package com.example.aida
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class Accesibility4Assistant(): AccessibilityService() {


        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            Log.d("ACCESIBILITYLOG", "AIXO S'ESTÀ EXECUTANT")
            // Detectar el evento de la notificación específica
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                // Obtener la información de la notificación
                val packageName = event.packageName
                val tickerText = event.text[0]

                // Verificar si es tu propia notificación
                if (packageName != null && packageName == "com.example.aida" &&
                    tickerText != null && tickerText == "Asistente AIDA") {
                    // Abrir el fullScreenIntent de la notificación
                    if (event.parcelableData != null && event.parcelableData is Notification) {
                        val notification = event.parcelableData as Notification
                        val fullScreenIntent = notification.fullScreenIntent
                        try {
                            fullScreenIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        override fun onInterrupt() {
            // Método requerido pero no utilizado en este ejemplo
        }

}