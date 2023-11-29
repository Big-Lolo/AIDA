package com.example.aida.conectivity2model

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast
import kotlin.math.roundToInt

class Utilities {
    companion object{

        fun bulbState(context: Context, text:String){
            val lowercaseSentence = text.lowercase()

            if (containsAnyKeyword(lowercaseSentence, arrayOf("activar", "encender", "iluminar", "habilitar"))) {
                toggleTorch(context, true)
            } else if (containsAnyKeyword(lowercaseSentence, arrayOf("desactivar", "apagar", "inhabilitar", "oscurecer"))) {
                toggleTorch(context, false)
            } else {
                // Acción no reconocida
                Toast.makeText(context, "Acción no reconocida para la linterna", Toast.LENGTH_SHORT).show()
            }

        }

        private fun toggleTorch(context: Context, enable: Boolean) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, enable)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al cambiar el estado de la linterna", Toast.LENGTH_SHORT).show()
            }
        }


        fun volumenStates(context: Context, sentence: String) {
            val lowercaseSentence = sentence.lowercase()

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when {
                containsAnyKeyword(lowercaseSentence, arrayOf("sube el volumen", "aumenta el volumen", "sube volumen", "aumenta volumen", "incrementa volumen", "incrementa el volumen")) -> {
                    val percentage = extractPercentage(lowercaseSentence)
                    adjustVolume(context, audioManager, AudioManager.ADJUST_RAISE, percentage)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("baja el volumen", "disminuye el volumen", "bajar volumen", "dismenuir volumen", "disminuye volumen", "baja volumen", "reduce el volumen", "reduce volumen")) -> {
                    val percentage = extractPercentage(lowercaseSentence)
                    adjustVolume(context, audioManager, AudioManager.ADJUST_LOWER, percentage)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("modo vibracion", "modo vibración", "activa la vibracion", "activa vibración")) -> {
                    setRingerMode(context, audioManager, AudioManager.RINGER_MODE_VIBRATE)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("modo silencio", "mutea", "silencia", "mute")) -> {
                    setRingerMode(context, audioManager, AudioManager.RINGER_MODE_SILENT)
                }
                else -> {
                    // Acción no reconocida
                    Toast.makeText(context, "Acción no reconocida para el volumen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun controlBrightness(context: Context, sentence: String) {
            val lowercaseSentence = sentence.toLowerCase()

            when {
                containsAnyKeyword(lowercaseSentence, arrayOf("sube el brillo", "aumenta el brillo")) -> {
                    adjustBrightness(context, true)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("baja el brillo", "disminuye el brillo")) -> {
                    adjustBrightness(context, false)
                }
                else -> {
                    // Acción no reconocida
                    Toast.makeText(context, "Acción no reconocida para el brillo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun adjustBrightness(context: Context, increase: Boolean) {
            try {
                val contentResolver = context.contentResolver
                val brightnessMode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)

                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    // Desactivar el modo automático de brillo si está activado
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                }

                val currentBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                val maxBrightness = 255

                var newBrightness = currentBrightness

                if (increase && currentBrightness < maxBrightness) {
                    newBrightness = minOf(currentBrightness + 25, maxBrightness)
                } else if (!increase && currentBrightness > 0) {
                    newBrightness = maxOf(currentBrightness - 25, 0)
                }

                // Ajustar el brillo de la pantalla
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness)

                // Reflejar los cambios en la pantalla
                val window = context as? Activity)?.window
                val layoutParams = window?.attributes
                layoutParams?.screenBrightness = newBrightness / maxBrightness.toFloat()
                window?.attributes = layoutParams

                // Mostrar un mensaje, puedes personalizar esto según tus necesidades
                Toast.makeText(context, "Brillo ajustado a $newBrightness", Toast.LENGTH_SHORT).show()

            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al ajustar el brillo", Toast.LENGTH_SHORT).show()
            }
        }

















//Herramientas para las funciones

        private fun containsAnyKeyword(text: String, keywords: Array<String>): Boolean {
            return keywords.any { keyword -> containsKeyword(text, keyword) }
        }

        private fun containsKeyword(text: String, keyword: String): Boolean {
            return text.contains(keyword, ignoreCase = true)
        }
        private fun extractPercentage(sentence: String): Int {
            val percentageRegex = Regex("(\\d+)\\s*%")
            val matchResult = percentageRegex.find(sentence)

            return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 10
        }
        private fun adjustVolume(context: Context, audioManager: AudioManager, direction: Int, percentage: Int) {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            var newVolume = currentVolume

            if (percentage > 0) {
                // Calcular el volumen ajustado
                newVolume = when (direction) {
                    AudioManager.ADJUST_RAISE -> {
                        val targetVolume = (currentVolume + (maxVolume * percentage / 100.0)).roundToInt()
                        minOf(targetVolume, maxVolume)
                    }
                    AudioManager.ADJUST_LOWER -> {
                        val targetVolume = (currentVolume - (maxVolume * percentage / 100.0)).roundToInt()
                        maxOf(targetVolume, 0)
                    }
                    else -> currentVolume
                }
            }

            // Ajustar el volumen actual
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

            // Mostrar un mensaje, puedes personalizar esto según tus necesidades
            val volumeType = "sonido"
            Toast.makeText(context, "Volumen de $volumeType ajustado al $newVolume", Toast.LENGTH_SHORT).show()
        }

        private fun setRingerMode(context: Context, audioManager: AudioManager, ringerMode: Int) {
            // Cambiar el modo de timbre (normal, vibración, silencio)
            audioManager.ringerMode = ringerMode

            // Mostrar un mensaje, puedes personalizar esto según tus necesidades
            val ringerModeText = getRingerModeText(context, ringerMode)
            Toast.makeText(context, "Modo de timbre cambiado a $ringerModeText", Toast.LENGTH_SHORT).show()
        }

        private fun getVolumeType(context: Context, ringerMode: Int): String {
            return when (ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> "sonido"
                AudioManager.RINGER_MODE_VIBRATE -> "vibración"
                AudioManager.RINGER_MODE_SILENT -> "silencio"
                else -> "desconocido"
            }
        }

        private fun getRingerModeText(context: Context, ringerMode: Int): String {
            return when (ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> "Normal"
                AudioManager.RINGER_MODE_VIBRATE -> "Vibración"
                AudioManager.RINGER_MODE_SILENT -> "Silencio"
                else -> "Desconocido"
            }
        }


    }
}