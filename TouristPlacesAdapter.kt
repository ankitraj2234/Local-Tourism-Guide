package com.example.localtourismguide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.localtourismguide.R
import com.example.localtourismguide.Venue

class TouristPlacesAdapter(private var places: List<Venue>) : RecyclerView.Adapter<TouristPlacesAdapter.TouristPlaceViewHolder>() {

    class TouristPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.placeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristPlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tourist_place_item, parent, false)
        return TouristPlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: TouristPlaceViewHolder, position: Int) {
        holder.placeName.text = places[position].name // Corrected to use Venue.name
    }

    override fun getItemCount(): Int {
        return places.size
    }

    fun updateData(newPlaces: List<Venue>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}
