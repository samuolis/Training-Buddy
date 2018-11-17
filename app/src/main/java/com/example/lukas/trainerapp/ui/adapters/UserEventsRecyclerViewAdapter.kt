package com.example.lukas.trainerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.entity.Event
import kotlinx.android.synthetic.main.event_list_recyclerview_item.view.*

class UserEventsRecyclerViewAdapter(eventsList: List<Event>, context: Context) : RecyclerView.Adapter<UserEventsRecyclerViewAdapter.ViewHolder>() {

    var eventList = eventsList
    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        var view: View = layoutInflater.inflate(R.layout.event_list_recyclerview_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.eventName.text = eventList[position].eventName
        holder.eventDate.text = eventList[position].eventDate.toString()
        holder.eventPlaceName.text = eventList[position].eventLocationName
        holder.eventPlayersCount.text = eventList[position].eventPlayers.toString()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val eventName = view.event_name_text_view
        val eventDate = view.event_date_text_view
        val eventPlayersCount = view.event_players_number_text_view
        val eventPlaceName = view.event_place_name_text_view
    }

}