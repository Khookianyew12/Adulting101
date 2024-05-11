package com.example.adulting101

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubCategoryAdapter(private val subcategories: List<SubCategory>, private val onItemClick: (SubCategory) -> Unit) : RecyclerView.Adapter<SubCategoryAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subCategoryNameTextView: TextView = itemView.findViewById(R.id.subCategoryNameTextView)

        fun bind(subcategory: SubCategory) {
            itemView.setOnClickListener { onItemClick(subcategory) }
            subCategoryNameTextView.text = subcategory.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sub_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subcategory = subcategories[position]
        holder.bind(subcategory)
    }

    override fun getItemCount(): Int {
        return subcategories.size
    }
}
