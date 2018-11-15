package com.example.lukas.trainerapp.ui.fragments


import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.enums.ProfilePicture
import com.example.lukas.trainerapp.utils.DrawableUtils
import kotlinx.android.synthetic.main.fragment_account_edit.*
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileFragment : Fragment() {

    lateinit var bitmap : Bitmap
    private val r = Rect()
    lateinit var userViewModel : UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        rootView.post({
            setupInfo()
        })
        return rootView
    }

    private fun setupInfo(){
        userViewModel.user.observe(this, Observer { user: User ->
            if (user.profilePictureIndex == null || user.profilePictureIndex!! >= ProfilePicture.values().size){
                DrawableUtils.setupInitials(initials_image_view, user)
            } else{
                initials_image_view.setImageResource(ProfilePicture.values()[user.profilePictureIndex!!].drawableId)
            }
            user_full_name_text_view.text = user.fullName
            user_email_text_view.text = user.email
            user_phone_number_text_view.text = user.phoneNumber
        })
        user_full_name_text_view.setOnClickListener({
            (activity as NavigationActivity).showAccountEditDialogFragment()
        })
        user_email_text_view.setOnClickListener({
            (activity as NavigationActivity).showAccountEditDialogFragment()
        })
        user_phone_number_text_view.setOnClickListener({
            (activity as NavigationActivity).showAccountEditDialogFragment()
        })

    }
}
