package com.example.chatapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.ModelClasses.Chat
import com.example.chatapp.ModelClasses.Users
import com.example.chatapp.R
import com.example.chatapp.VisitUserProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter(
               mContext : Context,
               mUsers : List<Users>,
               isChatCheck :Boolean
                 ) :RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
     private val mContext : Context
     private val mUsers : List<Users>
     private var isChatCheck :Boolean
    var lastMsg : String = ""

    //initializing
    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent,false)
            return UserAdapter.ViewHolder(view)

    }

//use to get the total number of childs from our list that is how many data exists in our database. so we have to pass the size here.
    override fun getItemCount(): Int {
      return mUsers.size
    }

//basically use to display the data on over these controllers that we just initialize here
// means username , profile, online, offline last message  in ViewHolder Class
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val user : Users = mUsers[position]
        holder.userNameTxt.text = user!!.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)

        //retrieve last message
        if(isChatCheck)
        {
            retrieveLastMessage(user.getUID(), holder.lastMessageTxt)
        }

        else{
            holder.lastMessageTxt.visibility = View.GONE
        }

        //display the green and gray icon that is online and offline status
        if(isChatCheck)
        {
            if(user.getStatus() == "online")
            {
                //green dot
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            }

            else
            {
                //gray dot
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE

            }
        }
        else
        {
            holder.onlineImageView.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE

        }

        //if a user click on any user profile
        holder.itemView.setOnClickListener{

            //displaying two options to the user so we are using arrayOf
            val options = arrayOf<CharSequence>(
                //first option is Send Message and second option is Visit Profile
                "Send Message",
                "Visit Profile"
            )
            val builder : AlertDialog.Builder = AlertDialog.Builder(mContext)
            //title of the dialog box then the two options will be available
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener{ dialog, position ->
              //index for the first option is zero
                if(position == 0)
                {
                  val intent = Intent(mContext, MessageChatActivity::class.java)
                    //id of person profile which we are going to visit
                    //that person on which i will click
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }


                //index for the second option is 1.
                if (position == 1)
                {
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    //id of person profile which we are going to visit
                    //that person on which i will click
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)

                }


            })
             builder.show()

        }

    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    {
        // all the controllers that we access inside this ViewHolder
        var userNameTxt : TextView
        var profileImageView : CircleImageView
        var onlineImageView : CircleImageView
        var offlineImageView: CircleImageView
        var lastMessageTxt : TextView

        init{
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
        }
        //aad the userlayout means how this user adapter will know that to which xml file all these controllers belong

    }

    //retrieve and display the last message on the chatList.
    private fun retrieveLastMessage(chatUserId: String?, lastMessageTxt: TextView)
    {

        lastMsg = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                for(dataSnapshot in p0.children)
                {
                    val chat : Chat? = dataSnapshot.getValue(Chat::class.java)

                    if(firebaseUser!=null && chat!=null)
                    {

                        if(chat.getReceiver() == firebaseUser!!.uid &&
                            chat.getSender() == chatUserId ||
                                chat.getReceiver() == chatUserId &&
                                chat.getSender() == firebaseUser!!.uid)
                        {

                            lastMsg = chat.getMessage()!!

                        }

                    }
                }

                when(lastMsg)
                {
                    "defaultMsg" -> lastMessageTxt.text = "No Message Received yet.."
                    "sent you an image." -> lastMessageTxt.text = "image sent."
                    else -> lastMessageTxt.text = lastMsg
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}