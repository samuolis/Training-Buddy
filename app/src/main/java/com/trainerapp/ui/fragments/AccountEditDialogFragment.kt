package com.trainerapp.ui.fragments


import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.enums.ProfilePicture
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.models.User
import com.trainerapp.navigation.NavigationController
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.utils.DrawableUtils
import kotlinx.android.synthetic.main.fragment_account_edit.*
import java.util.*
import javax.inject.Inject

class AccountEditDialogFragment : BaseDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var navigationController: NavigationController
    lateinit var eventViewModel: EventViewModel
    var userId: String? = null
    var databaseId: Long = 0
    var profileInt: Int? = null
    var signedEvents: List<Long>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_account_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(100))
        eventViewModel = getViewModel(viewModelFactory)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        eventViewModel.error.nonNullObserve(this) {
            Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
        eventViewModel.user.observe(this, Observer { user: User ->
            profileInt = user.profilePictureIndex
            if (user.profilePictureIndex == null || user.profilePictureIndex!! >= ProfilePicture.values().size) {
                DrawableUtils.setupInitials(initials_image_view_fragment_edit, user)
            } else {
                initials_image_view_fragment_edit.setImageResource(ProfilePicture.values()[user.profilePictureIndex!!].drawableId)
            }
            name_edit_text.text = SpannableStringBuilder(user.fullName)
            userId = user.userId
            databaseId = user.id
            edit_profile_submit_text_view.setOnClickListener {
                submitEdit()
            }
            initials_image_view_fragment_edit.setOnClickListener {
                navigationController.showProfilePictureDialogFragment()
            }
            edit_profile_default_picture_text_view.setOnClickListener {
                profileInt = ProfilePicture.values().size
                DrawableUtils.setupInitials(initials_image_view_fragment_edit, user)
            }
            signedEvents = user.signedEventsList
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    private fun submitEdit() {
        // Reset errors.
        name_edit_text.error = null

        // Store values at the time of the login attempt.
        val fullName = name_edit_text.getText().toString()

        val currentTime = Calendar.getInstance().time
        val user = User(userId, fullName, currentTime, profileInt, signedEvents)

        eventViewModel.postUser(user)
    }
}
