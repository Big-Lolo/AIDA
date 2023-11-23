package com.example.aida

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import edu.cmu.pocketsphinx.Hypothesis
import edu.cmu.pocketsphinx.RecognitionListener
import edu.cmu.pocketsphinx.SpeechRecognizer
import edu.cmu.pocketsphinx.SpeechRecognizerSetup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SpeechRecognizerManager(private val context: Context) : RecognitionListener {
    private val KWS_SEARCH = "wakeup"
    private val KEYPHRASE = "computer"
    private var recognizer: SpeechRecognizer? = null

    fun initialize() {
        Log.d("DEBUG", "Iniciando el método initialize")

        val assetManager = context.assets

        // Copia los archivos necesarios al directorio de archivos internos de la aplicación
        val assetDir = File(context.filesDir, "assets")
        assetDir.mkdirs()
        copyAssetDirectory(assetManager, "en-us-ptm", assetDir)
        copyAssetFile(assetManager, "cmudict-en-us.dict", File(assetDir, "cmudict-en-us.dict").path)
        //copyAssetFile(assetManager, "hey_aida.jsgf", File(assetDir, "hey_aida.jsgf").path)

        recognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(File(assetDir, ""))
            .setDictionary(File(assetDir, "cmudict-en-us.dict"))
            .setRawLogDir(assetDir)
            .setKeywordThreshold((-10.0).toFloat())
            .recognizer
        recognizer?.addListener(this)

        val keywordFile = File(assetDir, "hey_aida.jsgf")

        recognizer?.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE)
    }

    private fun copyAssetDirectory(assetManager: AssetManager, sourceDir: String, destinationDir: File) {
        Log.d("DEBUG", "Copiando directorio: $sourceDir a ${destinationDir.path}")

        val files = assetManager.list(sourceDir) ?: return
        for (filename in files) {
            val sourcePath = "$sourceDir/$filename"
            val destinationPath = File(destinationDir, filename).path
            if (isDirectory(assetManager, sourcePath)) {
                // Es un directorio
                val subDestinationDir = File(destinationDir, filename)
                subDestinationDir.mkdirs()
                copyAssetDirectory(assetManager, sourcePath, subDestinationDir)
            } else {
                // Es un archivo dentro del directorio sourceDir
                val relativePath = sourcePath.substringAfter("$sourceDir/")
                val fileDestination = File(destinationDir, relativePath)
                copyAssetFile(assetManager, sourcePath, fileDestination.path)

                // Imprimir resumen del estado
                printDirectorySummary(destinationDir)
            }
        }
    }

    private fun printDirectorySummary(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            val totalFiles = files.size
            var totalSize = 0L
            for (file in files) {
                totalSize += file.length()
            }
            Log.d("DEBUG", "Resumen de estado en ${directory.path}: $totalFiles archivos, tamaño total: $totalSize bytes")
        }
    }

    private fun isDirectory(assetManager: AssetManager, path: String): Boolean {
        return try {
            val inputStream = assetManager.open(path)
            inputStream.close()
            false
        } catch (e: IOException) {
            true
        }
    }

    private fun copyAssetFile(assetManager: AssetManager, sourcePath: String, destinationPath: String) {
        Log.d("DEBUG", "Copiando archivo: $sourcePath a $destinationPath")

        val correctedDestinationPath = if (sourcePath.contains("en-us-ptm/")) {
            appendSourcePath(destinationPath, sourcePath)
        } else {
            removeEnUsPtm(destinationPath)
        }
        Log.d("DEBUG", "Copiando archivo a ruta de: $sourcePath a $correctedDestinationPath")

        val inputStream = assetManager.open(sourcePath)
        val outputStream = FileOutputStream(correctedDestinationPath)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    private fun removeEnUsPtm(path: String): String {
        val index = path.indexOf("/en-us-ptm")
        return if (index != -1) {
            path.substring(0, index) + path.substring(index + "/en-us-ptm".length)
        } else {
            path
        }
    }
    private fun appendSourcePath(destinationPath: String, sourcePath: String): String {
        val assetsIndex = destinationPath.indexOf("/assets/")
        return if (assetsIndex != -1) {
            destinationPath.substring(0, assetsIndex + "/assets/".length) + "en-us-ptm"

        } else {
            destinationPath
        }
    }
    fun startListening() {
        recognizer?.startListening(KWS_SEARCH)
    }

    fun stopListening() {
        recognizer?.stop()
    }

    override fun onBeginningOfSpeech() {
        Log.d("onBeginningOfSpeech", "Starting")
    }

    override fun onEndOfSpeech() {
        Log.d("onEndOfSpeech", "Ending")
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        // Se llama cuando hay un resultado parcial disponible
        val result = hypothesis?.hypstr
        if (!result.isNullOrBlank()) {
            if (result == KEYPHRASE) {
                recognizer?.stop()
            }
        }
    }

    override fun onResult(hypothesis: Hypothesis?) {
        // Se llama cuando se completa el reconocimiento
        Log.d("onResult", "ONRESULT RECIBIDO")
        val result = hypothesis?.hypstr
        if (!result.isNullOrBlank()) {
            Log.d("SpeechRecognition", "EJECUTAR LA FUNCIÓN DE LO QUE SEA")

        }
        recognizer?.startListening(KWS_SEARCH)
    }

    override fun onError(e: Exception?) {
        // Manejar errores del reconocimiento
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }
}