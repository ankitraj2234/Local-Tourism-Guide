package com.example.localtourismguide

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


data class Memory(
    val id: Int,
    val username: String,
    val locationName: String,
    val image: ByteArray,
    val description: String
)

class MemoriesAdapter(
    private var memories: List<Memory>,
    private val onDelete: (Memory) -> Unit
) : RecyclerView.Adapter<MemoriesAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memories[position]
        holder.bind(memory, onDelete)
    }

    override fun getItemCount() = memories.size

    fun updateData(newMemories: List<Memory>) {
        memories = newMemories
        notifyDataSetChanged()
    }

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.memoryImageView)
        private val descriptionView: TextView = itemView.findViewById(R.id.memoryDescription)

        fun bind(memory: Memory, onDelete: (Memory) -> Unit) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(memory.image, 0, memory.image.size))
            descriptionView.text = memory.description

            itemView.setOnLongClickListener {
                onDelete(memory)
                true
            }
        }
    }
}
