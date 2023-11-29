package com.example.aida.conectivity2model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast


class NetworkUtils() {
    companion object {

        // Crear una variable que almacene el servicio de conectividad


        // Crear un método que active los datos móviles
        fun enableMobileData(context:Context) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            connectivityManager.requestNetwork(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {})
        }

        // Crear un método que desactive los datos móviles
        fun disableMobileData(context:Context) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            // Comprobar la versión del SDK de Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build()
                connectivityManager.unregisterNetworkCallback(object :
                    ConnectivityManager.NetworkCallback() {})
            }
        }

        fun toggleWifiDetection(context: Context, enable: Boolean) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

            if (networkCapabilities != null) {
                val wifiTransport =
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

                if (enable && !wifiTransport) {
                    connectivityManager.bindProcessToNetwork(null)
                } else if (!enable && wifiTransport) {
                    connectivityManager.bindProcessToNetwork(null)
                }
            }
        }
        @SuppressLint("MissingPermission")
        fun isMobileDataEnabled(context: Context): Boolean {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.isDataEnabled
            } else {
                try {
                    val getMobileDataEnabledMethod =
                        TelephonyManager::class.java.getDeclaredMethod("getDataEnabled")
                    getMobileDataEnabledMethod.isAccessible = true
                    getMobileDataEnabledMethod.invoke(telephonyManager) as Boolean
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun toggleBluetoothDetection(enable: Boolean) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null) {
                if (enable) {
                    bluetoothAdapter.enable()
                } else {
                    bluetoothAdapter.disable()
                }
            }
        }

        fun conectionSelectorInterpreter(context: Context, texto: String) {
            val lowercaseSentence = texto.lowercase()

            when {
                containsAnyKeyword(lowercaseSentence, arrayOf("wifi")) -> {
                    if (containsAnyKeyword(lowercaseSentence, arrayOf("activame", "activa", "enciende", "enciendeme", "habilitame"))) {
                        toggleWifiDetection(context, true)
                    } else if (containsAnyKeyword(lowercaseSentence, arrayOf("desactivame", "desactiva", "apaga", "desconecta", "deshabilita"))) {
                        toggleWifiDetection(context, false)
                    }
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("datos", "datos móviles", "datos moviles", "datos móvil", "datos movil")) -> {
                    if (containsAnyKeyword(lowercaseSentence, arrayOf("activame", "activa", "enciende", "enciendeme", "habilitame"))) {
                        enableMobileData(context)
                    } else if (containsAnyKeyword(lowercaseSentence, arrayOf("desactivame", "desactiva", "apaga", "desconecta", "deshabilita"))) {
                        disableMobileData(context)
                    }
                }
                containsAnyKeyword(lowercaseSentence, arrayOf("bluetooth")) -> {
                    if (containsAnyKeyword(lowercaseSentence, arrayOf("activame", "activa", "enciende", "enciendeme", "habilitame"))) {
                        toggleBluetoothDetection(true)
                    } else if (containsAnyKeyword(lowercaseSentence, arrayOf("desactivame", "desactiva", "apaga", "desconecta", "deshabilita"))) {
                        toggleBluetoothDetection(false)
                    }
                }
                else -> {
                    // Acción no reconocida
                    Toast.makeText(context, "Acción no reconocida", Toast.LENGTH_SHORT).show()
                }
            }

        }

        private fun containsAnyKeyword(text: String, keywords: Array<String>): Boolean {
            return keywords.any { keyword -> containsKeyword(text, keyword) }
        }

        private fun containsKeyword(text: String, keyword: String): Boolean {
            return text.contains(keyword, ignoreCase = true)
        }

    }
}