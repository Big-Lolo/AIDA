package com.example.aida

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnHomeInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        var view = inflater.inflate(R.layout.fragment_home, container, false)
        var fecha = view.findViewById<TextView>(R.id.Fecha_Completa)
        fecha.text = formattedDate

        val sharedPreferences = requireContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val nombreDeUsuarioExistente = sharedPreferences?.getString("usuario", null)
        var username = "Usuario"
        if (nombreDeUsuarioExistente != null) {
            // Ya existe un nombre de usuario, no es necesario guardar uno nuevo.
            // Puedes asignar el valor existente a la variable "username" si necesitas usarlo en tu aplicación.
            val username = nombreDeUsuarioExistente
        } else {
            // No hay un nombre de usuario guardado, puedes proceder a guardar uno.
            val editor = sharedPreferences?.edit()
            if (editor != null) {
                editor.putString("usuario", username)
                editor.apply()
            }
        }
        var textView = view.findViewById<TextView>(R.id.Bienvenida)
        textView.text = "Bienvenido de nuevo ${username}"

        //Asignación del boton de Alarmas
        val wasouskiButton: Button = view.findViewById(R.id.wasouski)
        wasouskiButton.setOnClickListener {

            Log.d("Home tyagg", "Nuevo fragmento")
            val nuevoFragmento = AlarmFragment()
            listener?.onAlarmButtonClicked()

            /* Reemplazar el fragmento actual con el nuevo fragmento
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.phatherContainerB, nuevoFragmento)
                .addToBackStack(null) // Agregar a la pila de retroceso
                .commit()*/

            //MainActivity.replaceFragmentt(AlarmFragment(), MainActivity())
        }



        return view
    }
    interface OnHomeInteractionListener {
        fun onAlarmButtonClicked()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnHomeInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnHomeInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}