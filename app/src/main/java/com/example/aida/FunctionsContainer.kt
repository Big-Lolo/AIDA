package com.example.aida

import android.content.Context
import com.example.aida.conectivity2model.CallSystem
import com.example.aida.conectivity2model.NetworkUtils
import com.example.aida.conectivity2model.Utilities

class FunctionsContainer {


    public fun callerInterpreter(context: Context, text:String){       //Llamadas
        CallSystem.interpretActionCall(context, text)
    }

    public fun interpreterDeConexiones(context: Context, text:String){      //Conexiones
        NetworkUtils.conectionSelectorInterpreter(context, text)
    }

    public fun linternaInterpreter(context: Context, text: String){    //Linterna
        Utilities.bulbState(context, text)
    }

    public fun volumenInterpreter(context: Context, text: String){       //Volumen dispositivo
        Utilities.volumenStates(context, text)
    }

    public fun brilloInterpreter(context: Context, text: String){      //Brillo pantalla
        Utilities.controlBrightness(context, text)
    }

    public fun powerControlInterpreter(context: Context, text: String){   //Apagar reiniciar cerrar
        Utilities.controlPower(context, text)
    }

    public fun create_whatsapp_message(context: Context, text: String){

    }
    public fun crear_audio_whatsapp(context: Context, text: String){

    }
}
