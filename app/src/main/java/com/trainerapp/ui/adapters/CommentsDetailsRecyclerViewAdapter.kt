package com.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.models.CommentMessage
import kotlinx.android.synthetic.main.event_comment_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class CommentsDetailsRecyclerViewAdapter(commentMessages: List<CommentMessage>?, context: Context):
        RecyclerView.Adapter<CommentsDetailsRecyclerViewAdapter.ViewHolder>() {

    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context
    val commentMessages = commentMessages

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        var view: View = layoutInflater.inflate(R.layout.event_comment_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentMessages?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (commentMessages != null) {
            holder.userName.text = commentMessages[position].messageUserName
            holder.messageText.text = commentMessages[position].messageText
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
            var dateString = formatter.format(Date(commentMessages[position].messageTime!!))
            holder.messageTime.text = dateString
        }
    }


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val userName = view.message_user
        val messageText = view.message_text
        val messageTime = view.message_time


    }
}