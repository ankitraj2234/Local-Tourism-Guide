package com.example.localtourismguide

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

data class Feedback(
    var id: Int = 0,
    val username: String,
    val locationName: String,
    val text: String?,
    val image: ByteArray?,
    var rating: Float = 0.0f,
    var averageRating: Float = 0.0f

)

class FeedbackAdapter(

    private val feedbackList: MutableList<Feedback>,
    private val isMyFeedback: Boolean,
    private val databaseHelper: DatabaseHelper,
    private val updateAverageRatingCallback: (String, Float) -> Unit= { _, _ -> }

) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImageView: ImageView = view.findViewById(R.id.profile_image_view)
        val usernameTextView: TextView = view.findViewById(R.id.username_text_view)
        val feedbackTextView: TextView = view.findViewById(R.id.feedback_text_view)
        val feedbackImageView: ImageView = view.findViewById(R.id.feedback_image_view)
        val ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
        val myfeedbackLayout: LinearLayout = view.findViewById(R.id.myfeedback_layout)
        val editButton: Button = view.findViewById(R.id.edit_button) // Add this button in your layout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        // Add an edit button next to the delete button

        val feedback = feedbackList[position]
        // Display the average rating
        val averageRatingTextView: TextView = holder.itemView.findViewById(R.id.average_rating_text_view)
        averageRatingTextView.text = String.format("Avg Rating: %.1f", feedback.averageRating)


        holder.usernameTextView.text = feedback.username
        holder.feedbackTextView.text = feedback.text ?: ""

        // Update feedback rating in database when changed
        holder.ratingBar.rating = feedback.rating
        if (!isMyFeedback) {
            holder.ratingBar.setIsIndicator(false) // Allow rating for other users' feedback
            holder.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                feedback.rating = rating
                databaseHelper.updateFeedbackRating(feedback.id, rating) // Update rating in DB
                updateAverageRating(feedback.locationName) // Update the average rating immediately
            }



        } else {
            holder.ratingBar.setIsIndicator(true) // Prevent rating for the user's own feedback
        }




        // Display feedback image if available
        if (feedback.image != null) {
            val bitmap = BitmapFactory.decodeByteArray(feedback.image, 0, feedback.image.size)
            holder.feedbackImageView.setImageBitmap(bitmap)
            holder.feedbackImageView.visibility = View.VISIBLE // Make ImageView visible
        } else {
            holder.feedbackImageView.visibility = View.GONE // Hide ImageView if no image
        }

        // Display user profile image
        val userImage = databaseHelper.getUserImage(feedback.username)
        if (userImage != null) {
            val bitmap = BitmapFactory.decodeByteArray(userImage, 0, userImage.size)
            holder.profileImageView.setImageBitmap(bitmap)
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_user_profile)
        }

        // Show delete button for user's feedback only and edit functionality
        if (isMyFeedback) {
            holder.myfeedbackLayout.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            holder.ratingBar.visibility = View.GONE

            holder.deleteButton.setOnClickListener {
                val feedbackId = feedback.id
                val success = databaseHelper.deleteFeedback(feedbackId)

                if (success) {
                    feedbackList.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(holder.itemView.context, "Feedback deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(holder.itemView.context, "Failed to delete feedback", Toast.LENGTH_SHORT).show()
                }
            }

            // Add Edit Button functionality
            val editButton: Button = holder.itemView.findViewById(R.id.edit_button)
            editButton.visibility = View.VISIBLE
            editButton.setOnClickListener {
                val activity = holder.itemView.context as FeedbackActivity
                activity.populateFeedbackForEdit(feedback)
                feedbackList.removeAt(position) // Remove from adapter
                notifyItemRemoved(position)
                databaseHelper.deleteFeedback(feedback.id) // Remove from the database
                Toast.makeText(holder.itemView.context, "Editing feedback...", Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.deleteButton.visibility = View.GONE
            holder.itemView.findViewById<Button>(R.id.edit_button).visibility = View.GONE
        }


    }



    override fun getItemCount(): Int = feedbackList.size

    fun setFeedbackList(feedbacks: List<Feedback>) {
        feedbackList.clear()
        feedbackList.addAll(feedbacks)
        notifyDataSetChanged()
    }

    fun addFeedback(feedback: Feedback) {
        feedbackList.add(feedback)
        notifyItemInserted(feedbackList.size - 1)
    }

    private fun updateAverageRating(locationName: String) {
        // Get all feedback for the location
        val feedbacks = databaseHelper.getFeedbacksForLocationWithAverage(locationName)
        val totalRatings = feedbacks.sumOf { it.rating.toDouble() }
        val averageRating = if (feedbacks.isNotEmpty()) totalRatings / feedbacks.size else 0.0

        // Update the database with the new average rating
        feedbacks.forEach { feedback ->
            feedback.averageRating = averageRating.toFloat()
            databaseHelper.updateFeedbackRating(feedback.id, feedback.rating)
        }

        // Update the UI immediately
        Handler(Looper.getMainLooper()).post {
            updateAverageRatingCallback(locationName, averageRating.toFloat())
            notifyDataSetChanged()
        }

        Log.d("FeedbackAdapter", "Average Rating updated: $averageRating")
    }


}