package com.example.aida

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
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aida.utils.AlarmDetails
import com.example.aida.utils.AlarmTools
import com.example.aida.utils.AlarmTools.Companion.deleteAlarm
import java.sql.Date
import java.text.SimpleDateFormat

class AlarmEditFragment(private var datas: AlarmDetails? = null, private var submenu:Boolean = false): Fragment(), OnItemClickListener {
    private var diasSemanaMap = mutableMapOf<String, Boolean>(
        "Lunes" to false,
        "Martes" to false,
        "Miércoles" to false,
        "Jueves" to false,
        "Viernes" to false,
        "Sábado" to false,
        "Domingo" to false
    )

    private var selectedAlarmTone: Uri? = null
    private var fechaSeleccionada: Date? = null
    private var dayList : Boolean = false
    private var dateString:String? = null
    private var nombreAlarma = datas?.alarmName
    private var horaAlarma = datas?.hour
    private var minutoAlarma = datas?.minute
    private var vibracion = datas?.vibrate
    private var uriSong = datas?.toneUri
    private var dayListMap = datas?.diasRepetirMap
    private var day = datas?.day
    private var month = datas?.month
    private var year = datas?.year
    private var isDaylist = datas?.dayList
    private var codeId =  datas?.id
    private var listener: Home.OnHomeInteractionListener? = null
    private var typeofalarm: String? = null

    interface OnHomeInteractionListener {
        fun onReturn2Home()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Resto del código
    }


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflar el diseño del fragmento (por ejemplo, un fondo blanco)
        return inflater.inflate(R.layout.alarm_fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                listener?.onReturn2Home()
                remove()
            }
        })
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val alarmConfigJson = sharedPreferences.getString("AlarmConfig", null)
        val alarmCacheInf = alarmConfigJson?.let {
            AlarmCache.fromJson(
                it,
                AlarmCache::class.java
            )
        }
        if(submenu) {
            nombreAlarma = alarmCacheInf?.alarmName
            horaAlarma = alarmCacheInf?.hour
            minutoAlarma = alarmCacheInf?.minute
            vibracion = alarmCacheInf?.vibrate
            uriSong = Uri.parse(alarmCacheInf?.toneUri).toString()
            dayListMap = alarmCacheInf?.diasSemanaMap
            day = alarmCacheInf?.day
            month = alarmCacheInf?.month
            year = alarmCacheInf?.year
            Log.d("FECHA_CARGADA", "LA FECHA ES: $day de $month de $year")
            isDaylist = alarmCacheInf?.dayList
            codeId = alarmCacheInf?.codeId
            typeofalarm = alarmCacheInf?.typeofalarm
        }


        val buttonSelectDate: Button = view.findViewById(R.id.calendarpk2)
        buttonSelectDate.setOnClickListener {
            // Al hacer clic, abre el selector de fecha
            showDatePickerDialog(view, true)
        }

        //Setear el reloj segun hora y minutos obtenidos //Setear el nombre de la alarma
        val campoNombre =  view.findViewById<EditText>(R.id.eventss2)
        campoNombre.setText(nombreAlarma)
        val timePicker = view.findViewById<TimePicker>(R.id.datePicker12) //
        timePicker.setIs24HourView(true)
        val hour: Int? = horaAlarma
        val minute: Int? = minutoAlarma

        if (Build.VERSION.SDK_INT >= 23) {
            if (hour != null) {
                timePicker.hour = hour
            }
            if (minute != null) {
                timePicker.minute = minute
            }
        }


        if(isDaylist == true) {
            val diasSemanaIds = intArrayOf(
                R.id.lunes2,
                R.id.martes2,
                R.id.miercoles2,
                R.id.jueves2,
                R.id.viernes2,
                R.id.sabado2,
                R.id.domingo2
            )
            for ((index, entry) in dayListMap?.entries?.withIndex()!!) {
                val dia = entry.key
                val estaActivo = entry.value

                if (estaActivo) {
                    val idTextView = diasSemanaIds[index]

                    // Cambiar el color del TextView asociado.
                    val textView = view.findViewById<TextView>(idTextView)
                    textView.setTextColor(resources.getColor(R.color.colorPrimary))
                }
            }
        }else{

        }

        //Setear el switch de la vibración
        val switchVibracion= view.findViewById<SwitchCompat>(R.id.vibrateSwitch2)
        if (vibracion != null) {
            switchVibracion.isChecked = vibracion as Boolean
        }

        //Edicion de alarmas

        val diasSemanaIds = intArrayOf(R.id.lunes2, R.id.martes2, R.id.miercoles2, R.id.jueves2, R.id.viernes2, R.id.sabado2, R.id.domingo2)

        for (id in diasSemanaIds) {
            val textView = view.findViewById<TextView>(id)
            val dia = when (id) {
                R.id.lunes2 -> "Lunes"
                R.id.martes2 -> "Martes"
                R.id.miercoles2 -> "Miércoles"
                R.id.jueves2 -> "Jueves"
                R.id.viernes2 -> "Viernes"
                R.id.sabado2 -> "Sábado"
                R.id.domingo2 -> "Domingo"
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
                val text = view.findViewById<TextView>(R.id.textView31112)
                if (sentenceDays!= ""){
                    dateString = sentenceDays
                    text.text = sentenceDays
                } else {
                    dateString = "Hoy"
                    text.text = "Hoy"
                }
            }
        }


        val selectToneButton = view.findViewById<Button>(R.id.selectToneButtons2)
        selectToneButton.setOnClickListener {
            selectAlarmTone(view)
        }
        //TODO: Configurar el sistema de URI (sonido de alarma junto a su volumen)





        val cancelButton = view.findViewById<Button>(R.id.btnCancelar2)
        val editarButton = view.findViewById<Button>(R.id.btnGuardar2)



        editarButton.setOnClickListener {

            //Primero hay que cancelar la alarma antigua y despues crear una nueva con la misma id:
            Log.d("ALARMID", "LA ID DE LA ALARMA ES $codeId")
            if (codeId != null) {
                deleteAlarm(requireContext(), codeId!!)
            } //Esto cancelara la alarma y la borrará del preference.


            val timePicker2 = view.findViewById<TimePicker>(R.id.datePicker12)

            var hour2: Int = timePicker2.hour
            var minute2: Int = timePicker2.minute
            Log.d("ALARMAVALORES", "LA ALARMA SONARA A LAS ${timePicker2.hour} Y ${timePicker2.minute}")

            val switchVibration = view.findViewById<SwitchCompat>(R.id.vibrateSwitch2)
            var VibrationState = switchVibration.isChecked

            val switchTone = view.findViewById<SwitchCompat>(R.id.soundSwitch2)
            var ToneState = switchTone.isChecked

            val nameAlarm = view.findViewById<EditText>(R.id.eventss2)
            var nombre2 = nameAlarm.text.toString()

            var timeAplazamiento = 5 //5 minutos de aplazamiento default. Ya se hará el selector


            val calendar = Calendar.getInstance()
            val currentDate = calendar.time // Esto te da la fecha actual como un objeto Date

            // Ahora, puedes utilizar currentDate para asignar la fecha actual al Calendar si fechaSeleccionada es null
            if (fechaSeleccionada == null) {
                calendar.time = currentDate
            }else{
                calendar.time = fechaSeleccionada
            }
            val year2 = calendar.get(Calendar.YEAR)
            val month2 = calendar.get(Calendar.MONTH) + 1
            val day2 = calendar.get(Calendar.DAY_OF_MONTH)

            val todosDiasFalse = diasSemanaMap.values.all { !it }
            if (todosDiasFalse){
                dayList = false
            }
                if (codeId != null) {
                    AlarmTools.setAlarm(
                        requireContext(),
                        year2,
                        month2,
                        day2,
                        hour2,
                        minute2,
                        nombre2,
                        volumenLevel = 50,
                        toneUri = uriSong,
                        diasRepetirMap = diasSemanaMap,
                        dayList = dayList,
                        vibrate = VibrationState,
                        aplazarTime = timeAplazamiento,
                        edit = true,
                        identifier = codeId!!
                    )

            }

        }


        cancelButton.setOnClickListener {

                   }


























    }
    fun convertFechaActual(day: Int?, month: Int?, year: Int?): Triple<Int, Int, Int> {
        val calendarHoy = Calendar.getInstance()

        val currentYear = calendarHoy.get(Calendar.YEAR)
        val currentMonth = calendarHoy.get(Calendar.MONTH)
        val currentDay = calendarHoy.get(Calendar.DAY_OF_MONTH)

        return if (year == currentYear && month == currentMonth && day == currentDay) {
            Triple(currentYear, currentMonth, currentDay)
        } else if (calendarHoy.after(Calendar.getInstance().apply { set(year ?: 0, month ?: 0, day ?: 0) })) {
            Triple(currentYear, currentMonth, currentDay)
        } else {
            Triple(year ?: 0, month ?: 0, day ?: 0)
        }
    }
    fun showDatePickerDialog(vieww: View, isSetting: Boolean = false) {
        Log.d("FECHA_CARGADA_datapicker", "LA FECHA ES: $day de $month de $year")

        val (convertedYear, convertedMonth, convertedDay) = convertFechaActual(day, month, year)

        Log.d("FECHA_CARGADA_datapicker2", "LA FECHA ES: $convertedDay de $convertedMonth de $convertedYear")

        val datePicker = context?.let {
            val initialYear = if (isSetting) convertedYear else Calendar.getInstance().get(Calendar.YEAR)
            val initialMonth = if (isSetting) convertedMonth?.minus(1) else Calendar.getInstance().get(Calendar.MONTH)
            val initialDay = if (isSetting) convertedDay else Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                it,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    fechaSeleccionada = Date(selectedCalendar.timeInMillis)
                    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
                    val formattedDate = simpleDateFormat.format(fechaSeleccionada)

                    val text = vieww.findViewById<TextView>(R.id.textView31112)
                    val calendarHoy = Calendar.getInstance()
                    dayList = false
                    restartListDay()
                    if (selectedCalendar.get(Calendar.YEAR) == calendarHoy.get(Calendar.YEAR) &&
                        selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendarHoy.get(Calendar.DAY_OF_YEAR)) {
                        dateString = "Hoy"
                        text.text = "Hoy"
                    } else {
                        calendarHoy.add(Calendar.DAY_OF_YEAR, 1)
                        if (selectedCalendar.get(Calendar.YEAR) == calendarHoy.get(Calendar.YEAR) &&
                            selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendarHoy.get(Calendar.DAY_OF_YEAR)) {
                            dateString = "Mañana"
                            text.text = "Mañana"
                        } else {
                            dateString = formattedDate
                            text.text = formattedDate
                        }
                    }
                },
                // Configura la fecha actual como fecha predeterminada
                initialYear,
                initialMonth ?: 0,
                initialDay ?: 1
            )
        }

        val calendarHoy = Calendar.getInstance()
        datePicker?.datePicker?.minDate = calendarHoy.timeInMillis
        datePicker?.show()
    }

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

    private fun restartListDay(){
        for (key in diasSemanaMap.keys) {
            diasSemanaMap[key] = false
        }
        val diasSemanaIds = listOf(R.id.lunes2, R.id.martes2, R.id.miercoles2, R.id.jueves2, R.id.viernes2, R.id.sabado2, R.id.domingo2)

        for (id in diasSemanaIds) {
            view?.findViewById<TextView>(id)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

    }

    private fun selectAlarmTone(view: View) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
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

        val timePicker = view.findViewById<TimePicker>(R.id.datePicker12) // Reemplaza R.id.timePicker con tu ID real
        val hour: Int = timePicker.hour
        val minute: Int = timePicker.minute
        val switchVibration = view.findViewById<SwitchCompat>(R.id.vibrateSwitch2)
        var vibrate = switchVibration.isChecked
        val nombre = view.findViewById<EditText>(R.id.eventss2)

        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var AlarmCacheInf = codeId?.let {
            AlarmCache(
                sesion = "AlarmFragment",
                month = month,
                day = day,
                year = year,
                hour = hour,
                minute = minute,
                alarmName = nombre.text.toString(),
                volumeLevel = 50,
                toneUri = uriSong.toString(),
                diasSemanaMap = diasSemanaMap,
                dayList = dayList,
                vibrate = vibrate,
                aplazamiento = 5,
                fechaSeleccionada = fechaSeleccionada,
                dateString = dateString,
                typeofalarm = typeofalarm,
                codeId = it
            )
        }


        if (AlarmCacheInf != null) {
            editor.putString("AlarmConfig", AlarmCacheInf.toJson())
        }
        editor.apply()
        listener?.openMusicSource()
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
    }
}

