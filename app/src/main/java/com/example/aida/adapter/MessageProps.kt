package com.example.aida.adapter

class MessageProps(content:String, user:Boolean) {

    var message: String
    var isUser: Boolean

    init {
        message = content
        isUser = user
    }


}