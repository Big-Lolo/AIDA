package com.example.aida.conectivity2model

import android.app.Application
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.roundToInt

class Utilities {
    companion object{

        fun bulbState(context: Context, text:String){
            Log.d("NewStatus", "Estas en el bulbState")
            val lowercaseSentence = text.lowercase()

            if (containsAnyKeyword(lowercaseSentence, arrayOf("activar", "activame", "actívame", "encender", "iluminar", "iluminame", "habilitar", "enciende", "enciendeme"))) {
                toggleTorch(context, true)

            } else if (containsAnyKeyword(lowercaseSentence, arrayOf("desactivar", "apagar", "inhabilitar", "oscurecer", "apaga", "apagame", "desactiva", "desactivame", "deshabilita", "deshabilitame"))) {
                toggleTorch(context, false)

            } else {
                // Acción no reconocida
                Toast.makeText(context, "Acción no reconocida para la linterna", Toast.LENGTH_SHORT).show()
            }

        }

        private fun toggleTorch(context: Context, enable: Boolean) {
            Log.d("TogleTorch", "Estas en el togletorch")

            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, enable)
            } catch (e: CameraAccessException) {
                Log.e("ERRORSS", "El error es $e")
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
                    val volumeType = extractVolumeType(lowercaseSentence)
                    adjustVolume(context, audioManager, AudioManager.ADJUST_RAISE, percentage, volumeType)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("baja el volumen", "disminuye el volumen", "bajar volumen", "disminuir volumen", "disminuye volumen", "baja volumen", "reduce el volumen", "reduce volumen")) -> {
                    val percentage = extractPercentage(lowercaseSentence)
                    val volumeType = extractVolumeType(lowercaseSentence)
                    adjustVolume(context, audioManager, AudioManager.ADJUST_LOWER, percentage, volumeType)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("modo vibracion", "modo vibración", "activa la vibracion", "activa vibración")) -> {
                    setRingerMode(context, audioManager, AudioManager.RINGER_MODE_VIBRATE)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("modo silencio", "mutea", "silencia", "mute")) -> {
                    setRingerMode(context, audioManager, AudioManager.RINGER_MODE_SILENT)
                }
                else -> {
                    // Si no se especifica un tipo de volumen, ajustar el volumen de multimedia por defecto
                    adjustVolume(context, audioManager, AudioManager.ADJUST_RAISE, extractPercentage(lowercaseSentence), AudioManager.STREAM_MUSIC)
                }
            }
        }

        private fun extractVolumeType(sentence: String): Int {
            return when {
                containsAnyKeyword(sentence, arrayOf("multimedia", "musica")) -> AudioManager.STREAM_MUSIC
                containsAnyKeyword(sentence, arrayOf("notificaciones")) -> AudioManager.STREAM_NOTIFICATION
                containsAnyKeyword(sentence, arrayOf("alarma", "alarmas")) -> AudioManager.STREAM_ALARM
                containsAnyKeyword(sentence, arrayOf("llamada", "llamadas", "tono")) -> AudioManager.STREAM_RING
                else -> AudioManager.STREAM_MUSIC
            }
        }

        fun controlBrightness(context: Context, sentence: String) {
            val lowercaseSentence = sentence.lowercase()

            when {
                containsAnyKeyword(lowercaseSentence, arrayOf("sube el brillo", "aumenta el brillo", "sube brillo")) -> {
                    adjustBrightness(context, true)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("baja el brillo", "disminuye el brillo", "baja brillo", "baja el brillo")) -> {
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
                checkWriteSettingsPermission(context)
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

                // Mostrar un mensaje, puedes personalizar esto según tus necesidades
                Toast.makeText(context, "Brillo ajustado a $newBrightness", Toast.LENGTH_SHORT).show()

            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al ajustar el brillo", Toast.LENGTH_SHORT).show()
            }
        }

        private fun checkWriteSettingsPermission(context: Context) {
            if (Settings.System.canWrite(context)) {
                // Ya tienes permisos, puedes llamar directamente a la función adjustBrightness
                adjustBrightness(context, true)
            } else {
                // No tienes permisos, solicita el permiso al usuario
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)

                // Asegúrate de que el contexto sea un contexto de aplicación
                if (context is Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            }
        }


        fun controlPower(context: Context, sentence: String) {
            val lowercaseSentence = sentence.lowercase()

            when {
                containsAnyKeyword(lowercaseSentence, arrayOf("apagar la pantalla", "suspender la pantalla", "suspende pantalla", "cierra pantalla", "cierrame la pantalla", "pantalla",
                    "suspendeme la pantalla", "apagame la pantalla", "suspende la pantalla", "apaga la pantalla", )) -> {
                    Log.d("SUSPENDER_PANTALLA", "orden recibida")

                    turnOffScreen(context)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("apagar dispositivo", "apaga el móvil", "apaga móvil", "apágame el móvil", "apagame el movil", "apagame el dispositivo")) -> {
                    powerOffDevice(context)
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("reinicia el dispositivo", "reinicia móvil", "reiniciame el móvil", "reiniciame el dispositivo", "reiniciar", "reiniciame")) -> {
                    restartDevice(context)
                }
                else -> {
                    // Acción no reconocida
                    Log.d("acctionNrecognized", "La accion no ha sido reconocida")
                    Toast.makeText(context, "Acción no reconocida para el control de energía", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun turnOffScreen(context: Context) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            if (powerManager.isInteractive) {
                // Apagar la pantalla solo si la pantalla está encendida
                val windowManager = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                val display = windowManager.defaultDisplay

                if (display.state == Display.STATE_ON) {
                    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

                    if (devicePolicyManager.isAdminActive(ComponentName(context, DeviceAdminReceiver::class.java))) {
                        // Si es un administrador de dispositivos activo, apagar la pantalla
                        devicePolicyManager.lockNow()
                    } else {
                        // Si no es un administrador de dispositivos activo, mostrar un mensaje de error
                        Log.d("turnOffScreen", "El administrador de dispositivos no esta activo")

                        Toast.makeText(context, "Necesitas activar el administrador de dispositivos para apagar la pantalla", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Mostrar un mensaje de error si la versión de Android no es compatible o la pantalla está apagada
                Toast.makeText(context, "No se pudo apagar la pantalla", Toast.LENGTH_SHORT).show()
            }
        }

        private fun powerOffDevice(context: Context) {
            val intent = Intent("com.example.aida.ACTION_POWER_OFF")
            context.sendBroadcast(intent)
        }

        private fun restartDevice(context: Context) {
            val intent = Intent("com.example.aida.ACTION_RESTART_IT")
            context.sendBroadcast(intent)
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
        private fun adjustVolume(context: Context, audioManager: AudioManager, direction: Int, percentage: Int, streamType: Int) {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val currentVolume = audioManager.getStreamVolume(streamType)
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

            // Ajustar el volumen actual para el flujo de audio especificado
            audioManager.setStreamVolume(streamType, newVolume, 0)

            // Mostrar un mensaje, puedes personalizar esto según tus necesidades
            val volumeType = when (streamType) {
                AudioManager.STREAM_RING -> "llamadas"
                AudioManager.STREAM_ALARM -> "alarmas"
                AudioManager.STREAM_NOTIFICATION -> "notificaciones"
                else -> "sonido"
            }

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