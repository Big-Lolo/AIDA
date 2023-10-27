package com.example.aida.serialization
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel.MapMode
import java.util.Properties



private fun initializeStanfordNLP(): StanfordCoreNLP {
    // Crea un conjunto de propiedades para configurar Stanford NLP
    val props = Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma") // Puedes configurar otros annotators según tus necesidades
    props.setProperty("tokenize.language", "es") // Especifica el idioma, en este caso, español

    // Configura el modelo de lenguaje (debes proporcionar la ruta correcta al modelo descargado)
    props.setProperty("pos.model", "ruta/al/modelo/spanish.tagger") // Ejemplo de modelo para el etiquetado POS
    props.setProperty("lemma.model", "ruta/al/modelo/spanish-lemmatizer.txt") // Ejemplo de modelo para lematización

    // Inicializa Stanford NLP con las propiedades configuradas
    return StanfordCoreNLP(props)
}

fun loadTFLiteModel(modelPath: String): Interpreter {
    val modelFile = File(modelPath)
    val modelBuffer: MappedByteBuffer
    val interpreter: Interpreter

    try {
        val modelChannel = FileInputStream(modelFile).channel
        modelBuffer = modelChannel.map(MapMode.READ_ONLY, 0, modelChannel.size())
        interpreter = Interpreter(modelBuffer)
    } catch (e: Exception) {
        throw RuntimeException("Error al cargar el modelo TensorFlow Lite: $e")
    }

    return interpreter
}




// Clase para representar la estructura de datos de intents
@Serializable
data class Intent(val tag: String, val responses: List<String>)

// Clase para representar la estructura de datos de WordsAndClasses (words y classes)
@Serializable
data class WordsAndClasses(val words: List<String>, val classes: List<String>)

val ERROR_THRESHOLD = 0.68

// Cargar datos desde archivos JSON y binarios (pkl)
val modelPath = "ruta/al/modelo/chatbot_model_v5.tflite" // Reemplaza con la ruta correcta
val interpreter = loadTFLiteModel(modelPath)


val json = File("intents.json").readText()
val type = object : TypeToken<List<Intent>>() {}.type
val intents: List<Intent> = Gson().fromJson(json, type)

data class Intent(val tag: String, val patterns: List<String>, val responses: List<String>)




val words: List<String> = FileInputStream("words.pkl").use { fileInputStream ->
    ObjectInputStream(fileInputStream).use { objectInputStream ->
        objectInputStream.readObject() as List<String>
    }
}


val classes: List<String> = FileInputStream("classes.pkl").use { fileInputStream ->
    ObjectInputStream(fileInputStream).use { objectInputStream ->
        objectInputStream.readObject() as List<String>
    }
}






val stanfordNLP = initializeStanfordNLP()

fun cleanUpSentence(sentence: String): List<String> {
    val nlp = initializeStanfordNLP()
    val annotation = Annotation(sentence)
    nlp.annotate(annotation)

    val sentenceWords = mutableListOf<String>()

    for (sentence in annotation.get(CoreAnnotations.SentencesAnnotation::class.java)) {
        for (token in sentence.get(CoreAnnotations.TokensAnnotation::class.java)) {
            val lemma = token.get(CoreAnnotations.LemmaAnnotation::class.java)
            if (lemma !in setOf(" ", "\n")) {
                sentenceWords.add(lemma)
            }
        }
    }

    return sentenceWords
}

fun bagOfWords(sentence: String, words: List<String>): IntArray {
    val sentenceWords = cleanUpSentence(sentence)

    val bag = IntArray(words.size) { 0 }

    for (w in sentenceWords) {
        for (i in words.indices) {
            if (words[i] == w) {
                bag[i] = 1
            }
        }
    }
    return bag
}





fun predictClass(sentence: String, interpreter: Interpreter, errorThreshold: Float, classes: List<String>): List<Map<String, String>> {
    val bow = bagOfWords(sentence, words)

    // Realiza la predicción con el modelo
    val inputData = Array(1) { bow.map { it.toFloat() }.toFloatArray() }
    val outputData = Array(1) { FloatArray(classes.size) }
    interpreter.run(inputData, outputData)

    val res = outputData[0]

    val results = mutableListOf<Pair<Int, Float>>()

    for (index in res.indices) {
        val r = res[index]
        if (r > errorThreshold) {
            results.add(Pair(index, r))
        }
    }

    val sortedResults = results.sortedByDescending { it.second }
    val returnList = mutableListOf<Map<String, String>>()

    if (sortedResults.isEmpty() || sortedResults[0].second < errorThreshold) {
        returnList.add(mapOf("intent" to "Desconocido", "probability" to sortedResults[0].second.toString()))
    } else {
        for (r in sortedResults) {
            returnList.add(mapOf("intent" to classes[r.first], "probability" to r.second.toString()))
        }
    }

    return returnList
}