package com.example.localtourismguide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.localtourismguide.models.ForecastDay

class ForecastAdapter(private val forecastDays: List<ForecastDay>) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val tempTextView: TextView = itemView.findViewById(R.id.tempTextView)
        val conditionTextView: TextView = itemView.findViewById(R.id.conditionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)





    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastDay = forecastDays[position]
        holder.dateTextView.text = forecastDay.date
        holder.tempTextView.text = "${forecastDay.day.avgtemp_c}°C"
        holder.conditionTextView.text = forecastDay.day.condition.text
    }

    override fun getItemCount() = forecastDays.size


}