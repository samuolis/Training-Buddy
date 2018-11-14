package com.example.lukas.trainerapp.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.enums.ProfilePicture
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.utils.DrawableUtils

class ProfilePictureRecyclerViewAdapter(bitmap: Bitmap?, activity: NavigationActivity) : RecyclerView.Adapter<ProfilePictureRecyclerViewAdapter.ViewHolder>() {

    var activity: NavigationActivity = activity

    override fun getItemCount(): Int {
        return ProfilePicture.values().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setImageResource(ProfilePicture.values()[position].drawableId)
        holder.imageView.setOnClickListener {
            var userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
            userViewModel.setmProfilePicture(position)
            (activity as NavigationActivity).backOnStack()
        }
    }

    class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ProfilePictureRecyclerViewAdapter.ViewHolder {
        // create a new view
        val imageView = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_recyclerview_item, parent, false) as ImageView
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(imageView)
    }

}