package com.example.chatapp.Fragments


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.chatapp.ModelClasses.Users

import com.example.chatapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

/**
 * A simple [Fragment] subclass.
 */


//retrieve the cover image and the profile image from the database that has been stored there.
class SettingsFragment : Fragment() {

    //create a reference to our database basically reference to user node
    var usersReference : DatabaseReference? = null
    var firebaseUser :FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri : Uri? = null
    //need a reference to firebase storage where we basically put all our images and the cover images
    private var storageRef : StorageReference? = null
    private var coverChecker : String? = ""
    //check the button status
    private var socialChecker : String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

         firebaseUser = FirebaseAuth.getInstance().currentUser
         usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        // we are going to create a folder inside our firebase storage bucket where we will have all our cover and profile images.
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")



        usersReference!!.addValueEventListener(object : ValueEventListener

        {

            override fun onDataChange(p0: DataSnapshot)
            {
             //if user exists in the database whose id we are getting
                if(p0.exists())
                {
                    val user : Users? = p0.getValue(Users::class.java)

                    if(context!=null)
                    {
                        view.username_settings.text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image_settings)

                    }

                }
            }


            override fun onCancelled(p0: DatabaseError) {

            }

        })

        //if a user click on it we are going to send the user to the mobile phone gallery from where user will pick an image
        view.profile_image_settings.setOnClickListener{

            pickImage()
        }

        view.cover_image_settings.setOnClickListener{

            //when user click on the cover image view user is going to change the cover image
            coverChecker = "cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener{

            socialChecker = "facebook"
            setSocialLinks()
        }

        view.set_instagram.setOnClickListener{

            socialChecker = "instagram"
            setSocialLinks()
        }

        view.set_website.setOnClickListener{

            socialChecker = "website"
            setSocialLinks()
        }

        return view
    }

    private fun setSocialLinks() {

        val builder : AlertDialog.Builder =
            AlertDialog.Builder(context!!, R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if(socialChecker == "website")
        {
            builder.setTitle("Write URL:")
        }
        else
        {
            builder.setTitle("Write username:")
        }

        val editText = EditText(context)

        if(socialChecker == "website")
        {
            //in this format user has to write the website URL in the editText.
            editText.hint = "e.g www.google.com"
        }
        else
        {
            //in this format user has to write the username
            editText.hint = "e.g irfa645"
        }
        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, which ->

            //get the text from the editText whatever user writes in the editText.
            val str = editText.text.toString()

            if(str == null)
            {
                Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()

            }

            else
            {
                //save to the database
                saveSocialLinks(str)
            }
        })

      builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{
          dialog, which ->

          dialog.cancel()
      })

        builder.show()
    }

    private fun saveSocialLinks(str: String) {

        val mapSocial = HashMap<String, Any>()


        when(socialChecker)
        {
            "facebook" ->
            {
             mapSocial["facebook"] = "https://m.facebook.com/$str"
            }

            "instagram" ->
            {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }

            "website" ->
            {
                mapSocial["website"] = "https://$str"
            }


        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener {
                task ->

            if(task.isSuccessful)
            {
                Toast.makeText(context, "Updated Successfully..", Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun pickImage()
    {
        //in order to send the user to the mobile phone gallery use Intent
        val intent = Intent()
        //file type is images
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                                                                            // if any image has been selected.
        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null)
        {
             //get the data using this data
            // it will pass the image to the imageUri
            imageUri = data.data
            Toast.makeText(context, "Uploading....", Toast.LENGTH_LONG).show()
            //upload image to the firebase Storage and to the Firebase RealTime Database
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase()
    {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading, please wait....")
        progressBar.show()

        if(imageUri!=null)
        {
            //so we have our reference to the folder
            // in order to make it unique we will get the time on which user will upload the image
            //storing the images inside the folder UserImages that is in firebase Storage by the .jpg format.
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> {task->

                if(!task.isSuccessful)
                {

                    task.exception?.let{

                        throw it
                    }

                }
                   //get the Url
                return@Continuation fileRef.downloadUrl

                //at the time when the image is uploaded successfully
                //next thing is to store this Url inside the firebase RealTime Database
            }).addOnCompleteListener{task ->

                if(task.isSuccessful)
                {

                   val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    //now we have to differentiate between the profile and cover Images.
                    if(coverChecker == "cover")
                    {
                        //we will save the image as cover image
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    }

                    //otherwise we will save the image as profile image.
                    else
                    {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        usersReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""

                    }

                    progressBar.dismiss()
                }
            }


        }


    }




}
