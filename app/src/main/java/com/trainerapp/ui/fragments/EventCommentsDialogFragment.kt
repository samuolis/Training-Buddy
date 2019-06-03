package com.trainerapp.ui.fragments


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.models.CommentMessage
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.CommentsDetailsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_event_comments_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class EventCommentsDialogFragment : BaseDialogFragment() {

    lateinit var eventDetailsViewModel: EventDetailsViewModel
    @Inject
    lateinit var eventWebService: EventWebService
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val eventId: Long by lazy {
        arguments!!.getLong(EventDetailsDialogFragment.ARG_EVENT_ID)
    }

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_event_comments_dialog, container, false)
        val userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0") ?: "0"

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventDetailsViewModel = getViewModel(viewModelFactory)
        eventDetailsViewModel.loadDetailsEvent(eventId)
        val commentsLinearLayout = LinearLayoutManager(context)
        commentsLinearLayout.orientation = RecyclerView.VERTICAL
        commentsLinearLayout.reverseLayout = true
        event_comments_recycler_view.layoutManager = commentsLinearLayout
        comment_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(500))

        comment_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val editTextString = comment_edit_text.text.toString().trim()
                confirm_message_submit_text_view.isEnabled = !editTextString.isEmpty()
            }

        })

        eventDetailsViewModel.detailsOneEvent.observe(this, androidx.lifecycle.Observer { event ->
            event_comments_recycler_view.adapter = CommentsDetailsRecyclerViewAdapter(
                    commentMessages = event.eventComments,
                    context = context!!
            )
        })

        confirm_message_submit_text_view.setOnClickListener { view ->
            if (comment_edit_text.text.toString().length <= 100) {
                comment_edit_text.isEnabled = false
                sendCommentMessage(eventId)
            } else {
                Snackbar.make(event_comments_layout, "Text is too long", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    private fun sendCommentMessage(eventId: Long?) {
        showProgressBarComment()
        val message = comment_edit_text.text.toString()
        val timeNow = Calendar.getInstance().timeInMillis
        val commentMessage = CommentMessage(message, userId, eventId, timeNow, "")
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful()) {
                val token = it.getResult()?.getToken();
                eventWebService.createCommentMessage(commentMessage, token).enqueue(object : Callback<CommentMessage> {
                    override fun onFailure(call: Call<CommentMessage>, t: Throwable) {
                        hideProgressBarComment()
                        Snackbar.make(event_comments_layout, "Failed to send message", Snackbar.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<CommentMessage>, response: Response<CommentMessage>) {
                        hideProgressBarComment()
                        comment_edit_text.text = SpannableStringBuilder("")
                        comment_edit_text.isEnabled = true
                        eventDetailsViewModel.loadDetailsEvent(eventId)
                    }

                })
            } else{
                hideProgressBarComment()
                Snackbar.make(event_comments_layout, "Failed to send message", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun showProgressBarComment() {
//        progress_bar_background_event_details_comment_recycler.layoutParams =
//                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        progress_bar_background_event_details_comment_recycler.setVisibility(View.VISIBLE)
        progress_event_details_comment_recycler.setVisibility(View.VISIBLE)
        confirm_message_submit_text_view.isEnabled = false
    }

    private fun hideProgressBarComment() {
        progress_bar_background_event_details_comment_recycler.setVisibility(View.GONE)
        progress_event_details_comment_recycler.setVisibility(View.GONE)
        confirm_message_submit_text_view.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.event_details_title_label)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(mMessageReceiver,
                IntentFilter(NavigationActivity.BROADCAST_REFRESH))
    }

    //Must unregister onPause()
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mMessageReceiver)
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(NavigationActivity.NOTIFICATION_EVENT_KEY)) {
                NavigationActivity.NOTIFICATION_EVENT_COMMENT_VALUE -> {
                    val id = intent.getStringExtra(NavigationActivity.EVENT_ID_INTENT)
                    if (id != "" && id.toLong() == eventId) {
                        eventDetailsViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                }
                NavigationActivity.NOTIFICATION_EVENT_REFRESH_VALUE -> {
                    val id = intent.getStringExtra(NavigationActivity.EVENT_ID_INTENT)
                    if (id != "" && id.toLong() == eventId) {
                        eventDetailsViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                }
            }
        }
    }

    companion object {
        val ARG_EVENT_ID = "EVENT_ID"

        fun newInstance(
                eventId: Long? = null
        ): EventCommentsDialogFragment {
            return EventCommentsDialogFragment().apply {
                arguments = Bundle().apply {
                    eventId?.let {
                        putLong(ARG_EVENT_ID, it)
                    }
                }
            }
        }
    }
}
