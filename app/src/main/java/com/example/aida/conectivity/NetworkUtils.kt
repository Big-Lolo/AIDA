package com.example.aida.conectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build


class NetworkUtils(private val context: Context) {

    // Crear una variable que almacene el servicio de conectividad
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Crear un método que active los datos móviles
    fun enableMobileData() {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {})
    }

    // Crear un método que desactive los datos móviles
    fun disableMobileData() {
        // Comprobar la versión del SDK de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            connectivityManager.unregisterNetworkCallback(object : ConnectivityManager.NetworkCallback() {})
        }
    }

    fun setWifiEnabled(context: Context, enabled: Boolean) {
        //Hacer esto con permisos de accesibilidad, ya que esta depreciado el metodo con la api
        
    }
}