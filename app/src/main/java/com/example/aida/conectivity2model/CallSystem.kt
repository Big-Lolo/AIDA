package com.example.aida.conectivity2model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.regex.Pattern


class CallSystem {
    companion object{
    private fun llamarNumero(context: Context, numero: String, altavoz: Boolean, bluetooth: Boolean) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$numero")

        // Verificar si se tiene el permiso para realizar la llamada
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Configurar el altavoz si es necesario
            if (altavoz) {
                intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            }

            // Configurar el Bluetooth si es necesario y es compatible
            if (bluetooth && context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                    intent.putExtra("android.bluetooth.headset", true)
                } else {
                    // Puedes manejar la lógica para solicitar al usuario que habilite Bluetooth aquí
                }
            }

            context.startActivity(intent)
        } else {
            // Si no se tiene el permiso, solicitarlo al usuario
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                1
            )
        }
    }

    @SuppressLint("Range")
    fun buscarContactoPorOracion(context: Context, oracion: String): String? {
        val contactList = getContactList(context)
        var phoneNumber: String? = null
        var bestMatch: String? = null
        var bestMatchDistance = Int.MAX_VALUE

        val palabras = oracion.split(" ")

        for (palabra in palabras) {
            for ((name, number) in contactList) {
                val nameWords = name.split(" ")
                for (nameWord in nameWords) {
                    val distance = levenshteinDistance(nameWord.lowercase(), palabra.lowercase())
                    if (distance == 0) {
                        // Si encuentra una coincidencia exacta, devuelve el número de teléfono inmediatamente
                        phoneNumber = number
                        return phoneNumber
                    } else if (distance < bestMatchDistance) {
                        bestMatch = name
                        bestMatchDistance = distance
                        phoneNumber = number
                    }
                }
            }
        }

        // Devuelve el número de teléfono de la mejor coincidencia encontrada
        if (bestMatch != null) {
            return phoneNumber
        }

        return null
    }
        private fun levenshteinDistance(s: String, t: String): Int {
            val m = s.length
            val n = t.length

            val d = Array(m + 1) { IntArray(n + 1) }

            for (i in 0..m) {
                d[i][0] = i
            }

            for (j in 0..n) {
                d[0][j] = j
            }

            for (j in 1..n) {
                for (i in 1..m) {
                    if (s[i - 1] == t[j - 1]) {
                        d[i][j] = d[i - 1][j - 1]
                    } else {
                        d[i][j] = minOf(
                            d[i - 1][j] + 1,
                            d[i][j - 1] + 1,
                            d[i - 1][j - 1] + 1
                        )
                    }
                }
            }

            return d[m][n]
        }
        private fun getContactList(context: Context): Map<String, String> {
            val contactList = mutableMapOf<String, String>()

            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumnIndex)
                    val phoneNumber = cursor.getString(numberColumnIndex)

                    contactList[name] = phoneNumber
                }
            }

            return contactList
        }


        fun interpretActionCall(context: Context, oracion: String) {
            Log.d("LLAMANDO", "REALIZANDO LLAMADAA")
            // Expresión regular para extraer números de teléfono
            val regexTelefono = "\\d{9,}"

            // Expresiones regulares para activar el altavoz y el Bluetooth
            val regexAltavoz = "(activa[me]* el altavoz)|(pon[me]* en altavoz)|(enciende el altavoz)"
            val regexBluetooth = "(activa[me]* el bluethood)|(activa[me]* el manos libres)"

            val patternTelefono = Pattern.compile(regexTelefono)
            val patternAltavoz = Pattern.compile(regexAltavoz, Pattern.CASE_INSENSITIVE)
            val patternBluetooth = Pattern.compile(regexBluetooth, Pattern.CASE_INSENSITIVE)

            // Inicializar variables
            var numeroTelefono: String? = null
            var activarAltavoz = false
            var activarBluetooth = false

            // Buscar número de teléfono en la oración
            val matcherTelefono = patternTelefono.matcher(oracion)
            if (matcherTelefono.find()) {
                numeroTelefono = matcherTelefono.group()
                Log.d("CALLSYSTEM", "B $numeroTelefono")



            } else {
                // Si no se encuentra un número, buscar contacto por nombre
                    // Usar la función buscarContactoPorNombre para obtener el número de teléfono
                    numeroTelefono = buscarContactoPorOracion(context, oracion)

            }

            // Verificar si se activa el altavoz
            val matcherAltavoz = patternAltavoz.matcher(oracion)
            activarAltavoz = matcherAltavoz.find()

            // Verificar si se activa el Bluetooth
            val matcherBluetooth = patternBluetooth.matcher(oracion)
            activarBluetooth = matcherBluetooth.find()

            llamarNumero(context = context, numero = numeroTelefono.toString(), altavoz = activarAltavoz, bluetooth = activarBluetooth)


        }



    }
}


