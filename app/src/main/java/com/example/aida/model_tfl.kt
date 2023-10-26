package com.example.aida
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class model_tfl(private val context: Context) {
    private lateinit var interpreter: Interpreter

    fun loadModel(modelPath: String) {
        val options = Interpreter.Options()
        interpreter = Interpreter(loadModelFile(modelPath), options)
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    //fun doInference(inputData: FloatArray): FloatArray {



    //}
}