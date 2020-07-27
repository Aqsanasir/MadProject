package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        register_welcome_btn.setOnClickListener {

           //if the user click on the register button in welcome we are going to send the user to the register activity
            val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        login_welcome_btn.setOnClickListener {

            //if the user click on the login button in welcome we are going to send the user to the login activity
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }




    }

    //whenever the app starts on the welcome activity basically it will check in this on start() method that
// if the firebaseUser = null means that the user is already logged in

    override fun onStart() {
        super.onStart()

        //This is basically for the validation of user.
    //if the user is already logged in then it will direct the user to the main app/activity otherwise the user must login
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //means that the user is already logged in
        if(firebaseUser != null)
        {
        //we have to create intent in order to send the user from the welcome activity to the main activity

            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()



        }
    }




}

