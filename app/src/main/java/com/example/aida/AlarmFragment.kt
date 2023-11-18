package com.example.aida

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aida.utils.AlarmTools.Companion.setAlarm
import com.google.gson.Gson
import java.sql.Date
import java.text.SimpleDateFormat

class AlarmFragment(private val submenu: Boolean = false) : Fragment(), OnItemClickListener {
    private var fechaSeleccionada: Date? = null
    private var dateString:String? = null
    private var selectedAlarmTone: Uri? = null
    private var diasSemanaMap = mutableMapOf<String, Boolean>(
        "Lunes" to false,
        "Martes" to false,
        "Miércoles" to false,
        "Jueves" to false,
        "Viernes" to false,
        "Sábado" to false,
        "Domingo" to false
    )
    private var dayList : Boolean = false
    private var listener: Home.OnHomeInteractionListener? = null
    private var yearCache: Int = 0
    private var monthCache: Int = 0
    private var dayCache: Int = 0
    private var typeofalarm: String? = null
    private var uriAlarm: Uri? = null
    private var volumeAlarma: Int = 50


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmFragment", "onCreate")

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (savedInstanceState != null) {
            var miDato = savedInstanceState.getString("miDato", "")
            Log.d("DATO_RECUPERADO", "El dato recuperado es: $miDato")
        }
        // Inflar el diseño del fragmento (por ejemplo, un fondo blanco)
        return inflater.inflate(R.layout.alarm_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Recuperar cache y actualizar datos previos.
        if(submenu){
            //Obtener cache si estaba previamente en submenu
            val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val alarmConfigJson = sharedPreferences.getString("AlarmConfig", null)
            val alarmCacheInf = alarmConfigJson?.let {
                AlarmCache.fromJson(
                    it,
                    AlarmCache::class.java
                )
            }

            //Asignar información
            if (alarmCacheInf != null) {
                fechaSeleccionada = alarmCacheInf.fechaSeleccionada
            }
            if (alarmCacheInf != null) {
                dateString = alarmCacheInf.dateString
            }
            if (alarmCacheInf != null) {
                diasSemanaMap = alarmCacheInf.diasSemanaMap as MutableMap<String, Boolean>
            }
            val nombre = alarmCacheInf?.alarmName
            val hours = alarmCacheInf?.hour
            val minutes = alarmCacheInf?.minute
            dayCache = alarmCacheInf?.day!!
            monthCache = alarmCacheInf?.month!!
            yearCache = alarmCacheInf?.year!!
            dayList = alarmCacheInf.dayList
            typeofalarm = alarmCacheInf.typeofalarm
            uriAlarm = Uri.parse(alarmCacheInf.toneUri) //convertir string a uri
            volumeAlarma = alarmCacheInf.volumeLevel
            val vibrate = alarmCacheInf?.vibrate
            val campoNombre =  view.findViewById<EditText>(R.id.eventss)
            campoNombre.setText(nombre)
            val timePicker = view.findViewById<TimePicker>(R.id.datePicker1) //
            timePicker.setIs24HourView(true)
            val hour: Int? = hours
            val minute: Int? = minutes
            if (Build.VERSION.SDK_INT >= 23) {
                if (hour != null) {
                    timePicker.hour = hour
                }
                if (minute != null) {
                    timePicker.minute = minute
                }
            }
            val switchVibracion= view.findViewById<SwitchCompat>(R.id.vibrateSwitch)
            if (vibrate != null) {
                switchVibracion.isChecked = vibrate
            }
            if(dayList) {
                val diasSemanaIds = intArrayOf(
                    R.id.lunes,
                    R.id.martes,
                    R.id.miercoles,
                    R.id.jueves,
                    R.id.viernes,
                    R.id.sabado,
                    R.id.domingo
                )
                for ((index, entry) in diasSemanaMap.entries.withIndex()) {
                    val dia = entry.key
                    val estaActivo = entry.value
                    if (estaActivo) {
                        val idTextView = diasSemanaIds[index]

                        // Cambiar el color del TextView asociado.
                        val textView = view.findViewById<TextView>(idTextView)
                        textView.setTextColor(resources.getColor(R.color.colorPrimary))
                    }
                }
            }
            val text = view.findViewById<TextView>(R.id.textView3111)
            text.text = dateString

            val sharedPreferencess = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPreferencess.edit()
            editor.remove("AlarmConfig") //Eliminamos el AlarmConfig tras volver al lobby y haber cargado toda la informacion requerida.
            editor.apply()
        }








        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                listener?.onReturn2Home()
                remove()
            }
        })




        // Encuentra el TimePicker por su ID
        val picker: TimePicker = view.findViewById(R.id.datePicker1)
        picker.setIs24HourView(true)



        val buttonSelectDate: Button = view.findViewById(R.id.calendarpk)

        val diasSemanaIds = intArrayOf(R.id.lunes, R.id.martes, R.id.miercoles, R.id.jueves, R.id.viernes, R.id.sabado, R.id.domingo)

        fun obtenerDiasSeleccionados(): String {
            val diasSeleccionados = StringBuilder()

            for ((dia, activo) in diasSemanaMap) {
                if (activo) {
                    val tresPrimerasLetras = dia.take(3) // Tomar solo las 3 primeras letras del día
                    diasSeleccionados.append("$tresPrimerasLetras, ")
                }
            }

            if (diasSeleccionados.isNotEmpty()) {
                diasSeleccionados.delete(diasSeleccionados.length - 2, diasSeleccionados.length)
            }
            val diasString = diasSeleccionados.toString()
            dayList = true
            return if (diasString.isNotEmpty()) {
                "Cada $diasString"

            } else {
                ""
            }
        }

        fun showDatePickerDialog(vieww: View) {
            val calendarHoy = Calendar.getInstance()
            val datePicker = context?.let {
                DatePickerDialog(
                    it,
                    { view, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth)
                        fechaSeleccionada = Date(selectedCalendar.timeInMillis)
                        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
                        val formattedDate = simpleDateFormat.format(fechaSeleccionada)

                        val text = vieww.findViewById<TextView>(R.id.textView3111)
                        dayList = false
                        restartListDay()
                        if (selectedCalendar.get(Calendar.YEAR) == calendarHoy.get(Calendar.YEAR) &&
                            selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendarHoy.get(
                                Calendar.DAY_OF_YEAR
                            )) {
                            dateString = "Hoy"
                            text.text = "Hoy"

                        } else {
                            calendarHoy.add(Calendar.DAY_OF_YEAR, 1)
                            if (selectedCalendar.get(Calendar.YEAR) == calendarHoy.get(Calendar.YEAR) &&
                                selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendarHoy.get(
                                    Calendar.DAY_OF_YEAR
                                )) {

                                dateString = "Mañana"
                                text.text = "Mañana"

                            }else {
                                dateString = formattedDate
                                text.text = formattedDate
                            }
                        }



                    },
                    // Configura la fecha actual como fecha predeterminada
                    if (submenu) yearCache else calendarHoy.get(Calendar.YEAR),
                    if (submenu) (monthCache - 1) else calendarHoy.get(Calendar.MONTH),
                    if (submenu) dayCache else calendarHoy.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePicker?.datePicker?.minDate = calendarHoy.timeInMillis
            datePicker?.show()
        }


        buttonSelectDate.setOnClickListener {
            // Al hacer clic, abre el selector de fecha
            showDatePickerDialog(view)
        }

        for (id in diasSemanaIds) {
            val textView = view.findViewById<TextView>(id)
            val dia = when (id) {
                R.id.lunes -> "Lunes"
                R.id.martes -> "Martes"
                R.id.miercoles -> "Miércoles"
                R.id.jueves -> "Jueves"
                R.id.viernes -> "Viernes"
                R.id.sabado -> "Sábado"
                R.id.domingo -> "Domingo"
                else -> ""
            }
            textView.setOnClickListener {
                if (textView.currentTextColor == context?.let { it1 -> ContextCompat.getColor(it1, R.color.black) }) {
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    diasSemanaMap[dia] = true
                } else {
                    // El color actual del texto NO es negro
                    textView.setTextColor(resources.getColor(R.color.black)) // Cambiar a negro u otro color según sea necesario
                    diasSemanaMap[dia] = false
                }
                val sentenceDays = obtenerDiasSeleccionados()
                val text = view.findViewById<TextView>(R.id.textView3111)
                if (sentenceDays!= ""){
                    dateString = sentenceDays
                    text.text = sentenceDays
                } else {
                    dateString = "Hoy"
                    text.text = "Hoy"
                }
            }
        }


        //Boton selector de alarma
        val selectToneButton = view.findViewById<Button>(R.id.selectToneButtons)
        selectToneButton.setOnClickListener {
            selectAlarmTone(view)
        }

        //Boton de guardar / cancelar. El de guardar crea alarma y el de cancelar vuelve atrás.
        val cancelButton = view.findViewById<Button>(R.id.btnCancelar)
        val guardarButton = view.findViewById<Button>(R.id.btnGuardar)


        guardarButton.setOnClickListener {
            val timePicker = view.findViewById<TimePicker>(R.id.datePicker1) // Reemplaza R.id.timePicker con tu ID real
            val hour: Int
            val minute: Int

            if (Build.VERSION.SDK_INT >= 23) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour // Obsoleto a partir del nivel de API 23
                minute = timePicker.currentMinute // Obsoleto a partir del nivel de API 23
            }

            val switchVibration = view.findViewById<SwitchCompat>(R.id.vibrateSwitch)
            var VibrationState = switchVibration.isChecked

            val nameAlarm = view.findViewById<EditText>(R.id.eventss)
            var nombre = nameAlarm.text.toString()

            var timeAplazamiento = 5


            val calendar = Calendar.getInstance()
            val currentDate = calendar.time // Esto te da la fecha actual como un objeto Date

            // Ahora, puedes utilizar currentDate para asignar la fecha actual al Calendar si fechaSeleccionada es null
            if (fechaSeleccionada == null) {
                calendar.time = currentDate
            }else{
                calendar.time = fechaSeleccionada
            }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val todosDiasFalse = diasSemanaMap.values.all { !it }
            if (todosDiasFalse){
                dayList = false
            }

            Log.d("AlarmAcceptInfo", "El valor de nombre es: $nombre")
            Log.d("AlarmAcceptInfo", "El valor de minuto es: $minute")
            Log.d("AlarmAcceptInfo", "El valor de hour es: $hour")
            Log.d("AlarmAcceptInfo", "El valor de year es: $year")
            Log.d("AlarmAcceptInfo", "El valor de month es: $month")
            Log.d("AlarmAcceptInfo", "El valor de day es: $day")
            Log.d("AlarmAcceptInfo", "El valor de volumeAlarma es: $volumeAlarma")
            Log.d("AlarmAcceptInfo", "El valor de selectedAlarmTone es: $selectedAlarmTone")
            Log.d("AlarmAcceptInfo", "El valor de uriAlarm es: $uriAlarm")
            Log.d("AlarmAcceptInfo", "El valor de diasSemanaMap es: $diasSemanaMap")
            Log.d("AlarmAcceptInfo", "El valor de VibrationState es: $VibrationState")
            Log.d("AlarmAcceptInfo", "El valor de dayList es: $dayList")












            uriAlarm?.let { it2 -> setAlarm(requireContext(), year, month, day, hour, minute, nombre, volumenLevel = volumeAlarma, toneUri = it2, diasRepetirMap = diasSemanaMap, dayList = dayList, vibrate = VibrationState, aplazarTime = timeAplazamiento) }

        }


        cancelButton.setOnClickListener {
            listener?.onReturn2Home()
        }


    }

    private fun restartListDay(){
        for (key in diasSemanaMap.keys) {
            diasSemanaMap[key] = false
        }
        val diasSemanaIds = listOf(R.id.lunes, R.id.martes, R.id.miercoles, R.id.jueves, R.id.viernes, R.id.sabado, R.id.domingo)

        for (id in diasSemanaIds) {
            view?.findViewById<TextView>(id)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

    }
    private fun selectAlarmTone(view: View) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        //Este boton lo que hará sera el launch de otro fragmento.
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        if (fechaSeleccionada == null) {
            calendar.time = currentDate
        }else{
            calendar.time = fechaSeleccionada
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val timePicker = view.findViewById<TimePicker>(R.id.datePicker1) // Reemplaza R.id.timePicker con tu ID real
        val hour: Int = timePicker.hour
        val minute: Int = timePicker.minute
        val switchVibration = view.findViewById<SwitchCompat>(R.id.vibrateSwitch)
        var vibrate = switchVibration.isChecked
        val nombre = view.findViewById<EditText>(R.id.eventss)

        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var AlarmCacheInf = AlarmCache(
            sesion = "AlarmFragment",
            month = month,
            day = day,
            year = year,
            hour = hour,
            minute = minute,
            alarmName = nombre.text.toString(),
            volumeLevel = 50,
            toneUri = null,
            diasSemanaMap = diasSemanaMap,
            dayList = dayList,
            vibrate = vibrate,
            aplazamiento = 5,
            fechaSeleccionada = fechaSeleccionada,
            dateString = dateString,
            typeofalarm = typeofalarm)


        editor.putString("AlarmConfig", AlarmCacheInf.toJson()) //Esto lo que hará será guardar los datos
        //ya hay escritos de manera temporal para luego recuperarlos.
        editor.apply()
        listener?.openMusicSource()
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            // Hacer algo con la URI de la alarma seleccionada
            if (uri != null) {
                selectedAlarmTone = uri
            } else {
                // El usuario no seleccionó ninguna alarma
            }
        } else {
            // La selección de alarma falló o se canceló
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Home.OnHomeInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnHomeInteractionListener")
        }
    }

    override fun onItemClick(position: Int) {
        Log.d("ALFANUM", "MENSAJE DE PRUEBA PINORRO")
    }
}



data class AlarmCache(
    var sesion:String,
    var alarmName: String,
    var toneUri: String? = null,
    var toneState:Boolean = true,
    var volumeLevel: Int,
    var vibrate:Boolean,
    var aplazamiento: Int,
    var year: Int,
    var month: Int,
    var day: Int,
    var dayList:Boolean,
    var diasSemanaMap: Map<String, Boolean>,
    var hour: Int,
    var minute: Int,
    var active: Boolean = true,
    var fechaSeleccionada:Date? = null,
    var dateString: String? = null,
    var typeofalarm: String? = null,
    var volumeStatus:Boolean? = true
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }


    companion object {




        fun fromJson(json: String, clazz: Class<AlarmCache>): AlarmCache {
            val alarmCache = Gson().fromJson(json, clazz)
            return alarmCache
        }
    }
}