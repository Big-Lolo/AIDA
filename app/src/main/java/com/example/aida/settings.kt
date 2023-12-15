package com.example.aida

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import java.sql.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var sharedPreferences: SharedPreferences
private val PREFS_NAME = "UserConfigurations"
private lateinit var cacheInfogeneral: SettingDataList
private lateinit var cacheProfiles: ProfilesDataList



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
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Comprueba si el archivo de configuración está vacío
        if (!sharedPreferences.contains("UserConfig")) {
            // Si está vacío, guarda valores por defecto
            val defaultConfig = SettingDataList(
                "DefaultUser",
                null,
                true,
                true,
                true,
                "DefaultProfile",
                mutableListOf("DefaultProfile"),
                "default_image_url"
            )
            cacheInfogeneral = defaultConfig
            saveConfigs()

        }else{
            //Cargar el sharedpreferences en el cacheInfogeneral
            val userConfigString = sharedPreferences.getString("UserConfig", "")
            val gson = Gson()
            val userConfig = gson.fromJson(userConfigString, SettingDataList::class.java)
            cacheInfogeneral = userConfig
        }
        if (!sharedPreferences.contains("UserProfiles")) {
            // Si está vacío, guarda valores por defecto
            val defaultConfig = ProfilesDataList(
                "DefaultUser",
                true,
                false,
                true,
                true,
                true,
                true,
                false,
                true,
                50,
                50,
                50,
            )

            cacheProfiles = defaultConfig
            saveProfiles()

        }else{
            //Cargar el sharedpreferences en el cacheProfiles
            val userConfigString = sharedPreferences.getString("UserProfiles", "")
            val gson = Gson()
            val userConfig = gson.fromJson(userConfigString, ProfilesDataList::class.java)
            cacheProfiles = userConfig
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val spinner: Spinner = view.findViewById(R.id.spinnerOpciones)
        val opcionesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cacheInfogeneral.profileList
        )
        opcionesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = opcionesAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = parent.getItemAtPosition(position).toString()
                //Obtener la configuracion del perfil Seleccionado

                // Actualizar el infocacheGeneral con los ajustes activos y inactivos

                // Cambiar los valores de los switch y otros


            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Método requerido, pero puedes dejarlo vacío si no necesitas hacer nada aquí
            }
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setear los datos actuales
        val configParameters = sharedPreferences.getString("UserConfigurations", null)
        val configInformation = configParameters?.let {
            SettingDataList.fromJsons(
                it,
                SettingDataList::class.java
            )
        }




        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)

        if(buttonEditor.text != "") {
            buttonEditor.setOnClickListener { EditorButtonState(view, true) }
        }else{
            buttonEditor.setOnClickListener { EditorButtonState(view, false) }
        }

        cancelButton.setOnClickListener { CancelButtonClicable(view) }

        //Lo que hay que hacer aqui seria de cargar el nombre actual
        //El nombre lo cargamos del SharedConfig.
        //Asignar los clicables del nav vertical de las subcategorias.
        //Una de las subcategorias sera de perfiles enteras.
        //Tendrá un selector, que tendrá todas las que existen y un "+Crear".

        val switchGPS = view.findViewById<Switch>(R.id.switch8)

    }

    private fun EditorButtonState(view:View, saveInfo:Boolean){
        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)
        val editor = view.findViewById<EditText>(R.id.EditorUsername)
        val viewname = view.findViewById<TextView>(R.id.viewNombrefield)


        if (!saveInfo) {
            buttonEditor.text = "Guardar"
            cancelButton.visibility = View.VISIBLE
            cancelButton.isClickable = true
            viewname.visibility = View.INVISIBLE
            editor.visibility = View.VISIBLE
            editor.isClickable = true
            editor.focusable = View.FOCUSABLE
            val editable: Editable = SpannableStringBuilder(cacheInfogeneral.username)
            editor.text = editable
        }else{
            val name2save = editor.text
            cacheInfogeneral.username = name2save.toString()
            saveConfigs()
            viewname.visibility = View.VISIBLE
            editor.visibility = View.INVISIBLE
            cancelButton.visibility = View.INVISIBLE
            cancelButton.isClickable = false
            buttonEditor.text = "Editar"
            editor.isClickable = false
            editor.focusable = View.NOT_FOCUSABLE
        }
    }


    private fun CancelButtonClicable(view: View){
        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)
        val editor = view.findViewById<EditText>(R.id.EditorUsername)
        val viewname = view.findViewById<TextView>(R.id.viewNombrefield)

        if(cancelButton.isVisible){
            //Si es bisible y se pulsa cancelar, que devuelva estado inicial
            cancelButton.visibility = View.INVISIBLE
            cancelButton.isClickable = false
            //Volver al texto de Editar nombre
            buttonEditor.text = "Editar"
            editor.isClickable = false
            editor.focusable = View.NOT_FOCUSABLE
            viewname.visibility = View.VISIBLE
            editor.visibility = View.INVISIBLE
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

    fun saveProfiles() {
        val defaultConfigJson = cacheProfiles.toJson()
        sharedPreferences.edit().putString("UserConfig", defaultConfigJson).apply()
    }

    fun saveConfigs(){
        val defaultConfigJson = cacheInfogeneral.toJson()
        sharedPreferences.edit().putString("UserConfig", defaultConfigJson).apply()
    }
}



data class SettingDataList(
    var username:String,
    var birthday: Date?,
    var AssistantVoice: Boolean,
    var AssistantCalls: Boolean,
    var gpsStatus: Boolean,
    var actualProfile: String,
    var profileList:MutableList<String>, //En esta lista, siempre estará el perfil Default. Cada vez que se cree un perfil, se añadira su nombre aqui.
    var userImageProfile: String

) {
    fun toJson(): String {
        return Gson().toJson(this)
    }


    companion object {

        fun fromJsons(json: String, clazz: Class<SettingDataList>): SettingDataList {
            val alarmCache = Gson().fromJson(json, clazz)
            return alarmCache
        }
    }
}


data class ProfilesDataList(
    var profileName:String,
    var AssistantVoice: Boolean,
    var AssistantCalls: Boolean,
    var gpsStatus: Boolean,
    var wifiEnable: Boolean,
    var datosEnable: Boolean,
    var bluetoothEnable: Boolean,
    var notifyReader: Boolean,
    var recordCalls:Boolean,
    var Volumecalls: Int = 0,
    var Volumemultimedia: Int = 0,
    var Volumenotifications: Int = 0,


) {
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJsonsP(json: String, clazz: Class<ProfilesDataList>): ProfilesDataList {
            val alarmCache = Gson().fromJson(json, clazz)
            return alarmCache
        }
    }
}