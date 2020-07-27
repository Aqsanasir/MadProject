package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    //let just initailize aur firebaseAuth
    private lateinit var mAuth: FirebaseAuth
    //let's create a reference to our database
    private lateinit var refUsers: DatabaseReference
    //when the user is authenticated in the firebase then each user carries a id that uniquely identifies the users.
     private var firebaseUserID: String =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar : Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //if the user click on back button ..click listener on back button in toolbar of register activity then finish the current activity
        toolbar.setNavigationOnClickListener {

            //when the back button is clicked in the toolbar user will go to the welcome activity
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }



        mAuth = FirebaseAuth.getInstance()
        // when user click on the register button we have to check first either the user write in the edit text fields or not
        register_btn.setOnClickListener {
            registerUser()
        }





    }

    private fun registerUser() {
        //store the fields in variables
        val username : String = username_register.text.toString()
        val email : String = email_register.text.toString()
        val password : String = password_register.text.toString()

        if(username == "")
        {
            Toast.makeText(this@RegisterActivity, "Please Enter UserName", Toast.LENGTH_LONG).show()
        }
        else if (email == "")
        {
            Toast.makeText(this@RegisterActivity, "Please Enter Email", Toast.LENGTH_LONG).show()
        }
        else if( password == "")
        {
            Toast.makeText(this@RegisterActivity, "Please Enter Password", Toast.LENGTH_LONG).show()
        }
        else
        {
           //authenticate the user first and then save the user information in the firebase realtime database.
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    task->
                    if(task.isSuccessful)
                    {
                        //in order to save the user information in the database we need to get the user id.
                        firebaseUserID = mAuth.currentUser!!.uid
                        //child will be the users
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        //storing the id of user
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        //profile image for user (default profile image for new user like placeholder)
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatapp-d6853.appspot.com/o/profile.png?alt=media&token=7263a472-3bfa-4326-b5e8-db397d5fd597"
                        //cover photo for user (default cover photo for new user like placeholder)
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatapp-d6853.appspot.com/o/cover.jpg?alt=media&token=e198b277-57dd-4250-9270-3e52176c1b8f"
                        userHashMap["status"] = "offline"
                        //create child for searching when the user wants to search for other user
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"


                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener{task ->

                                if(task.isSuccessful)
                                {
                                    //login the user  and send it to the main activity
                                    //user must logged in and if the user click on the back button then
                                    // we are not going to send the user back to the login or register activity
                                    // until and unless the user click on log out button
                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }


                    }
                    else
                    {                                                                                //non-asserted call which means that it must contain message.
                        Toast.makeText(this@RegisterActivity, "Error Message:" + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }

            }
        }


    }
}
