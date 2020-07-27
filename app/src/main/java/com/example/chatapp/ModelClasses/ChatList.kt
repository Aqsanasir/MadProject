package com.example.chatapp.ModelClasses

class ChatList {


    //we need to create a model class in order to retrieve the chatList
    // that have single child i.e; id for both the sender and the receiver , on our chat fragment

    private var id : String = ""

    constructor()

    constructor(id: String) {
        this.id = id
    }


    fun getId():String?
    {
        return id
    }

    fun setId(id: String?)
    {
        this.id = id!!
    }

}