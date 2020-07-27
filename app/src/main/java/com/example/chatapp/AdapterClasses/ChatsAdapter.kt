package com.example.chatapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.ModelClasses.Chat
import com.example.chatapp.ModelClasses.ChatList
import com.example.chatapp.R
import com.example.chatapp.ViewFullmageActivity
import com.example.chatapp.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    mContext : Context,
    mChatList: List<Chat>,
    imageUrl : String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>()
{

    private val mContext : Context
    private val mChatList : List<Chat>
    private val imageUrl : String
    var firebaseUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {

        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl

    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {

        //on position 0 we have message_item_left layout and on position 1 we have message_item_right layout
        return if(position == 1)
        {
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent,false)
            ViewHolder(view)
        }

        else
        {
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent,false)
            ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {

        return mChatList.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

       val chat : Chat = mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profile_image)

        //for image message

        if(chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
        {

            //image message --right side means sender side
            if (chat.getSender().equals(firebaseUser!!.uid))
            {
                holder.show_text_message!!.visibility = View.GONE
                holder.right_image_view!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)

                holder.right_image_view!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )

                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")

                    builder.setItems(options, DialogInterface.OnClickListener{
                        dialog, which ->
                      //user click on first option that is View Full Image
                        if(which == 0)
                        {
                            val intent = Intent(mContext, ViewFullmageActivity::class.java)
                            intent.putExtra("url",chat.getUrl())
                            mContext.startActivity(intent)

                        }
                        //user click on second option that is Delete Image
                        else if(which == 1)
                        {
                            deleteSentMessage(position,holder)

                        }
                    })
                    builder.show()
                }
            }

            //image message --left side means receiver side
            else if (!chat.getSender().equals(firebaseUser!!.uid))
            {
                holder.show_text_message!!.visibility = View.GONE
                holder.left_image_view!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.left_image_view)

                holder.left_image_view!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )

                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")

                    builder.setItems(options, DialogInterface.OnClickListener{
                            dialog, which ->
                        //user click on first option that is View Full Image
                        if(which == 0)
                        {
                            val intent = Intent(mContext, ViewFullmageActivity::class.java)
                            intent.putExtra("url",chat.getUrl())
                            mContext.startActivity(intent)

                        }

                    })
                    builder.show()
                }

            }
        }

        //text Messages
        else
        {
            holder.show_text_message!!.text = chat.getMessage()

           if(firebaseUser!!.uid == chat.getSender())
           {
               holder.show_text_message!!.setOnClickListener{
                   val options = arrayOf<CharSequence>(
                       "Delete Message",
                       "Cancel"
                   )

                   var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                   builder.setTitle("What do you want?")

                   builder.setItems(options, DialogInterface.OnClickListener{
                           dialog, which ->

                       //user click on second option that is Delete Message
                       if(which == 0)
                       {
                           deleteSentMessage(position,holder)

                       }
                   })
                   builder.show()
               }
           }

        }

        //sent and seen messages
        if(position == mChatList.size-1)
        {
            if (chat.isIsSeen())
            {

                holder.text_seen!!.text = "Seen"

                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
                {

                    val lp: RelativeLayout.LayoutParams? =
                        holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    holder.text_seen!!.layoutParams = lp

                }
            }

            else{

                holder.text_seen!!.text = "Sent"

                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
                {

                    val lp: RelativeLayout.LayoutParams? =
                        holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    holder.text_seen!!.layoutParams = lp

                }

            }
        }
        else
        {
            holder.text_seen!!.visibility = View.GONE
        }

    }

    //retrieving messages on the message chat activity
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var profile_image : CircleImageView? = null
        var show_text_message : TextView? = null
        var left_image_view : ImageView? = null
        var text_seen : TextView? = null
        var right_image_view : ImageView? = null


        init {

            profile_image = itemView.findViewById(R.id.profile_image)
            show_text_message = itemView.findViewById(R.id.show_text_message)
            left_image_view = itemView.findViewById(R.id.left_image_view)
            text_seen = itemView.findViewById(R.id.text_seen)
            right_image_view = itemView.findViewById(R.id.right_image_view)

        }

    }


    override fun getItemViewType(position: Int): Int
    {
       //if the firebaseUser id is equal to sender id then this will be onlineUser and
        // we will return message_item_right layout other message_item_left layout.
        return if(mChatList[position].getSender().equals(firebaseUser!!.uid))
        {
           1
        }
        else{

            //receiver
            0
        }
    }

    private fun deleteSentMessage(position: Int, holder: ChatsAdapter.ViewHolder)

    {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).getMessageId()!!)
            .removeValue()
            .addOnCompleteListener{task ->

                if(task.isSuccessful)
                {
                    Toast.makeText(holder.itemView.context, "Deleted", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(holder.itemView.context, "Failed, Not Deleted", Toast.LENGTH_SHORT).show()

                }
            }

    }

}