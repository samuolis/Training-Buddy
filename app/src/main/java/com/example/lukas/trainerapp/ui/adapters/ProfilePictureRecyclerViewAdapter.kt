package com.example.lukas.trainerapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.lukas.trainerapp.AppExecutors
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.enums.ProfilePicture
import com.example.lukas.trainerapp.ui.NavigationActivity

class ProfilePictureRecyclerViewAdapter(activity: NavigationActivity, user: User) : RecyclerView.Adapter<ProfilePictureRecyclerViewAdapter.ViewHolder>() {

    var activity: NavigationActivity = activity
    var user: User = user

    override fun getItemCount(): Int {
        return ProfilePicture.values().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setImageResource(ProfilePicture.values()[position].drawableId)
        holder.imageView.setOnClickListener {
            AppExecutors.getInstance().diskIO().execute {
                var mDb = AppDatabase.getInstance(activity)
                user.profilePictureIndex = position
                mDb.userDao().insertUser(user)
                activity.backOnStack()

            }
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