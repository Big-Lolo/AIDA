package com.example.aida

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
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
    private var visualizer: Visualizer? = null
    private lateinit var contextoService:Context

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
        startForeground(5, createNotification())
        speechRecognizerManager = SpeechRecognizerManager(this)
        speechRecognizerManager.setSpeechObserver(this)
        speechRecognizerManager.initialize()
        speechRecognizerManager.startListening()
        contextoService = this

        Log.d("VoiceRecognitionService", "Iniciado el servicio.")
        handler.postDelayed({
            this.openGoogleAssistantFragment()








        }, 10000)

    }

    private fun createNotification(): Notification {
        val channelId = "AssistantAida"
        val channelName = "My Channel"

        //val notificationIntent = Intent(this, MainActivity::class.java)
        //val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
          //  PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Assistant Service")
            .setContentText("Iniciado el servicio de Asiséncia de Aida.")
            .setSmallIcon(R.drawable.logo)
            //.setContentIntent(pendingIntent)
            .build()

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return notification
    }

    override fun onSpeechDetected() {
        openGoogleAssistantFragment()
    }


    private fun openGoogleAssistantFragment() {
        Log.d("AIDAASSISTANT", "Mostrando WindowManager del Asistente")
        speechRecognizerManager.stopListening()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        view = LayoutInflater.from(this).inflate(R.layout.bottomsheetlayouter, null)
        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.linearLayout2)
        val rootView = view.findViewById<ClickableConstraintLayout>(R.id.granContainer)

        rootView.setOnClickListener { view ->
            val x = view.x.toInt()
            val y = view.y.toInt()

            val location = IntArray(2)
            constraintLayout.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + constraintLayout.width
            val bottom = top + constraintLayout.height

            val event = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                x.toFloat(),
                y.toFloat(),
                0
            )

            if (event.action == MotionEvent.ACTION_UP && (x < left || x > right || y < top || y > bottom)) {
                windowManager.removeView(view)
                speechRecognizerManager.setSpeechObserver(this)
                speechRecognizerManager.startListening()
                true
            } else {
                false
            }
        }




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
                speechRecognizerManager = SpeechRecognizerManager(contextoService)
                speechRecognizerManager.initialize()
                speechRecognizerManager.startListening()



            }
        })
    }




    private fun startSpeechToText(callback: SpeechRecognitionCallback) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                val mediaPlayer = MediaPlayer()
                val audioSessionId = mediaPlayer.audioSessionId
                val visualizerBar = view.findViewById<View>(R.id.visualizer_bar)
                val handler = Handler(Looper.getMainLooper())
                if (visualizer == null) {
                    visualizer = Visualizer(audioSessionId)


                    // Configurar el tamaño del buffer para la captura de audio
                    visualizer?.setCaptureSize(Visualizer.getCaptureSizeRange()[1])

                    // Configurar el listener para recibir los datos de audio
                    visualizer?.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            // Aquí puedes procesar los datos de audio y actualizar la barra
                            // La variable waveform contiene los datos de audio en formato PCM

                            // Calcular el nivel de amplitud promedio del audio
                            val amplitude = waveform?.average()?.toInt() ?: 0

                            // Calcular el ancho de la barra en función del nivel de amplitud
                            val barWidth = amplitude * visualizerBar.width / 32768

                            // Actualizar el ancho y el color de la barra
                            handler.post {
                                // Actualizar el tamaño y el color de la barra según la amplitud
                                val layoutParams = visualizerBar.layoutParams
                                layoutParams.width = amplitude
                                visualizerBar.layoutParams = layoutParams

                                val color = getColorForAmplitude(amplitude)
                                visualizerBar.setBackgroundColor(color)
                            }
                        }

                        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            // Este método no es necesario para este caso, pero se debe implementar
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)

                    // Habilitar la captura de audio
                    visualizer?.enabled = true
                }
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

    fun getColorForAmplitude(amplitude: Int): Int {
        // Definir los umbrales de amplitud para cada color
        val redThreshold = 100
        val yellowThreshold = 50

        // Determinar el color según la amplitud
        return when {
            amplitude >= redThreshold -> Color.RED
            amplitude >= yellowThreshold -> Color.YELLOW
            else -> Color.GREEN
        }
    }

    override fun onInit(status: Int) {

    }

}





class ClickableConstraintLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x.toInt()
            val y = event.y.toInt()

            if (x < 0 || x > width || y < 0 || y > height) {
                // Cerrar la ventana aquí
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(this)
                return true
            }
        }

        return super.onTouchEvent(event)
    }
}

