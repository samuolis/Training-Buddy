package com.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.enums.ProfilePicture
import com.trainerapp.models.User
import com.trainerapp.utils.DrawableUtils
import kotlinx.android.synthetic.main.event_details_recyclerview_item.view.*

class EventDetailsRecyclerViewAdapter(usersList: List<User>?, context: Context, onClickListener: MyClickListener): RecyclerView.Adapter<EventDetailsRecyclerViewAdapter.ViewHolder>() {

    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context
    val usersList = usersList
    var myClickListener = onClickListener

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
            if (usersList[position].profilePictureIndex == null || usersList[position].profilePictureIndex!! >= ProfilePicture.values().size){
                DrawableUtils.setupInitialsForDetails(holder.userIcon, usersList[position], 100)
            } else{
                holder.userIcon.setImageResource(ProfilePicture.values()[usersList[position].profilePictureIndex!!].drawableId)
            }
            holder.userIconLayout.setOnClickListener { myClickListener?.onItemClicked(position) }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val userIcon = view.event_signed_users_item_imageview
        val userIconLayout = view.signed_users_layout_details

    }

    interface MyClickListener {
        fun onItemClicked(position: Int)
    }
}