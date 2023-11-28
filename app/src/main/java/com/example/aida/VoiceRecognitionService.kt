package com.example.aida

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import com.example.aida.serialization.WordsAndClasses
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.reflect.KFunction

class VoiceRecognitionService : Service(), SpeechObserver, TextToSpeech.OnInitListener {
    private lateinit var speechRecognizerManager: SpeechRecognizerManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var view: View
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var predicer: WordsAndClasses
    private lateinit var modelInterpreter: Interpreter


    interface SpeechRecognitionCallback {
        fun onTextRecognized(text: String)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Aquí es donde se ejecutará la tarea en segundo plano
        return START_STICKY
    }

    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    override fun onCreate() {
        modelInterpreter = Interpreter(FileUtil.loadMappedFile(this, "chatbot_model_lite.tflite"))
        predicer = WordsAndClasses(modelInterpreter, this)

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

        view = LayoutInflater.from(this).inflate(R.layout.bottomsheetlayouter, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val animation = AnimationUtils.loadAnimation(this, R.anim.progress_animation)
        progressBar.startAnimation(animation)

        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.linearLayout2)

        // Configuración de la animación
        val animator = ValueAnimator.ofInt(0, resources.getDimensionPixelSize(R.dimen.target_height))
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = constraintLayout.layoutParams
            layoutParams.height = value
            constraintLayout.layoutParams = layoutParams
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 1000 // Duración de la animación en milisegundos

        windowManager.addView(view, layoutParams)
        animator.start()

        //Ahora tras desplegar el window, hay que pasar a escucha y seguidamente a interactuar con
        //el usuario.
        VoiceListener()

    }

    private fun VoiceListener() {
        textToSpeech = TextToSpeech(this, this)
        startSpeechToText(object : SpeechRecognitionCallback {
            override fun onTextRecognized(voiceText: String) {
                // Actualizar un TextView
                val textView = view.findViewById<TextView>(R.id.textView9)
                textView.text = voiceText
                // Pasar texto a procesamiento
                val result = predicer.responseClass(voiceText.toString())
                val textoDeRespuesta = result.first
                val functions = result.second
                //Obtener respuesta del procesamiento y dictarla
                textToSpeech.speak(textoDeRespuesta, TextToSpeech.QUEUE_FLUSH, null, null)
                if (functions != null) {
                    functionExecuter(functions)
                }

            }
        })
    }




    private fun startSpeechToText(callback: SpeechRecognitionCallback) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                println("Listo para grabar")
            }

            override fun onBeginningOfSpeech() {
                println("Empezando a hablar...")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                println("Finalizacion de la grabacion")
            }

            override fun onError(error: Int) {}

            override fun onResults(results: Bundle?) {
                val voiceText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                if (!voiceText.isNullOrBlank()) {
                    // Hacer algo con el texto reconocido, como mostrarlo en un EditText.
                    println("Texto Reconocido: $voiceText")
                    callback.onTextRecognized(voiceText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val voiceText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                Log.d("SpeechRecognitionsss", "Partial result: $voiceText")

                if (!voiceText.isNullOrBlank()) {
                    val textView = view.findViewById<TextView>(R.id.textView9)
                    textView.text = voiceText
                }
            }


            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun functionExecuter(functionName: String){
        try {
            val functionsContainer = FunctionsContainer()

            // Obtener la referencia a la función mediante reflexión
            val function = FunctionsContainer::class.members
                .filterIsInstance<KFunction<*>>()
                .firstOrNull { it.name == functionName }

            // Verificar si se encontró la función
            if (function != null) {
                // Llamar a la función
                function.call(functionsContainer)
            } else {
                println("La función $functionName no fue encontrada")
            }
        } catch (e: Exception) {
            println("Error al ejecutar la función $functionName: ${e.message}")
        }
    }

    override fun onInit(status: Int) {

    }

}







