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
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import com.example.aida.serialization.WordsAndClasses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.reflect.KFunction


interface SpeechRecognitionControl {
    fun pauseRecognition()
    fun resumeRecognition()
    fun restartRecognition()
}

class VoiceRecognitionService : Service(), SpeechObserver, TextToSpeech.OnInitListener, SpeechRecognitionControl  {
    private lateinit var speechRecognizerManager: SpeechRecognizerManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var view: View
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var predicer: WordsAndClasses
    private lateinit var modelInterpreter: Interpreter
    private var visualizer: Visualizer? = null
    private lateinit var contextoService:Context
    private var isRecognitionPaused:Boolean = true
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var opened = false
    private lateinit var audioManager: AudioManager
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d("MICROPHONE_ON", "El microfono esta disponible nuevamente")
                resumeRecognition()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d("MICROPHONE_OFF", "El microfono no esta disponible actualmente")

                // Perdiste el foco del audio, debes pausar o detener tu servicio aquí
            }
            // Otros casos de cambios en el foco del audio que puedas manejar
        }
    }



    interface SpeechRecognitionCallback {
        fun onTextRecognized(text: String)
    }

    inner class VoiceRecognitionBinder : Binder() {
        fun getService(): VoiceRecognitionService {
            return this@VoiceRecognitionService
        }
    }
    override fun pauseRecognition() {
        // Lógica para pausar el reconocimiento de voz
        Log.d("pauseRecognition", "PAUSA, EL ESTADO ES  $isRecognitionPaused")

        isRecognitionPaused = true
        Log.d("pauseRecognition", "PAUSA, EL ESTADO ES  $isRecognitionPaused")

        // Detener el reconocimiento de voz actual
        speechRecognizerManager.stopListening()
    }

    override fun resumeRecognition() {
        // Lógica para reanudar el reconocimiento de voz
        Log.d("resumeRecognition", "El estado del paused es $isRecognitionPaused")
        isRecognitionPaused = false
        // Iniciar el reconocimiento de voz nuevamente si no está pausado
        if (!isRecognitionPaused) {
            speechRecognizerManager.startListening()
        }
    }

    override fun restartRecognition() {
        speechRecognizerManager = SpeechRecognizerManager(contextoService)
        speechRecognizerManager.setSpeechObserver(this)
        speechRecognizerManager.initialize()
        speechRecognizerManager.startListening()
        isRecognitionPaused = false
    }

    override fun onBind(intent: Intent): IBinder? {
        // Devolver una instancia de Binder personalizado
        return VoiceRecognitionBinder()
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
        //handler.postDelayed({
        //    this.openGoogleAssistantFragment()
       // }, 10000)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("abracadable", "Innto the rithm")
        } else {
            Log.d("abracadable", "OUTTERRR the rithm")

            // No se pudo obtener el foco de audio, el micrófono no está disponible
        }

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
        if(!opened) {
            openGoogleAssistantFragment()
        }else{
            //Activa el microfono del text-to-speech
            VoiceListener()
        }
    }

    private fun openGoogleAssistantFragment() {
        opened = true
        Log.d("AIDAASSISTANT", "Mostrando WindowManager del Asistente")
        speechRecognizerManager.stopListening()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
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
                opened = false
                true
            } else {
                false
            }
        }
        //Listener del boton de enviar / microfono
        val ElBoton2 = view?.findViewById<Button>(R.id.btn_sendss)
        view?.findViewById<Button>(R.id.btn_sendss)?.setOnClickListener { action_button() }

        val text2 = view?.findViewById<EditText>(R.id.et_messagess)
        text2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    ElBoton2?.text = "Enviar"
                } else {
                    ElBoton2?.text = "Microfono"
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })


        // Configuración de la animación
        val animator =
            ValueAnimator.ofInt(0, resources.getDimensionPixelSize(R.dimen.target_height))
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
        textToSpeech = TextToSpeech(this, this)

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
                Log.d("results", "${result}")
                val textoDeRespuesta = result.first
                val functions = result.second
                //Obtener respuesta del procesamiento y dictarla
                textToSpeech.speak(textoDeRespuesta, TextToSpeech.QUEUE_FLUSH, null, null)
                if (functions != null) {
                    functionExecuter(functions, this@VoiceRecognitionService , textoDeRespuesta)
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

            override fun onEndOfSegmentedSession() {
                Log.d("NODETECTION", "No se reconocio nada")
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

    private fun functionExecuter(functionName: String, vararg args: Any){
        try {
            val functionsContainer = FunctionsContainer()

            // Obtener la referencia a la función mediante reflexión
            val function = FunctionsContainer::class.members
                .filterIsInstance<KFunction<*>>()
                .firstOrNull { it.name == functionName }

            // Verificar si se encontró la función
            if (function != null) {
                // Llamar a la función
                function.call(functionsContainer, *args)
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


    private fun action_button(){
        val btnSend = view.findViewById<Button>(R.id.btn_sendss)
        val text = view.findViewById<EditText>(R.id.et_messagess)

        if (btnSend != null) {
            Log.d("texter", "btnsend diferente de null")

            if (btnSend.text == "Microfono"){
                VoiceListener()
                Log.d("texter", "microfono")

            }else{
                if (text != null && text.toString() != "") {
                    var texto = text.text.toString()
                    Log.d("msg_saved", "el texto es ${text.text.toString()}")
                    //messagechat.add(MessageProps(text.text.toString(), true))
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            /*messageDao.insert(
                                Message(
                                    0,
                                    texto,
                                    true,
                                    Date().time.toLong(),
                                    false
                                )
                            )*/

                            val result = predicer.responseClass(texto)
                            val respuesta = result.first


                            val functions = result.second
                            if (functions != null) {
                                functionExecuter(functions, this@VoiceRecognitionService , texto.toString())
                            }

                            // Llamada a speak dentro de withContext para asegurar que respuesta tiene el valor correcto
                            withContext(Dispatchers.Main) {
                                textToSpeech.speak(respuesta, TextToSpeech.QUEUE_FLUSH, null, null)
                            }

                            //messageDao.insert(Message(0, respuesta, false, Date().time.toLong(), false))
                            //val msg = messageDao.getAll()
                            //messagechat = msg
                        }
                        /*withContext(Dispatchers.Main) {
                            adapter.updateData(messagechat)
                            adapter.notifyDataSetChanged()
                            recyclerView.smoothScrollToPosition(messagechat.size - 1)
                        }*/

                    }
                }
                Log.d("texter", "Eliminar texto")
                text?.setText("")


                //logica de enviar el texto y responder con audio y texto.
                //Mostrar texto en un minirecuadro sobre el window.
                //Este recuadro se resetea cada sesion, es decir, no cargamos datos.
            }
        }
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

