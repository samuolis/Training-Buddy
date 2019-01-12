package com.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.models.CommentMessage
import kotlinx.android.synthetic.main.details_comments_list_item.view.*

class EventCommentsRecyclerViewAdapter(commentMessages: List<CommentMessage>?, context: Context, onClickListener: MyClickListener?):
        RecyclerView.Adapter<EventCommentsRecyclerViewAdapter.ViewHolder>() {

    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context
    val commentMessages = commentMessages
    var myClickListener = onClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        var view: View = layoutInflater.inflate(R.layout.details_comments_list_item, parent, false)
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
            holder.commentLayout.setOnClickListener { myClickListener?.onItemClicked(position) }
        }
    }


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val userName = view.details_comment_name
        val messageText = view.details_comment_text
        val commentLayout = view.comment_layout
    }

    interface MyClickListener {
        fun onItemClicked(position: Int)
    }

}