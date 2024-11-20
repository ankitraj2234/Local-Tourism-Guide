package com.example.localtourismguide

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

data class FavoritePlace(
    val id: Int,
    val locationName: String,
    val image: ByteArray
)

class FavoritePlacesAdapter(
    private var favoritePlacesList: List<FavoritePlace>,
    private val onItemClick: (FavoritePlace) -> Unit,
    private val onDeleteClick: (FavoritePlace) -> Unit
) : RecyclerView.Adapter<FavoritePlacesAdapter.FavoritePlacesViewHolder>() {

    inner class FavoritePlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameTextView: TextView = itemView.findViewById(R.id.locationNameTextView)
        val locationImageView: CircleImageView = itemView.findViewById(R.id.locationImageView)

        fun bind(favoritePlace: FavoritePlace) {
            locationNameTextView.text = favoritePlace.locationName
            locationImageView.setImageBitmap(
                BitmapFactory.decodeByteArray(favoritePlace.image, 0, favoritePlace.image.size)
            )

            // Click listener for navigating to MemoriesActivity
            itemView.setOnClickListener {
                onItemClick(favoritePlace)
            }

            // Long-click listener for deleting a favorite place
            itemView.setOnLongClickListener {
                onDeleteClick(favoritePlace)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlacesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_place, parent, false)
        return FavoritePlacesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritePlacesViewHolder, position: Int) {
        holder.bind(favoritePlacesList[position])
    }

    override fun getItemCount(): Int = favoritePlacesList.size

    // Update data for the adapter and notify changes
    fun updateData(newList: List<FavoritePlace>) {
        favoritePlacesList = newList
        notifyDataSetChanged()
    }
}
