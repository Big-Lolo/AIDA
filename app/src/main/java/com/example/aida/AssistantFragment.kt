package com.example.aida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AssistantFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_assistant, container, false)
        // Configura la interfaz de usuario del fragmento, como el diseño y los botones
        // Asegúrate de que el fragmento no ocupe toda la pantalla y se superponga a la actividad principal
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configura la lógica para cerrar el fragmento cuando se toque el botón de cerrar
    }
}