package com.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.models.Event
import kotlinx.android.synthetic.main.event_list_recyclerview_item.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class UserEventsRecyclerViewAdapter(eventsList: List<Event>?, context: Context, onClickListener: MyClickListener?) : RecyclerView.Adapter<UserEventsRecyclerViewAdapter.ViewHolder>() {

    var eventList = eventsList
    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context
    var myClickListener = onClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        var view: View = layoutInflater.inflate(R.layout.event_list_recyclerview_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (eventList != null) {
            holder.eventName.text = eventList!![position].eventName
            val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            timeStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateStr = timeStampFormat.format(eventList!![position].eventDate)
            holder.eventDate.text = dateStr
            holder.eventPlaceName.text = eventList!![position].eventLocationName
            var signedUsersCount = eventList!![position].eventSignedPlayers?.size ?: 0
            holder.eventPlayersCount.text = signedUsersCount.toString() + " of " + eventList!![position].eventPlayers.toString()
            if (eventList!![position].eventDistance == null) {
                holder.eventsDistanceLinearLAyout.visibility = View.INVISIBLE
            } else {
                holder.eventsDistanceLinearLAyout.visibility = View.VISIBLE
                holder.eventsDistance.text = DecimalFormat("##.##").format(eventList!![position].eventDistance)
            }
            holder.listContentLayout.setOnClickListener { myClickListener?.onItemClicked(position) }
            var eventDate = eventList!![position].eventDate!!.time
            var oneDayTime = System.currentTimeMillis() + 1000 * 3600 * 26
            var twoHoursTime = System.currentTimeMillis() + 1000 * 3600 * 4
            var nowTime = System.currentTimeMillis() + 1000 * 3600 * 2
            when {
                eventDate > oneDayTime -> {
                    holder.listContentLayout.setBackgroundResource(R.color.event_by_date_green)
                }
                eventDate in twoHoursTime..oneDayTime -> {
                    holder.listContentLayout.setBackgroundResource(R.color.event_by_date_yellow)
                }
                eventDate in nowTime..twoHoursTime -> {
                    holder.listContentLayout.setBackgroundResource(R.color.event_by_date_red)
                }
                else -> {
                    holder.listContentLayout.setBackgroundResource(R.color.event_by_date_grey)
                }
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val eventName = view.event_name_text_view
        val eventDate = view.event_date_text_view
        val eventPlayersCount = view.event_players_number_text_view
        val eventPlaceName = view.event_place_name_text_view
        val eventsDistance = view.event_distance_text_view
        val eventsDistanceLinearLAyout = view.distance_linear_layout
        val listContentLayout = view.list_content_layout
    }

    interface MyClickListener {
        fun onItemClicked(position: Int)
    }

}