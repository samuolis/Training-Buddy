package com.example.lukas.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.enums.ProfilePicture
import com.example.lukas.trainerapp.utils.DrawableUtils
import kotlinx.android.synthetic.main.event_details_recyclerview_item.view.*

class EventDetailsRecyclerViewAdapter(usersList: List<User>?, context: Context): RecyclerView.Adapter<EventDetailsRecyclerViewAdapter.ViewHolder>() {

    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context
    val usersList = usersList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        var view: View = layoutInflater.inflate(R.layout.event_details_recyclerview_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usersList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (usersList != null) {
            holder.userName.text = usersList[position].fullName
            if (usersList[position].profilePictureIndex == null || usersList[position].profilePictureIndex!! >= ProfilePicture.values().size){
                DrawableUtils.setupInitialsForDetails(holder.userIcon, usersList[position])
            } else{
                holder.userIcon.setImageResource(ProfilePicture.values()[usersList[position].profilePictureIndex!!].drawableId)
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val userName = view.event_detail_item_textview
        val userIcon = view.event_detail_item_imageview

    }
}