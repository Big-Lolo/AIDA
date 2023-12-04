package com.example.aida

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import com.google.gson.Gson
import java.sql.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class settings : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        if(buttonEditor.text != "") {
            buttonEditor.setOnClickListener { EditorButtonState(view, true) }
        }else{
            buttonEditor.setOnClickListener { EditorButtonState(view, false) }
        }
        //Lo que hay que hacer aqui seria de cargar el nombre actual
        //El nombre lo cargamos del SharedConfig.
        //Asignar los clicables del nav vertical de las subcategorias.
        //Una de las subcategorias sera de perfiles enteras.
        //Tendrá un selector, que tendrá todas las que existen y un "+Crear".
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val configParameters = sharedPreferences.getString("UserConfigurations", null)
        val ConfigInformation = configParameters?.let {
            AlarmCache.fromJson(
                it,
                SettingDataList::class.java
            )
        }
        //TODO: Arreglar el tema del get info from Config
        val switchGPS = view.findViewById<Switch>(R.id.switch8)
        //1switchGPS.isChecked = ConfigInformaton.
    }

    private fun EditorButtonState(view:View, saveInfo:Boolean){
        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)
        if (!saveInfo) {
            buttonEditor.text = "Guardar"
            cancelButton.visibility = View.VISIBLE
            cancelButton.isClickable = true
            //Cambiar el boton de editar por "Guardar" y hacer visible el boton de cancelar.
            //o sino cambiarlo por default
            val editor = view.findViewById<EditText>(R.id.EditorUsername)
            editor.inputType = 1 //para que no se pueda editar
            //Hay que comprobar cual seria la ID correspondiente del valor del inputType.

        }else{
            //Actualizar la informacion de algun Config con tal de cambiar el nombre

            //Hacer invisible el boton de cancelar
            cancelButton.visibility = View.INVISIBLE
            cancelButton.isClickable = false
            //Volver al texto de Editar nombre
            buttonEditor.text = "Editar"


        }



    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}



data class SettingDataList(
    var username:String,
    var birthday: Date,
    var AssistantVoice: Boolean,
    var AssistantCalls: Boolean,
    var gpsStatus: Boolean,
    var useProfies: Boolean,
    var actualProfile: String,
    var profileList:MutableList<String>, //En esta lista, siempre estará el perfil Default
    var userImageProfile: String

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