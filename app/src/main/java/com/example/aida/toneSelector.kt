package com.example.aida

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aida.AlarmCache.Companion.fromJson
import com.google.gson.Gson

class toneSelector(var submenu:Boolean = false): Fragment(), OnItemClickListener {
    private var listener: Home.OnHomeInteractionListener? = null
    private var typeSong:String? = null
    private var VolumenLevel: Int = 0
    private var activoSonido: Boolean = true

    interface OnHomeInteractionListener {

        fun onReturn2Home()
    }

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
        return inflater.inflate(R.layout.general_tone_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(submenu){
            //TODO: Cambiar el "Melodia default" por el nombre del que seria la melodia seleccionada

        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                listener?.onAlarmButtonClicked(submenu = true)
                remove()
            }
        })

        val informativo = view.findViewById<LinearLayout>(R.id.innerLinearLayout)
        val spotify = view.findViewById<LinearLayout>(R.id.innerLinearLayout2)
        val melodia = view.findViewById<LinearLayout>(R.id.innerLinearLayout3)
        val infCheck = view.findViewById<ImageView>(R.id.anotherImage)
        val spotCheck = view.findViewById<ImageView>(R.id.anotherImage2)
        val melCheck = view.findViewById<ImageView>(R.id.anotherImage3)
        val infSubtitle = view.findViewById<TextView>(R.id.subtitleTextView)
        val spotSubtitle = view.findViewById<TextView>(R.id.subtitleTextView2)
        val melSubtitle = view.findViewById<TextView>(R.id.subtitleTextView3)
        val colorBlack = ContextCompat.getColor(requireContext(), R.color.black)
        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)



        informativo.setOnClickListener {
            infCheck.visibility = View.VISIBLE
            spotCheck.visibility = View.INVISIBLE
            melCheck.visibility = View.INVISIBLE

            infSubtitle.setTextColor(colorPrimary)
            spotSubtitle.setTextColor(colorBlack)
            melSubtitle.setTextColor(colorBlack)

            typeSong = "informativo"
            openMenuButton("informativo")

        }

        spotify.setOnClickListener {
            infCheck.visibility = View.INVISIBLE
            spotCheck.visibility = View.VISIBLE
            melCheck.visibility = View.INVISIBLE

            infSubtitle.setTextColor(colorBlack)
            spotSubtitle.setTextColor(colorPrimary)
            melSubtitle.setTextColor(colorBlack)

            typeSong = "spotify"
            openMenuButton("spotify")

        }

        melodia.setOnClickListener {
            infCheck.visibility = View.INVISIBLE
            spotCheck.visibility = View.INVISIBLE
            melCheck.visibility = View.VISIBLE

            infSubtitle.setTextColor(colorBlack)
            spotSubtitle.setTextColor(colorBlack)
            melSubtitle.setTextColor(colorPrimary)

            typeSong = "melodia"
            openMenuButton("melodia")
        }

        val seekBarVolume = view.findViewById<SeekBar>(R.id.seekBar)

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumeValue = progress
                VolumenLevel = volumeValue
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //TODO: USAR ESTO PARA REPRODUCIR EL TONO MIENTRAS SE MANTIENE PULSADO.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Este método se llama cuando se deja de tocar la SeekBar
            }
        })


    }

    fun openMenuButton(category: String ){
        if(category == "spotify" || category == "informativo" || category == "melodia"){

            val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val alarmConfigString = sharedPreferences.getString("AlarmConfig", null)

            if (alarmConfigString != null) {
                // Convertir el JSON a un objeto (asumiendo que sea JSON)
                val alarmConfig = fromJson(alarmConfigString, AlarmCache::class.java)


                // Actualizar los valores en el objeto según sea necesario

                alarmConfig.volumeLevel = if(activoSonido) VolumenLevel else 0
                alarmConfig.typeofalarm = typeSong
                alarmConfig.volumeStatus = activoSonido


                // Convertir el objeto actualizado a JSON
                val updatedAlarmConfigString = Gson().toJson(alarmConfig)

                // Guardar el valor actualizado de "AlarmConfig" en SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("AlarmConfig", updatedAlarmConfigString)
                editor.apply()


                if(category == "spotify" ){
                    //TODO openSpotifySelector()
                }else if (category == "informativo"){

                }else if (category == "melodia"){
                    listener?.openMelodySelector()
                }
            }

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
    }





}