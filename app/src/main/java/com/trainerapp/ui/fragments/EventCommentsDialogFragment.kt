package com.trainerapp.ui.fragments


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.GsonBuilder

import com.trainerapp.R
import com.trainerapp.models.CommentMessage
import com.trainerapp.ui.adapters.CommentsDetailsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_event_comments_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class EventCommentsDialogFragment : DialogFragment() {

    lateinit var eventViewModel: EventViewModel
    lateinit var eventWebService: EventWebService
    lateinit var auth: FirebaseAuth
    lateinit var userId: String
    var currentUser: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_event_comments_dialog, container, false)

        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        var userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0") ?: "0"

        val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(eventViewModel?.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        eventWebService = retrofit.create(EventWebService::class.java)

        rootView.post {

            var commentsLinearLayout = LinearLayoutManager(context)
            commentsLinearLayout.orientation = RecyclerView.VERTICAL
            commentsLinearLayout.reverseLayout = true
            event_comments_recycler_view.layoutManager = commentsLinearLayout
            comment_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(500))

            comment_edit_text.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    var editTextString = comment_edit_text.text.toString().trim()
                    confirm_message_submit_text_view.isEnabled = !editTextString.isEmpty()
                }

            })

            eventViewModel.getEventComments()?.observe(this, androidx.lifecycle.Observer {
                event_comments_recycler_view.adapter = CommentsDetailsRecyclerViewAdapter(it, context!!)
                event_comments_recycler_view.smoothScrollToPosition(it.size - 5)
            })

            eventViewModel.getDetailsOneEvent()?.observe(this, androidx.lifecycle.Observer {
                confirm_message_submit_text_view.setOnClickListener { view ->
                    if (comment_edit_text.text.toString().length <= 100){
                        comment_edit_text.isEnabled = false
                        sendCommentMessage(it.eventId)
                    } else{
                        Snackbar.make(event_comments_layout, "Text is too long", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }

        return rootView
    }


    fun sendCommentMessage(eventId: Long?){
        showProgressBarComment()
        var message = comment_edit_text.text.toString()
        var timeNow = Calendar.getInstance().timeInMillis
        var commentMessage = CommentMessage(message, userId, eventId, timeNow, "")
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful()) {
                var token = it.getResult()?.getToken();
                eventWebService.createCommentMessage(commentMessage, token).enqueue(object : Callback<CommentMessage> {
                    override fun onFailure(call: Call<CommentMessage>, t: Throwable) {
                        hideProgressBarComment()
                        Snackbar.make(event_comments_layout, "Failed to send message", Snackbar.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<CommentMessage>, response: Response<CommentMessage>) {
                        hideProgressBarComment()
                        comment_edit_text.text = SpannableStringBuilder("")
                        comment_edit_text.isEnabled = true
                        eventViewModel.loadEventComments(eventId)
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


}
