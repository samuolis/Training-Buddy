package com.trainerapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.enums.ProfilePicture
import com.trainerapp.ui.NavigationActivity

class ProfilePictureRecyclerViewAdapter(activity: NavigationActivity, onClickListener: MyClickListener?) : RecyclerView.Adapter<ProfilePictureRecyclerViewAdapter.ViewHolder>() {

    var activity: NavigationActivity = activity
    var myClickListener = onClickListener

    override fun getItemCount(): Int {
        return ProfilePicture.values().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setImageResource(ProfilePicture.values()[position].drawableId)
        holder.imageView.setOnClickListener { myClickListener?.onItemClicked(position) }
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

    interface MyClickListener {
        fun onItemClicked(position: Int)
    }

}