package com.example.localtourismguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.localtourismguide.R
import com.example.localtourismguide.Venue


// HotelsAdapter.kt
class HotelsAdapter(private var hotels: List<Venue>) : RecyclerView.Adapter<HotelsAdapter.HotelViewHolder>() {

    class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hotelName: TextView = itemView.findViewById(R.id.hotelName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.hotelName.text = hotels[position].name
    }

    override fun getItemCount(): Int {
        return hotels.size
    }

    fun updateData(newHotels: List<Venue>) {
        hotels = newHotels
        notifyDataSetChanged()
    }
}


