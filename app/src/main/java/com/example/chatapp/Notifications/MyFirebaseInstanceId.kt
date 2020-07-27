package com.example.chatapp.Notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


//unique token for each notification when a user send message to another user
class MyFirebaseInstanceId :FirebaseMessagingService()
{
    override fun onNewToken(p0: String)
    {
        super.onNewToken(p0)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token

        if(firebaseUser!=null)
        {
            updateToken(refreshToken)
        }
    }

    //identify users using their unique id that is in the firebase Users node.
    //for each notification create a separate parent node in the database by the name Tokens
    // which will contain the each id for the specific notification with the help of which we will send notification from user A to user B.

    private fun updateToken(refreshToken: String?)
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference().child("Tokens")
        val token = Token(refreshToken!!)
        ref.child(firebaseUser!!.uid).setValue(token)

    }

}