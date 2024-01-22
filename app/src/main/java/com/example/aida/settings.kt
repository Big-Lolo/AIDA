package com.example.aida

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
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
            Log.d("UserconfigJSON", "$userConfigString")
            val gson = Gson()
            val userConfig = gson.fromJson(userConfigString, SettingDataList::class.java)
            Log.d("UserconfigJSON2", "$userConfig")

            cacheInfogeneral = userConfig
        }
        if (!sharedPreferences.contains("UserProfiles_DefaultUser")) {
            // Si está vacío, guarda valores por defecto
            val defaultConfig = ProfilesDataList(
                "DefaultUser",
                true,
                true,
                false,
                true,
                true,
                true,
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
            val userConfigString = sharedPreferences.getString("UserProfiles_DefaultUser", "")
            val gson = Gson()
            val userConfig = gson.fromJson(userConfigString, ProfilesDataList::class.java)
            cacheProfiles = userConfig
        }



        val spinner: Spinner = view.findViewById(R.id.spinnerOpciones)
        val opcionesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cacheInfogeneral.profileList
        )
        Log.d("PROFILElIST", "eL PROFILELIST ES: ${cacheInfogeneral}")
        opcionesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = opcionesAdapter

        val elementoSeleccionado = cacheInfogeneral.actualProfile
        val posicionElemento = cacheInfogeneral.profileList.indexOf(elementoSeleccionado)
        spinner.setSelection(posicionElemento)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = parent.getItemAtPosition(position).toString()
                //Obtener la configuracion del perfil Seleccionado
                if (sharedPreferences.contains("UserProfiles_$opcionSeleccionada")) {
                    val userConfigString = sharedPreferences.getString("UserProfiles_$opcionSeleccionada", "")
                    val gson = Gson()
                    val userConfig = gson.fromJson(userConfigString, ProfilesDataList::class.java)
                    cacheProfiles = userConfig
                    //Con esto, cargas en el cache el nuevo perfil.
                }

                //función para actualizar los switch y toddo de la pantalla de config siguiendo cacheProfiles
                changeStatusSwitches()
                //Función para habilitar elementos que requieran cambio como el wifi, bluethooth...
                changeSystemConfigurations()
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
        cacheInfogeneral
        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)
        buttonEditor.setOnClickListener { EditorButtonState(view) }
        cancelButton.setOnClickListener { CancelButtonClicable(view) }


        //Lo que hay que hacer aqui seria de cargar el nombre actual
        val title = view.findViewById<TextView>(R.id.Introductorio)
        val texte = generarSaludo() + cacheInfogeneral.username
        title.text = texte
        //Asignar los clicables del nav vertical de las subcategorias.
        changeStatusSwitches()
        //Cambiar configuraciones del sistema segun el perfil
        changeSystemConfigurations()



    }

    fun generarSaludo(): String {
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when {
            horaActual < 12 -> "Buenos días "
            horaActual < 20 -> "Buenas tardes "
            else -> "Buenas noches "
        }
    }



    private fun changeStatusSwitches(){
        //Cambiar el estado de los switchess
        view?.findViewById<Switch>(R.id.windowAssist)!!.isChecked = cacheProfiles.windowAssist
        view?.findViewById<Switch>(R.id.gpsswitch)!!.isChecked = cacheProfiles.gpsStatus
        view?.findViewById<Switch>(R.id.callIdentify)!!.isChecked = cacheProfiles.AssistantCalls
        view?.findViewById<Switch>(R.id.recordCalls)!!.isChecked = cacheProfiles.recordCalls
        view?.findViewById<Switch>(R.id.callsBotAsist)!!.isChecked = cacheProfiles.AssistantVoice
        view?.findViewById<Switch>(R.id.notifyReader)!!.isChecked = cacheProfiles.notifyReader
        view?.findViewById<Switch>(R.id.WhatsappAssist)!!.isChecked = cacheProfiles.whatsappAssist
        view?.findViewById<Switch>(R.id.callHelper)!!.isChecked = cacheProfiles.callHelper
        view?.findViewById<Switch>(R.id.calendarAsistant)!!.isChecked = cacheProfiles.calendarAsistant
        view?.findViewById<Switch>(R.id.wifi)!!.isChecked = cacheProfiles.wifiEnable
        view?.findViewById<Switch>(R.id.datos)!!.isChecked = cacheProfiles.datosEnable
        view?.findViewById<Switch>(R.id.bluetooth)!!.isChecked = cacheProfiles.bluetoothEnable
    }
    private fun changeSystemConfigurations(){
        if (cacheProfiles.bluetoothEnable){
            //Encender bluethooth
        }else{
            //Apagar bluethooth
        }
        if (cacheProfiles.wifiEnable){
            //Encender wifi
        }else{
            //Apagar wifi
        }
        if (cacheProfiles.datosEnable){
            //Encender datos
        }else{
            //Apagar datos
        }
    }

    @SuppressLint("SetTextI18n")
    private fun EditorButtonState(view:View){
        val buttonEditor = view.findViewById<Button>(R.id.editButton)
        val cancelButton = view.findViewById<Button>(R.id.button6)
        val editor = view.findViewById<EditText>(R.id.EditorUsername)
        val viewname = view.findViewById<TextView>(R.id.viewNombrefield)
        val introduct = view.findViewById<TextView>(R.id.Introductorio)

        if (buttonEditor.text != "Guardar") {
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
            Log.d("NAME-EDITED", "El nombre nuevo es ${name2save.toString()}")
            cacheInfogeneral.username = name2save.toString()
            saveConfigs()
            viewname.visibility = View.VISIBLE
            viewname.text = name2save.toString()
            editor.visibility = View.INVISIBLE
            cancelButton.visibility = View.INVISIBLE
            cancelButton.isClickable = false
            buttonEditor.text = "Editar"
            editor.isClickable = false
            editor.focusable = View.NOT_FOCUSABLE
            introduct.text = generarSaludo() + name2save.toString()
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

    private fun saveProfiles() {
        val defaultConfigJson = cacheProfiles.toJson()
        sharedPreferences.edit().putString("UserProfiles_${cacheProfiles.profileName}", defaultConfigJson).apply()

    }

    private fun saveConfigs(){
        Log.d("Guardado de datos", "savingDatas")
        val defaultConfigJson = cacheInfogeneral.toJson()
        Log.d("configJSON", "El config en JSON es: $defaultConfigJson")
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
    var windowAssist: Boolean,
    var AssistantVoice: Boolean,
    var AssistantCalls: Boolean,
    var whatsappAssist: Boolean,
    var callHelper: Boolean,
    var calendarAsistant: Boolean,
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