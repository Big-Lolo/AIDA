package com.example.aida

import com.example.aida.serialization.WordsAndClasses
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

fun loadWordsAndClassesFromPickle(filePath: String): WordsAndClasses {
    val json = Json { isLenient = true; ignoreUnknownKeys = true }
    val fileContent = File(filePath).readText()
    return json.decodeFromString(fileContent)
}

val wordsAndClasses = loadWordsAndClassesFromPickle("words_and_classes.pkl")


val words = wordsAndClasses.words
val classes = wordsAndClasses.classes