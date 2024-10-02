package com.example.localtourismguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.localtourismguide.R
import com.example.localtourismguide.Venue

class EventsAdapter(private var eventsList: MutableList<Venue>) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocation) // For location
        val eventCategory: TextView = itemView.findViewById(R.id.eventCategory) // For category
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventName.text = event.name
        holder.eventLocation.text = event.location.address // Show event location address
        holder.eventCategory.text = event.categories.joinToString { it.name } // Show event categories
    }

    override fun getItemCount() = eventsList.size

    fun updateData(newEvents: List<Venue>) {
        eventsList.clear() // Clear the existing data
        eventsList.addAll(newEvents) // Add the new data
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }
}
