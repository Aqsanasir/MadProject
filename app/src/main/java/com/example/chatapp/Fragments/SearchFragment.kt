package com.example.chatapp.Fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.AdapterClasses.UserAdapter
import com.example.chatapp.ModelClasses.Users

import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * A simple [Fragment] subclass.
 */

//in search fragment first retrieve the users from the database and to store it in a list and
//we have to pass this list as a parameter to the userAdapter class and the context from the search fragment and isChatCheck boolean variable

class SearchFragment : Fragment() {

    //create instance of userAdapter
    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var recyclerView : RecyclerView? = null
    private var searchEditText : EditText? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchUsersET)


       //save all users in arrayList
        mUsers = ArrayList()
        retrieveAllUsers()

        searchEditText!!.addTextChangedListener(object : TextWatcher
        {


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                 searchForUsers(s.toString().toLowerCase())
            }


            override fun afterTextChanged(s: Editable?) {

            }



        })

        return view
    }

    //retrieving all the users and displaying on the recyclerView
    private fun retrieveAllUsers()
    {
       var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        //create reference to users node
        //we are retrieving all the users not a specific user
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener
        {

            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()

                //if searchEditText is empty then display all users on the recyclerView
                if(searchEditText!!.text.toString() == "")
                {
                    //get all the users from our user node in firebase database
                    for(snapshot in p0.children)
                    {
                        val user : Users? = snapshot.getValue(Users::class.java)
                        //an online user can not search his/her profile
                        if(!(user!!.getUID()).equals(firebaseUserID))
                        {
                            // retrieving all the users and adding it to arrayList mUsers one by one except user own's account
                            (mUsers as ArrayList<Users>).add(user)

                        }
                    }

                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    recyclerView!!.adapter =  userAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError)
            {

            }


        })
    }


    private fun searchForUsers(str : String)
    {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        //create a query on over user reference
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")


        queryUsers.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()

                for(snapshot in p0.children)
                {
                    val user : Users? = snapshot.getValue(Users::class.java)
                    //an online user can not search his/her profile
                    if(!(user!!.getUID()).equals(firebaseUserID))
                    {
                        // retrieving all the users and adding it to arrayList mUsers one by one except user own's account
                        (mUsers as ArrayList<Users>).add(user)

                    }
                }
                //making a query on over the search child
                //add a recycler view on the search fragment
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter =  userAdapter

            }

        })

    }


}
