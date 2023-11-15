package com.example.aida

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aida.utils.AlarmDetails

class AlarmEditFragment(private val datas: AlarmDetails): Fragment() {


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
        return inflater.inflate(R.layout.alarm_fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nombreAlarma = datas.alarmName
        val horaAlarma = datas.hour
        val minutoAlarma = datas.minute
        val vibracion = datas.vibrate
        val uriSong = datas.toneUri
        val enable = datas.active
        val dayListMap = datas.diasRepetirMap
        val day = datas.day
        val month = datas.month
        val year = datas.year
        val isDaylist = datas.dayList

        //Setear el reloj segun hora y minutos obtenidos


        //Comprobar si es daylist. Si lo es, colorearemos los dias. Si no lo es, asignamos el dia en
        //el calendario y dejamos los dias sin colorear.
        //Crear la lista mutable de dias con su booleano true o false


        //Setear el switch de la vibración

        //Si hay uri, setear el switch del sonido

        //Setear el nombre de la alarma

        //Realizar comprobacion de los cambios. Los cambios se tendran que cambiar en el shared, borrar la antigua alarma y crear una nueva.


    }
}

