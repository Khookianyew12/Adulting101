package com.example.adulting101

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categoriesRecyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        // Correctly setting LinearLayoutManager with horizontal orientation
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val categoryList = listOf("Cooking", "Car Maintenance", "Self Care", "Personal Finance")
        categoriesRecyclerView.adapter = CategoryAdapter(categoryList) // Ensure CategoryAdapter is properly implemented
    }
}
