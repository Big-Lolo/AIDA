package com.example.aida

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.aida.DataBase.Database
import com.example.aida.DataBase.Message
import com.example.aida.DataBase.MessageDao
import com.example.aida.conectivity2model.NetworkUtils
import com.example.aida.serialization.WordsAndClasses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KFunction


typealias SpeechRecognitionCallback = (String) -> Unit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [chat.newInstance] factory method to
 * create an instance of this fragment.
 */
class chat : Fragment(), OnInitListener  {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var messagechat: List<Message> = listOf()
    private var adapter: ChatAdapter = ChatAdapter(messagechat)
    private lateinit var recyclerView: RecyclerView
    private lateinit var db:Database
    private lateinit var messageDao: MessageDao
    private lateinit var modelInterpreter: Interpreter
    private lateinit var predicer: WordsAndClasses
    private lateinit var textToSpeech: TextToSpeech



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //context?.deleteDatabase("message_list") para borrar database
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                modelInterpreter = Interpreter(FileUtil.loadMappedFile(requireContext(), "chatbot_model_lite.tflite"))
                predicer = WordsAndClasses(modelInterpreter, requireContext())
                db = Room.databaseBuilder(requireContext(), Database::class.java, "message_list")
                    .build()
                messageDao = db.messageDao()
                val cal = Calendar.getInstance()
                cal.time = Date()
                cal.add(Calendar.DAY_OF_MONTH,-7)
                messageDao.archiveAll(cal.time)
                messagechat = messageDao.getAll()
                adapter.updateData(messagechat)
                adapter.notifyDataSetChanged()
            }

            withContext(Dispatchers.Main) {
                //recyclerView.smoothScrollToPosition(messagechat.size - 1)
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        textToSpeech = TextToSpeech(requireContext(), this)

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()));
        }
        val ElBoton2 = view?.findViewById<Button>(R.id.btn_send)
        val text2 = view?.findViewById<EditText>(R.id.et_message)
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

        return rootView
    }

    fun ejecutarFuncion(nombreFuncion: String, vararg argumentos: Any, contexte: Context) {
        val instancia = NetworkUtils(contexte)
        val funcion = instancia::class.java.getDeclaredMethod(nombreFuncion, *argumentos.map { it::class.java }.toTypedArray())
        funcion.invoke(instancia, *argumentos)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment chat.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            chat().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ElBoton = view.findViewById<Button>(R.id.btn_send)
        val text = view.findViewById<EditText>(R.id.et_message)

        ElBoton.setOnClickListener {btn_sender()}
        // Inflate the layout for this fragment

        recyclerView = view.findViewById<RecyclerView>(R.id.rv_messages)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter


        text?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    ElBoton?.text = "Enviar"
                } else {
                    ElBoton?.text = "Microfono"
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })



    }

    fun btn_sender(){
        val text = view?.findViewById<EditText>(R.id.et_message)
        val btnSend = view?.findViewById<Button>(R.id.btn_send)

        if (btnSend != null) {
            if (btnSend.text == "Microfono") {
                println("Enviada señal del speech")
                startSpeechToText { voiceText ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            messageDao.insert(Message(
                                    0,
                                    ("#VOZ: $voiceText"),
                                    true,
                                    Date().time.toLong(),
                                    false
                                )
                            )
                            val result = predicer.responseClass(voiceText.toString())
                            val respuesta = result.first
                            val functions = result.second
                            messageDao.insert(Message(0, respuesta, false, Date().time.toLong(), false))
                            val msg = messageDao.getAll()
                            messagechat = msg
                            leerTextoEnVozAlta(respuesta)
                        }
                        withContext(Dispatchers.Main) {
                            adapter.updateData(messagechat)
                            adapter.notifyDataSetChanged()
                            recyclerView.smoothScrollToPosition(messagechat.size - 1)
                        }
                    }

                }


                }
            else {
                if (text != null && text.toString() != "") {
                    var texto = text.text.toString()
                    Log.d("msg_saved", "el texto es ${text.text.toString()}")
                    //messagechat.add(MessageProps(text.text.toString(), true))
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            messageDao.insert(
                                Message(
                                    0,
                                    texto,
                                    true,
                                    Date().time.toLong(),
                                    false
                                )
                            )
                            Log.d("Text2predice", "eefe ${texto}")
                            val result = predicer.responseClass(texto)
                            val respuesta = result.first
                            val functionallamar = result.second?.toString()
                            val clasefuncionName = result.third?.toString()
                            Log.d("FuncionBOT", "LA FUNCION ES $functionallamar")
                            Log.d("FuncionBOT", "LA CLASE ES $clasefuncionName")

                            val argumento1 = context
                            val argumento2 = texto
                            if (functionallamar != null) {
                                if (argumento1 != null) {
                                    functionExecuter(functionallamar, argumento1, argumento2)
                                }
                            }

                            messageDao.insert(Message(0, respuesta, false, Date().time.toLong(), false))
                            val msg = messageDao.getAll()
                            messagechat = msg
                        }
                        withContext(Dispatchers.Main) {
                            adapter.updateData(messagechat)
                            adapter.notifyDataSetChanged()
                            recyclerView.smoothScrollToPosition(messagechat.size - 1)
                        }

                    }
                }
                text?.setText("")
            }

            }

        }




    private fun startSpeechToText(callback: SpeechRecognitionCallback) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
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
                    callback(voiceText)

                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechRecognizerIntent)
    }



    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configuración exitosa, puedes establecer el idioma aquí si es necesario
        } else {
            Log.e("TTS", "Error en la inicialización")
        }
    }

    // Asegúrate de liberar los recursos de TextToSpeech en onDestroyView o en otro lugar adecuado.
    override fun onDestroyView() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroyView()
    }

    // Agrega un método para leer texto en respuesta a un evento (por ejemplo, hacer clic en un botón).
    private fun leerTextoEnVozAlta(texto: String) {
        textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
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

}











