package com.example.aida.serialization

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream


class WordsAndClasses(private val modelInterpreter: Interpreter, private val context: Context) {

    private val assetManager = context.assets
    private val jsonMapper = jacksonObjectMapper()
    private val intentsDoc: Map<String, Any>
    private val words: List<String>
    private val classes: List<String>


    init {
        val assetManager = context.assets

        // Cargar el archivo intents.json
        val intentsInputStream = assetManager.open("intents.json")
        intentsDoc = jsonMapper.readValue(intentsInputStream)
        intentsInputStream.close()

        // Cargar el archivo words.json
        val wordsInputStream = assetManager.open("words.json")
        val wordsJson = wordsInputStream.bufferedReader().use { it.readText() }
        words = jsonMapper.readValue(wordsJson)
        wordsInputStream.close()

        // Cargar el archivo classes.json
        val classesInputStream = assetManager.open("classes.json")
        val classesJson = classesInputStream.bufferedReader().use { it.readText() }
        classes = jsonMapper.readValue(classesJson)
        classesInputStream.close()
    }



    //fun cleanUpSentence(sentence: String): List<String> {
      //  val words = sentence.split(Regex("\\s+"))
        //    .map { it.replace(Regex("[^a-zA-Z0-9]"), "").lowercase() }
          //  .filter { it.isNotBlank() }
        //return words
    //}

    fun cleanUpSentence(sentence: String): List<String> {
        // Tokenizar y lematizar la oración usando modelos de OpenNLP
        val tokenizerModel = TokenizerModel(FileInputStream("en-token.bin"))
        val tokenizer = TokenizerME(tokenizerModel)
        val tokens = tokenizer.tokenize(sentence)

        val posModel = POSModel(FileInputStream("en-pos-maxent.bin"))
        val posTagger = POSTaggerME(posModel)
        val tags = posTagger.tag(tokens)

        val lemmatizerDict = DictionaryLemmatizer(FileInputStream("en-lemmatizer.dict"))
        val lemmas = lemmatizerDict.lemmatize(tokens, tags)

        return lemmas.toList()
    }

    fun bagOfWords(sentence: String): IntArray {
        val sentenceWords = cleanUpSentence(sentence)
        val bag = IntArray(words.size) { 0 }

        for (w in sentenceWords) {
            for ((i, word) in words.withIndex()) {
                if (word == w) {
                    bag[i] = 1
                }
            }
        }
        return bag
    }


    fun predictClass(sentence: String): List<Map<String, String>> {
        val bow = bagOfWords(sentence)
        val input = Array(1) { FloatArray(bow.size) }
        input[0] = bow.map { it.toFloat() }.toFloatArray()

        val output = Array(1) { FloatArray(classes.size) }
        modelInterpreter.run(input, output)

        val errorThreshold = 0.68

        val maxProb = output[0].maxOrNull() ?: 0f
        if (maxProb < errorThreshold) {
            return listOf(mapOf("intent" to "Desconocido", "probability" to maxProb.toString()))
        }

        val results = output[0].mapIndexed { index, value ->
            if (value > errorThreshold) index to value else null
        }.filterNotNull()

        results.sortedByDescending { it.second }

        val returnList = results.map { result ->
            mapOf("intent" to classes[result.first], "probability" to result.second.toString())
        }

        return returnList
    }


    fun getResponse(intentsList: List<Map<String, Any>>, intentsJson: Map<String, Any>): Pair<String, String?> {
        val tag = intentsList[0]["intent"] as String
        if (tag == "Desconocido") {
            return Pair("Lo siento, no entiendo lo que quieres decir. ¿Podrías reformular tu pregunta?", null)
        }

        val list_of_intents = intentsJson["intents"] as List<Map<String, Any>>
        var result = ""
        var functions: String? = null

        for (i in list_of_intents) {
            if (i["tag"] == tag) {
                val responses = i["responses"] as List<String>
                result = responses.random()

                val function = i["function"]
                if (function != null) {
                    functions = function.toString()
                }
                break
            }
        }

        return Pair(result, functions)
    }

    fun responseClass(sentence: String): Pair<String, String?> {

        var a = predictClass(sentence)
        return getResponse(a, intentsDoc)
    }


}
