package com.example.aida

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class MelodySelectorIt(): Fragment(), OnItemClickListener {
    private var listener: Home.OnHomeInteractionListener? = null
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.melody_list_selector, container, false)

        // Obtener la referencia al ListView en el diseño del fragmento
        listView = view.findViewById<ListView>(R.id.listViewMelodies)

        // Obtener la lista de tonos de alarma del sistema
        val alarmTones = getAlarmTones()

        // Crear un ArrayAdapter para mostrar la lista en el ListView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, alarmTones)

        // Configurar el adaptador en el ListView
        listView.adapter = adapter

        // Configurar el listener de clic en elementos de la lista si es necesario
        val textSelect = view.findViewById<TextView>(R.id.textView7)
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedMelody = adapter.getItem(position)
            Log.d("SELECTED_SONG", "Se seleccionó $selectedMelody")
            textSelect.text = "Melodia Seleccionada: $selectedMelody"
            // Realizar acciones con el tono de alarma seleccionado
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    listener?.onAlarmButtonClicked(submenu = true)
                    remove()
                }
            })

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

    private fun getAlarmTones(): List<String> {
        val alarmTones: MutableList<String> = mutableListOf()

        val ringtoneManager = RingtoneManager(requireContext())
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)

        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            alarmTones.add(title)
        }
        cursor.close()

        return alarmTones
    }
}