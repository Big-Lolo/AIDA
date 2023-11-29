package com.example.aida

import android.content.Context
import com.example.aida.conectivity2model.CallSystem
import com.example.aida.conectivity2model.NetworkUtils
import com.example.aida.conectivity2model.Utilities

class FunctionsContainer {


    public fun callerInterpreter(context: Context, text:String){
        CallSystem.interpretActionCall(context, text)
    }

    public fun interpreterDeConexiones(context: Context, text:String){
        NetworkUtils.conectionSelectorInterpreter(context, text)
    }

    public fun linternaInterpreter(context: Context, text: String){
        Utilities.bulbState(context, text)
    }

    public fun volumenInterpreter(context: Context, text: String){
        Utilities.volumenStates(context, text)
    }
}
