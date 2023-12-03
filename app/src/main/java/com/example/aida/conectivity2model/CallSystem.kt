package com.example.aida.conectivity2model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import com.example.aida.chat
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

interface ChatListenerr {
    fun sendMessage(message: String)
}
class CallSystem {
    companion object{
        private var chatListener: ChatListenerr? = null
        fun setChatListener(listener: ChatListenerr) {
            chatListener = listener
        }
        fun sendMessageToUser(message: String) {
            // Enviar el mensaje al chat del usuario a través del fragmento
            val fragment = chat.getInstance()
            fragment.sendMessage(message)
        }
        private fun llamarNumero(context: Context, numero: String, altavoz: Boolean, bluetooth: Boolean) {
        val intent = Intent(Intent.ACTION_CALL)

        intent.data = Uri.parse("tel:$numero")

            // Configurar el altavoz si es necesario
            if (altavoz) {
                intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            }

            // Configurar el Bluetooth si es necesario y es compatible
            if (bluetooth ) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                    intent.putExtra("android.bluetooth.headset", true)
                } else {
                    // Puedes manejar la lógica para solicitar al usuario que habilite Bluetooth aquí
                }
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

    }

    @SuppressLint("Range")
    fun buscarContactoPorOracion(context: Context, oracion: String): String? {
        val contactList = getContactList(context)
        var phoneNumber: String? = null
        var bestMatch: String? = null
        var bestMatchSimilarity = 0.0

        val palabras = oracion.split(" ")

        for (palabra in palabras) {
            for ((name, number) in contactList) {
                val normalizedName = normalizeString(name)
                val normalizedPalabra = normalizeString(palabra)

                // Calcular la similitud utilizando un algoritmo avanzado (por ejemplo, Jaro-Winkler)
                val similarity = calculateSimilarity(normalizedName, normalizedPalabra)

                if (similarity >= 0.8) {
                    // Si la similitud supera el umbral, actualiza la mejor coincidencia
                    if (similarity > bestMatchSimilarity) {
                        bestMatch = name
                        bestMatchSimilarity = similarity
                        phoneNumber = number
                    }
                }
            }
        }
        Log.d("telContact", "El telefono del contacto es $phoneNumber")
        return phoneNumber
    }

        fun calculateSimilarity(text1: String, text2: String): Double {
            val similarity = StringUtils.getJaroWinklerDistance(text1, text2)
            return similarity
        }

        fun normalizeString(text: String): String {
            // Convertir a minúsculas
            val normalizedText = text.lowercase()

            // Eliminar caracteres especiales y espacios adicionales
            val pattern = Pattern.compile("[^a-z0-9 ]")
            val matcher = pattern.matcher(normalizedText)
            val cleanedText = matcher.replaceAll("").trim()

            return cleanedText
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


        fun interpretActionCall(context: Context, txt: String) {

            Log.d("LLAMANDO", "REALIZANDO LLAMADAA")
            val oracion:String = convertirTextoANumeros(txt)
            Log.d("TEL_RESPUESTA", "El tel es: $oracion")
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
                numeroTelefono = buscarContactoPorOracion(context, oracion)

                if(numeroTelefono != null) {
                    numeroTelefono = convertirTextoANumeros(numeroTelefono)
                }else{
                    sendMessageToUser("Lo siento, no pude encontrar a ese contacto en tu agenda")
                    return
                }
            }

            // Verificar si se activa el altavoz
            val matcherAltavoz = patternAltavoz.matcher(oracion)
            activarAltavoz = matcherAltavoz.find()

            // Verificar si se activa el Bluetooth
            val matcherBluetooth = patternBluetooth.matcher(oracion)
            activarBluetooth = matcherBluetooth.find()

            Log.d("Tel2call", "TELEFONO: ${numeroTelefono.toString()}")

            if (numeroTelefono == null) {
                // Notificar al usuario con un mensaje de que no se encontró el número
                sendMessageToUser("Lo siento, no pude encontrar ese número")
            } else {
                llamarNumero(
                    context = context,
                    numero = numeroTelefono.toString(),
                    altavoz = activarAltavoz,
                    bluetooth = activarBluetooth
                )
            }
        }

        // Función para convertir texto en números
        fun convertirTextoANumeros(texto: String?): String {
            val numeros: Map<String, String> = mapOf(
                "cero" to "0",
                "uno" to "1",
                "dos" to "2",
                "tres" to "3",
                "cuatro" to "4",
                "cinco" to "5",
                "seis" to "6",
                "siete" to "7",
                "ocho" to "8",
                "nueve" to "9"
                // Agrega más palabras y números según sea necesario
            )

            var numeroConvertido = texto

            // Encontrar números escritos en palabras
            val pattern = Pattern.compile("\\b(?:${numeros.keys.joinToString("|")})\\b")
            val matcher = pattern.matcher(numeroConvertido)

            while (matcher.find()) {
                val palabraNumero = matcher.group()
                val numeroReemplazo = numeros[palabraNumero.toLowerCase()]
                numeroConvertido = numeroConvertido?.replace("\\b$palabraNumero\\b".toRegex(), numeroReemplazo ?: "")
            }

            // Encontrar números y palabras
            val numerosEncontrados = "\\b\\d+\\b".toRegex().findAll(numeroConvertido ?: "")
                .map { it.value }
                .toList()

            val palabrasEncontradas = "\\b\\D+\\b".toRegex().findAll(numeroConvertido ?: "")
                .map { it.value }
                .toList()

            // Juntar números y palabras
            val numerosJuntos = numerosEncontrados.joinToString("")
            val palabrasJuntas = palabrasEncontradas.joinToString(" ")

            // Concatenar números y palabras
            return "$palabrasJuntas $numerosJuntos"
        }



    }
}


