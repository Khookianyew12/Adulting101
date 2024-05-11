package com.example.adulting101

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.adulting101.Content
import com.example.adulting101.SubcategoryContentAdapter


class SubCategoryListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryName: String
    private lateinit var subcategoryName: String
    private lateinit var database: DatabaseReference

    private lateinit var editbtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_category_list)

        // Get category and subcategory names passed from SubCategoryActivity
        categoryName = intent.getStringExtra("categoryName") ?: ""
        subcategoryName = intent.getStringExtra("subcategoryName") ?: ""
        title = subcategoryName

        recyclerView = findViewById(R.id.subCategoryListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference
            .child("Course")
            .child(categoryName)
            .child(subcategoryName)

        // Retrieve contents of the subcategory from Firebase
        val contentList = mutableListOf<Content>() // Initialize the content list

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contentList.clear()
                for (contentSnapshot in snapshot.children) {
                    val id = contentSnapshot.key ?: "" // Fetch the id
                    val title = contentSnapshot.child("title").getValue(String::class.java) ?: ""
                    val videoUrl = contentSnapshot.child("videoUrl").getValue(String::class.java) ?: ""
                    val description = contentSnapshot.child("description").getValue(String::class.java) ?: ""
                    contentList.add(Content(id, title, videoUrl, description)) // Include id in Content object
                }

                // Create adapter with the retrieved content list
                val adapter = SubcategoryContentAdapter(this@SubCategoryListActivity, contentList, categoryName, subcategoryName)
                recyclerView.adapter = adapter
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e(TAG, "Database error occurred: ${error.message}")
                Toast.makeText(this@SubCategoryListActivity, "Database error occurred", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
