package com.example.aida

import android.app.Activity
import android.app.DatePickerDialog
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aida.utils.AlarmTools.Companion.setAlarm
import java.sql.Date
import java.text.SimpleDateFormat

class AlarmFragment : Fragment() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmFragment", "onCreate")
        // Resto del código
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflar el diseño del fragmento (por ejemplo, un fondo blanco)
        return inflater.inflate(R.layout.alarm_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            return if (diasString.isNotEmpty()) {
                "Cada $diasString"
            } else {
                ""
            }
        }

        fun showDatePickerDialog(vieww: View) {
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
                        val calendarHoy = Calendar.getInstance()

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
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                )
            }
            val calendarHoy = Calendar.getInstance()
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

            val switchTone = view.findViewById<SwitchCompat>(R.id.soundSwitch)
            var ToneState = switchTone.isChecked

            val nameAlarm = view.findViewById<EditText>(R.id.eventss)
            var nombre = nameAlarm.text.toString()

            //context?.let { it1 -> setAlarm(enableTone = ToneState,  alarmTone = selectedAlarmTone, context = it1, hour =hour, minute = minute, enableVibration = VibrationState, alarmName = nombre ) }
            //context?.let { it1 -> AlarmTools.setAlarm(sound = ToneState,  tone = selectedAlarmTone, context = it1, hour =hour, minute = minute, vibration = VibrationState, name = nombre, repeatDays = listOf(Calendar.MONDAY, Calendar.FRIDAY), volume = 0.6f) }

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


            selectedAlarmTone?.let { it1 -> setAlarm(requireContext(), year, month, day, hour, minute, nombre, volumenLevel = 50, toneUri = it1) }

        }


        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }




    }

    fun selectAlarmTone(view: View) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        // Configurar el intent según sea necesario, por ejemplo:
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Seleccionar alarma")

        resultLauncher.launch(intent)
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


}