package com.example.adulting101

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.adulting101.R
import com.google.firebase.database.FirebaseDatabase

class SubcategoryContentAdapter(
    private val context: Context,
    private val contentList: List<Content>,
    private val categoryName: String,
    private val subcategoryName: String
) : RecyclerView.Adapter<SubcategoryContentAdapter.SubcategoryContentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryContentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_subcategory_content, parent, false)
        return SubcategoryContentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubcategoryContentViewHolder, position: Int) {
        val currentItem = contentList[position]

        holder.titleTextView.text = currentItem.title
        holder.descriptionTextView.text = currentItem.description

        // Check if video URL is available
        if (currentItem.videoUrl.isNotEmpty()) {
            // If video URL is available, show the video icon
            holder.videoIconImageView.visibility = View.VISIBLE
            holder.videoIconImageView.setOnClickListener {
                // When video icon is clicked, open the video in the default video player
                val videoUri = Uri.parse(currentItem.videoUrl)
                val videoIntent = Intent(Intent.ACTION_VIEW, videoUri)
                context.startActivity(videoIntent)
            }
        } else {
            // If video URL is not available, hide the video icon
            holder.videoIconImageView.visibility = View.GONE
        }

        holder.editButton.setOnClickListener {
            // Implement edit functionality here
            val intent = Intent(context, EditContentActivity::class.java)
            intent.putExtra("content", currentItem)
            intent.putExtra("categoryName", categoryName) // Pass category name
            intent.putExtra("subcategoryName", subcategoryName) // Pass subcategory name
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            // Implement delete functionality here
            showDeleteDialog(currentItem)
        }

    }
    private fun showDeleteDialog(currentItem: Content) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.remove_dialog, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        // Handle button click events
        dialogView.findViewById<Button>(R.id.CDeleteButton).setOnClickListener {
            // Implement delete functionality here
            // Call the function to delete the content
            deleteContent(currentItem)
            dialog.dismiss()
        }
    }
    private fun deleteContent(content: Content) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val contentRef = databaseRef
            .child("Course")
            .child(categoryName)
            .child(subcategoryName)
            .child(content.id) // Assuming content.id is the key of the content to be deleted

        contentRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Content deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to delete content: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    override fun getItemCount() = contentList.size

    class SubcategoryContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val videoIconImageView: ImageView = itemView.findViewById(R.id.videoIconImageView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }
}
