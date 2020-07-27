package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.AdapterClasses.ChatsAdapter
import com.example.chatapp.Fragments.APIService
import com.example.chatapp.ModelClasses.Chat
import com.example.chatapp.ModelClasses.Users
import com.example.chatapp.Notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {


    var userIdVisit : String = ""
    var firebaseUser : FirebaseUser? = null
    var chatsAdapter : ChatsAdapter? = null
    var mChatList : List<Chat>? = null
    lateinit var recycler_view_chats : RecyclerView
    var reference : DatabaseReference? = null

    var notify = false
    var apiService : APIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)


        //backbutton
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {

            //val intent = Intent(this@MessageChatActivity, WelcomeActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            // startActivity(intent)
            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        //firebase user will send a message to this userIdVisit e.g; John

        intent = intent
        //receiver id
        userIdVisit = intent.getStringExtra("visit_id")
        //sender id
        firebaseUser = FirebaseAuth.getInstance().currentUser


        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager


             // retrieve the information i.e; username and profile picture of the receiver
        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                val user : Users? = p0.getValue(Users::class.java)
                username_mchat.text = user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)


                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())



            }

            override fun onCancelled(p0: DatabaseError) {

            }



        })

        send_message_btn.setOnClickListener{
             notify = true
            val message = text_message.text.toString()

            if(message == "")
            {
                Toast.makeText(this@MessageChatActivity, "Please write a message, first...", Toast.LENGTH_LONG).show()
            }
            else
            {
                //save the message to the database
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
                //fisrt we have to get the firebase user and also the message
                //which user to which user basically we are going to send this message
                //online user will select and we have to get the id of that particular user to whom we are sending message.
            }

          text_message.setText("")
        }


        //send the image file to the receiver
        attach_image_file_btn.setOnClickListener{
             notify = true
            //we the user click on file button we want the to send the user to the mobile phone gallery from where the user will select the image.
            //once user select the image we have to get that image file by calling OnActivityResultMethod()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            //file type is images
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)

        }
        seenMessage(userIdVisit)

    }


     private fun sendMessageToUser(senderId: String, receiverId: String?, message: String)
     {

         val reference = FirebaseDatabase.getInstance().reference
         //create a unique key for each and every message in order to store it inside the database
         val messageKey = reference.push().key

         //saving the all message information to the database with the unique key for each message.
         val messageHashMap = HashMap<String, Any?>()
         messageHashMap["sender"] = senderId
         messageHashMap["message"] = message
         messageHashMap["receiver"] = receiverId
         messageHashMap["isseen"] = false
         //url of image message
         messageHashMap["url"] = ""
         messageHashMap["messageId"] = messageKey
         reference.child("Chats")
             .child(messageKey!!)
             .setValue(messageHashMap)
             .addOnCompleteListener{task ->

                 //if task is successful we are going to save chats which we will be displaying on chat fragment.
                 if(task.isSuccessful)
                 {
                      // we are going to save data for chat list in order to retrieve and display the last messages for each user
                     //display the badges for unread messages

                     val chatsListReference = FirebaseDatabase.getInstance()
                         .reference
                         .child("ChatList")
                         .child(firebaseUser!!.uid)
                         .child(userIdVisit)
                     //we are doing it for the sender and under sender saving the receiver

                     chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{

                         override fun onDataChange(p0: DataSnapshot)
                         {
                             if(!p0.exists())
                             {
                                 //to the chatList for the online user we have to add the receiver
                                 chatsListReference.child("id").setValue(userIdVisit)
                             }

                             //for the receiver
                             val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                 .reference
                                 .child("ChatList")
                                 .child(userIdVisit)
                                 .child(firebaseUser!!.uid)
                             //for the receiver we have to add the sender
                             chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)

                         }

                         override fun onCancelled(p0: DatabaseError) {

                         }

                     })

                     //chatList for retrieving the total number of unread messages and also the last message for our chat fragment

                 }

             }

         //implement the push notifications using fcm(firebase cloud messaging)

         val usersReference = FirebaseDatabase.getInstance().reference
             .child("Users").child(firebaseUser!!.uid)
         usersReference.addValueEventListener(object : ValueEventListener{

             override fun onDataChange(p0: DataSnapshot)
             {
                 val user = p0.getValue(Users::class.java)
                 if(notify)
                 {
                     sendNotification(receiverId, user!!.getUserName(), message)
                 }
                 notify = false

             }

             override fun onCancelled(p0: DatabaseError) {

             }

         })

    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String)
    {
        //reference to  the database to the tokens node.
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                for(dataSnapshot in p0.children)
                {
                    val token : Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$userName: $message",
                        "New Message",
                        userIdVisit

                    )

                    val sender = Sender(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{

                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            )
                            {
                                if(response.code() == 200)
                                {
                                    if(response.body()!!.success !== 1)
                                    {
                                        Toast.makeText(this@MessageChatActivity,"Failed, Nothing happen.",Toast.LENGTH_LONG).show()
                                    }
                                }

                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {


                            }


                        })
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data!!.data!=null)

        {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait....")
            progressBar.show()

            // then we have to store it inside the firebase storage
            //creating a folder inside the firebase storage for saving all the chat images.
            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")

            //create a reference to the database in order to save the chat images in the database
            val ref = FirebaseDatabase.getInstance().reference
            //unique id for storing each message
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task->

                if(!task.isSuccessful)
                {

                    task.exception?.let{

                        throw it
                    }

                }
                //get the Url
                return@Continuation filePath.downloadUrl

                //at the time when the image is uploaded successfully
                //next thing is to store this Url inside the firebase RealTime Database
            }).addOnCompleteListener{task ->

                if(task.isSuccessful)
                {
                      //save the image file to the database
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    //url of image message
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener{task ->

                            if(task.isSuccessful)
                            {
                                progressBar.dismiss()
                                //implement the push notifications using fcm(firebase cloud messaging)

                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)
                                reference.addValueEventListener(object : ValueEventListener{

                                    override fun onDataChange(p0: DataSnapshot)
                                    {
                                        val user = p0.getValue(Users::class.java)
                                        if(notify)
                                        {
                                            sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
                                        }
                                        notify = false

                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                })
                            }

                        }




                }
            }

        }

    }


    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")


        reference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                (mChatList as ArrayList<Chat>).clear()
                for(snapshot in p0.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)


                  //all messages belong to sender and receiver not anybody else
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {

                        (mChatList as ArrayList<Chat>).add(chat)
                    }

                    chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!! )
                    recycler_view_chats.adapter = chatsAdapter
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })


    }

    var seenListener : ValueEventListener? = null
//seen Message functionality
    private fun seenMessage(userId: String)
    {

        //create a reference to chats Node that is in FirebaseDatabase
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {

                for(dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId))

                    {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }


    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)


    }

}
