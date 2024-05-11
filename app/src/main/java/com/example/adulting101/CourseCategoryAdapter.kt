package com.example.adulting101

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class CourseCategoryAdapter(private val categories: List<CourseCategory>, private val onItemClick: (CourseCategory) -> Unit) : RecyclerView.Adapter<CourseCategoryAdapter.ViewHolder>() {
    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val CategoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTV)
        fun bind(category: CourseCategory) {
            itemView.setOnClickListener { onItemClick(category) }
            CategoryNameTextView.text = category.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}