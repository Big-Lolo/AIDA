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

class SpeechRecognizerManager(private val context: Context) : RecognitionListener {

    private var recognizer: SpeechRecognizer? = null

    fun initialize() {
        Log.d("DEBUG", "Iniciando el método initialize")

        val assetManager = context.assets

        // Copia los archivos necesarios al directorio de archivos internos de la aplicación
        val assetDir = File(context.filesDir, "assets")
        assetDir.mkdirs()
        copyAssetDirectory(assetManager, "en-us-ptm", assetDir)
        copyAssetFile(assetManager, "cmudict-en-us.dict", File(assetDir, "cmudict-en-us.dict").path)
        copyAssetFile(assetManager, "hey_aida.jsgf", File(assetDir, "hey_aida.jsgf").path)

        recognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(File(assetDir, ""))
            .setDictionary(File(assetDir, "cmudict-en-us.dict"))
            .setRawLogDir(assetDir)
            .recognizer
        recognizer?.addListener(this)

        val keywordFile = File(assetDir, "hey_aida.jsgf")
        recognizer?.addKeywordSearch("heyaida", keywordFile)
    }
    private fun copyAssetDirectory(assetManager: AssetManager, sourceDir: String, destinationDir: File) {
        Log.d("DEBUG", "Copiando directorio: $sourceDir a ${destinationDir.path}")

        val files = assetManager.list(sourceDir) ?: return
        for (filename in files) {
            val sourcePath = "$sourceDir/$filename"
            val destinationPath = File(destinationDir, filename).path
            if (assetManager.list(sourcePath)?.isNotEmpty() == true) {
                // Es un directorio
                File(destinationPath).mkdirs()
                copyAssetDirectory(assetManager, sourcePath, File(destinationPath))
            } else {
                // Es un archivo
                copyAssetFile(assetManager, sourcePath, destinationPath)
            }
        }
    }

    private fun copyAssetFile(assetManager: AssetManager, sourcePath: String, destinationPath: String) {
        Log.d("DEBUG", "Copiando archivo: $sourcePath a $destinationPath")

        val inputStream = assetManager.open(sourcePath)
        val outputStream = FileOutputStream(destinationPath)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }
    fun startListening() {
        recognizer?.startListening("keyword")
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
    }

    override fun onResult(hypothesis: Hypothesis?) {
        // Manejar resultados finales del reconocimiento
        if (hypothesis != null) {
            val result = hypothesis.hypstr
            if (result == "Hey Aida") {
                Log.d("PROCESO_DE_VOZ", "Comando de voz detectado correctamente!")
            }
        }
    }

    override fun onError(e: Exception?) {
        // Manejar errores del reconocimiento
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }
}