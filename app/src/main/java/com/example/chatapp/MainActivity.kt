package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.chatapp.Fragments.ChatsFragment
import com.example.chatapp.Fragments.SearchFragment
import com.example.chatapp.Fragments.SettingsFragment
import com.example.chatapp.ModelClasses.Chat
import com.example.chatapp.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    //create a reference to user nodes
    // if a xyz user is online then we have to create the reference then we will retrieve the user information
    // create a reference to database of the user nodes.
    var refUsers : DatabaseReference? = null
    // in order to get the firebase uid of user we have to create a firebase user
    var firebaseUser : FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        //create a reference to user nodes
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)



        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        //for setting the toolbar title to none and set it to username and profile as we set it in xml file.
        supportActionBar!!.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        val viewPager : ViewPager = findViewById(R.id.view_pager)

        //create a reference to Chats node
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")

        //display total number of unread messages that will be implemented through number of seen messages feature
        ref!!.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

                var countUnreadMessages = 0

                for(dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.isIsSeen())
                    {
                        countUnreadMessages += 1

                    }
                }
                if(countUnreadMessages == 0)
                {
                    //By default the position of chats will be zero.
                    viewPagerAdapter.addFragment(ChatsFragment(),"Chats")

                }
                else
                {
                    viewPagerAdapter.addFragment(ChatsFragment(),"($countUnreadMessages) Chats")

                }

                viewPagerAdapter.addFragment(SearchFragment(),"Search")
                viewPagerAdapter.addFragment(SettingsFragment(),"Settings")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


        //display username and profile picture

        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError)
            {

            }

            override fun onDataChange(p0: DataSnapshot)
            {
                //first of all we have to make sure that the online firebase user exists in User node.
                //p0 represent our user reference for that specific userID
                if(p0.exists())
                {
                    // then we are going to retrieve the two things here one is profile picture and second is username
                    //we will create model class for this to do in better way
                       val user : Users? = p0.getValue(Users::class.java)
                        user_name.text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(profile_image)





                }

            }


        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId)
        {
            R.id.action_logout ->
            {
                // if the user click on the sign out button
              FirebaseAuth.getInstance().signOut()
                //send back the user to the welcome Activity
                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }

        }
        return false
    }


    internal class ViewPagerAdapter(fragmentManager : FragmentManager) : FragmentPagerAdapter(fragmentManager) {
      //here we are basically setting our fragments we have to get the title for this fragment and also the position
      // so we are using the array list for this of type fragment.
        private val fragments : ArrayList<Fragment>
        private val titles : ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()

        }


        //this will return fragment position
        override fun getItem(position: Int): Fragment {
           return fragments[position]
        }
        // this will basically return the size i.e; how many fragments we have?
        // in our case we have three fragments and we want to display these fragments.
        override fun getCount(): Int {
          return fragments.size
        }
   // we have to add this fragment to our arrayaList
       fun addFragment(fragment: Fragment, title: String)
       {
         fragments.add(fragment)
           titles.add(title)

       }

        override fun getPageTitle(i: Int): CharSequence? {
            // i is representing the position.FOR moving to the other fragment
            // e.g; by default i am on chat fragment and i want to move to settings fragment I will click on setting
            // and i will move to settings fragment.
            return titles[i]

        }
    }


    private fun updateStatus(status : String)
    {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref!!.updateChildren(hashMap)


    }

    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updateStatus("offline")
    }
}
