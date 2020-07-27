package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {


    //let just initailize aur firebaseAuth
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val toolbar : Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        //if the user click on back button ..click listener on back button in toolbar of login activity then finish the current activity
        toolbar.setNavigationOnClickListener {
            //when the back button is clicked in the toolbar user will go to the welcome activity
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        login_btn.setOnClickListener {
            loginUser()
        }


    }

    private fun loginUser() {

        val email : String = email_login.text.toString()
        val password : String = password_login.text.toString()

        if (email == "")
        {
            Toast.makeText(this@LoginActivity, "Please Enter Email", Toast.LENGTH_LONG).show()
        }
        else if( password == "")
        {
            Toast.makeText(this@LoginActivity, "Please Enter Password", Toast.LENGTH_LONG).show()
        }
        else
        {
            //sign in the user using the firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{task ->
                    //user is successfully logged in
                    if(task.isSuccessful)
                    {
                    // we want the user to send to the mainpage/Main Activity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }
                    else
                    {
                        Toast.makeText(this@LoginActivity, "Error Message:" + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }

    }



}
