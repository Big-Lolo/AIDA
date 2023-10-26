import numpy as np
import json
import pickle
import spacy
from os.path import dirname, join
nlp = spacy.load('es_core_news_sm')



filename_intents = join(dirname(__file__), "intents.json")
filename_words = join(dirname(__file__), "words.pkl")
filename_classes = join(dirname(__file__), "classes.pkl")

intents = json.loads(open(filename_intents).read())
words = pickle.load(open(filename_words, 'rb'))
classes = pickle.load(open(filename_classes, 'rb'))


def clean_up_sentence(sentence):
    # Tokenizar y lematizar la oración usando spaCy
    doc = nlp(sentence)
    sentence_words = [token.lemma_ for token in doc if token.lemma_ not in [" ", "\n"]]
    return sentence_words

def bag_of_words(sentence):
    sentence_words = clean_up_sentence(sentence)
    bag = [0] * len(words)
    for w in sentence_words:
        for i, word in enumerate(words):
            if word == w:
                bag[i] = 1
    return np.array(bag)


def package_of_words(oracion):
    bow = bag_of_words(oracion)
    return bow